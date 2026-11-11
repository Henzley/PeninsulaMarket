const MESSAGES_API = `${window.location.protocol}//${window.location.hostname}:8080/api`;
const token = localStorage.getItem("token");
const storedUser = JSON.parse(localStorage.getItem("user") || "null");
const loginAs = (localStorage.getItem("loginAs") || "").toUpperCase();
const conversationList = document.getElementById("conversationList");
const messageHistory = document.getElementById("messageHistory");
const chatPlaceholder = document.getElementById("chatPlaceholder");
const activeChat = document.getElementById("activeChat");
const notice = document.getElementById("pageNotice");
let conversations = [];
let activeConversationId = null;
let pollTimer = null;

function escapeText(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[char]));
}

function initials(name) {
  return String(name || "?").trim().split(/\s+/).slice(0, 2).map(part => part[0] || "").join("").toUpperCase();
}

function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const now = new Date();
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  }
  return date.toLocaleDateString([], { day: "numeric", month: "short" });
}

function formatPrice(value) {
  const price = Number(value);
  return Number.isFinite(price) ? `R ${price.toLocaleString("en-ZA", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : "";
}

function showNotice(message) {
  notice.textContent = message;
  notice.hidden = !message;
}

async function api(path, options = {}) {
  const response = await fetch(`${MESSAGES_API}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      Authorization: `Bearer ${token}`,
      ...(options.headers || {})
    }
  });
  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = null; }
  }
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "index.html";
    }
    throw new Error(data?.error || `Request failed (HTTP ${response.status}).`);
  }
  return data;
}

function renderConversations() {
  conversationList.replaceChildren();
  if (!conversations.length) {
    const empty = document.createElement("p");
    empty.className = "inbox-empty";
    empty.textContent = loginAs === "BUYER"
      ? "No conversations yet. Open a listing and choose Message seller to start a chat."
      : "No conversations yet. When a buyer messages you about a listing, the conversation will appear here.";
    conversationList.append(empty);
    return;
  }
  conversations.forEach((conversation) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `conversation-item${conversation.id === activeConversationId ? " active" : ""}`;
    button.dataset.id = conversation.id;
    const avatar = document.createElement("span");
    avatar.className = "conversation-avatar";
    avatar.textContent = initials(conversation.otherUserName);
    const copy = document.createElement("span");
    copy.className = "conversation-copy";
    const top = document.createElement("span");
    top.className = "conversation-topline";
    const name = document.createElement("strong");
    name.textContent = conversation.otherUserName || "Marketplace user";
    const time = document.createElement("time");
    time.textContent = formatTime(conversation.updatedAt);
    top.append(name, time);
    const listing = document.createElement("p");
    listing.className = "conversation-listing";
    listing.textContent = conversation.listingTitle || "Listing";
    const preview = document.createElement("p");
    preview.className = "conversation-preview";
    preview.textContent = conversation.listingPrice == null ? "Open conversation" : formatPrice(conversation.listingPrice);
    copy.append(top, listing, preview);
    if (conversation.unreadCount > 0) {
      const unread = document.createElement("span");
      unread.className = "unread-count";
      unread.textContent = String(conversation.unreadCount);
      unread.setAttribute("aria-label", `${conversation.unreadCount} unread messages`);
      copy.append(unread);
    }
    button.append(avatar, copy);
    button.addEventListener("click", () => openConversation(conversation.id));
    conversationList.append(button);
  });
}

function renderMessages(messages) {
  messageHistory.replaceChildren();
  if (!messages.length) {
    const empty = document.createElement("p");
    empty.className = "message-history-empty";
    empty.textContent = "No messages yet. Say hello to get the conversation started.";
    messageHistory.append(empty);
    return;
  }
  let previousDay = "";
  messages.forEach((message) => {
    const date = new Date(message.createdAt);
    const day = Number.isNaN(date.getTime()) ? "" : date.toLocaleDateString([], { weekday: "long", month: "short", day: "numeric" });
    if (day && day !== previousDay) {
      const divider = document.createElement("div");
      divider.className = "message-day";
      divider.textContent = day;
      messageHistory.append(divider);
      previousDay = day;
    }
    const mine = Number(message.senderId) === Number(storedUser?.id);
    const row = document.createElement("article");
    row.className = `message-row${mine ? " mine" : ""}`;
    const bubble = document.createElement("div");
    bubble.className = "message-bubble";
    bubble.textContent = message.content;
    const meta = document.createElement("div");
    meta.className = "message-meta";
    meta.textContent = `${mine ? "You" : message.senderName} · ${formatTime(message.createdAt)}`;
    row.append(bubble, meta);
    messageHistory.append(row);
  });
  messageHistory.scrollTop = messageHistory.scrollHeight;
}

function showConversation(conversation) {
  chatPlaceholder.hidden = true;
  activeChat.hidden = false;
  document.getElementById("chatAvatar").textContent = initials(conversation.otherUserName);
  document.getElementById("chatPersonName").textContent = conversation.otherUserName || "Marketplace user";
  document.getElementById("chatListingTitle").textContent = conversation.listingTitle || "Listing";
  document.getElementById("chatListingPrice").textContent = formatPrice(conversation.listingPrice);
}

async function openConversation(id) {
  activeConversationId = Number(id);
  const conversation = conversations.find(item => Number(item.id) === activeConversationId);
  if (!conversation) return;
  showConversation(conversation);
  renderConversations();
  messageHistory.replaceChildren();
  const loading = document.createElement("p");
  loading.className = "message-history-empty";
  loading.textContent = "Loading messages…";
  messageHistory.append(loading);
  try {
    const messages = await api(`/messages/conversations/${activeConversationId}/messages`);
    renderMessages(Array.isArray(messages) ? messages : []);
    await api(`/messages/conversations/${activeConversationId}/read`, { method: "PUT" });
    await loadConversations(false);
  } catch (error) {
    showNotice(error.message);
  }
}

async function loadConversations(preserveChat = true) {
  try {
    conversations = await api("/messages/conversations") || [];
    renderConversations();
    if (preserveChat && activeConversationId && conversations.some(c => Number(c.id) === activeConversationId)) {
      const current = conversations.find(c => Number(c.id) === activeConversationId);
      showConversation(current);
    }
  } catch (error) {
    showNotice(error.message);
  }
}

async function refreshActiveMessages() {
  if (!activeConversationId || document.hidden) return;
  try {
    const messages = await api(`/messages/conversations/${activeConversationId}/messages`);
    renderMessages(Array.isArray(messages) ? messages : []);
    await api(`/messages/conversations/${activeConversationId}/read`, { method: "PUT" });
    await loadConversations(false);
  } catch (error) {
    showNotice(error.message);
  }
}

document.getElementById("sendMessageForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const input = document.getElementById("messageInput");
  const button = document.getElementById("sendMessageButton");
  const content = input.value.trim();
  if (!content || !activeConversationId) return;
  button.disabled = true;
  try {
    await api(`/messages/conversations/${activeConversationId}/messages`, {
      method: "POST",
      body: JSON.stringify({ content })
    });
    input.value = "";
    await refreshActiveMessages();
    showNotice("");
  } catch (error) {
    showNotice(error.message);
  } finally {
    button.disabled = false;
    input.focus();
  }
});

document.getElementById("refreshConversations").addEventListener("click", () => loadConversations());
document.getElementById("signOutButton").addEventListener("click", () => {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  localStorage.removeItem("loginAs");
  window.location.href = "index.html";
});

async function init() {
  if (!token || !storedUser?.id || !["BUYER", "SELLER"].includes(loginAs)) {
    window.location.href = "index.html";
    return;
  }
  document.getElementById("currentUserLabel").textContent = storedUser.fullName || loginAs;
  const params = new URLSearchParams(window.location.search);
  const listingId = Number(params.get("listingId"));
  if (listingId > 0 && loginAs === "BUYER") {
    try {
      const conversation = await api("/messages/conversations", { method: "GET" });
      conversations = Array.isArray(conversation) ? conversation : [];
      const existing = conversations.find(c => Number(c.listingId) === listingId);
      if (existing) {
        activeConversationId = Number(existing.id);
      } else {
        const created = await api("/messages/conversations", {
          method: "POST",
          body: JSON.stringify({ listingId })
        });
        activeConversationId = Number(created.id);
      }
    } catch (error) {
      showNotice(error.message);
    }
  }
  await loadConversations(false);
  if (activeConversationId) await openConversation(activeConversationId);
  clearInterval(pollTimer);
  pollTimer = setInterval(async () => {
    await loadConversations(false);
    await refreshActiveMessages();
  }, 5000);
}

init();