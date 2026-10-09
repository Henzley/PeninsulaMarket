const API_BASE = `${window.location.protocol}//${window.location.hostname}:8080/api`;
const adminToken = localStorage.getItem("token");
const adminLoginAs = localStorage.getItem("loginAs");
let adminUser;

try {
  adminUser = JSON.parse(localStorage.getItem("user") || "null");
} catch {
  adminUser = null;
}

if (!adminToken || adminLoginAs !== "ADMIN" || adminUser?.role !== "ADMIN") {
  window.location.replace("index.html");
}

const pageNotice = document.getElementById("pageNotice");

function showNotice(message, type = "error") {
  pageNotice.textContent = message;
  pageNotice.className = `notice ${type === "success" ? "success" : ""}`;
}

function clearNotice() {
  pageNotice.textContent = "";
  pageNotice.className = "notice hidden";
}

async function adminFetch(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${adminToken}`,
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers
    }
  });

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message = data && typeof data === "object" ? data.error : null;
    throw new Error(message || `Request failed (${response.status}).`);
  }

  return data;
}

function createElement(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function formatPrice(price) {
  const amount = Number(price);
  return Number.isFinite(amount)
    ? `R ${amount.toLocaleString("en-ZA", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    : "Price unavailable";
}

function renderStats(stats) {
  document.getElementById("metricUsers").textContent = stats.totalUsers ?? 0;
  document.getElementById("metricTotal").textContent = stats.totalListings ?? 0;
  document.getElementById("metricPending").textContent = stats.pendingListings ?? 0;
  document.getElementById("metricApproved").textContent = stats.approvedListings ?? 0;
  document.getElementById("metricRejected").textContent = stats.rejectedListings ?? 0;
}

let listings = [];
let listingFilter = "PENDING";

function createListingCard(listing) {
  const card = createElement("article", "listing-card");
  const imageContainer = createElement("div", "listing-image");
  const showImagePlaceholder = () => {
    const placeholder = createElement("i", "fa-regular fa-image");
    placeholder.setAttribute("aria-hidden", "true");
    imageContainer.replaceChildren(placeholder);
  };
  const imageUrl = Array.isArray(listing.imageUrls) ? listing.imageUrls[0] : null;

  if (imageUrl) {
    const image = document.createElement("img");
    image.alt = listing.title ? `${listing.title} listing` : "Listing image";
    image.loading = "lazy";
    image.addEventListener("error", showImagePlaceholder, { once: true });
    image.src = imageUrl;
    imageContainer.append(image);
  } else {
    showImagePlaceholder();
  }

  const details = createElement("div", "listing-details");
  const title = createElement("h3", "listing-title", listing.title || "Untitled listing");
  const price = createElement("p", "listing-price", formatPrice(listing.price));
  const description = createElement("p", "listing-description", listing.description || "No description provided.");
  const meta = createElement("div", "listing-meta");
  const sellerName = listing.seller?.fullName || "Unknown seller";
  const sellerEmail = listing.seller?.email || "Email unavailable";
  const location = listing.location || "Location not provided";
  const contact = listing.contactInfo || "Contact not provided";

  for (const [icon, text] of [
    ["fa-user", `${sellerName} · ${sellerEmail}`],
    ["fa-location-dot", location],
    ["fa-phone", contact]
  ]) {
    const item = createElement("span");
    const itemIcon = createElement("i", `fa-solid ${icon}`);
    itemIcon.setAttribute("aria-hidden", "true");
    item.append(itemIcon, document.createTextNode(text));
    meta.append(item);
  }

  details.append(title, price, description, meta);

  const actions = createElement("div", "listing-actions");
  const status = String(listing.status || "PENDING").toUpperCase();
  if (status === "PENDING") {
    const approve = createElement("button", "button button-approve", "Approve");
    approve.type = "button";
    approve.dataset.listingAction = "approve";
    approve.dataset.listingId = listing.id;
    const approveIcon = createElement("i", "fa-solid fa-check");
    approveIcon.setAttribute("aria-hidden", "true");
    approve.prepend(approveIcon);
    const reject = createElement("button", "button button-reject", "Reject");
    reject.type = "button";
    reject.dataset.listingAction = "reject";
    reject.dataset.listingId = listing.id;
    const rejectIcon = createElement("i", "fa-solid fa-xmark");
    rejectIcon.setAttribute("aria-hidden", "true");
    reject.prepend(rejectIcon);
    actions.append(approve, reject);
  } else {
    actions.append(createElement("span", `status-badge status-${status.toLowerCase()}`, status));
  }

  card.append(imageContainer, details, actions);
  return card;
}

function renderListings() {
  const queue = document.getElementById("listingQueue");
  const visibleListings = listingFilter === "PENDING"
    ? listings.filter((listing) => listing.status === "PENDING")
    : listings;

  queue.replaceChildren();
  if (visibleListings.length === 0) {
    queue.append(createElement("p", "empty-state",
      listingFilter === "PENDING" ? "No listings are waiting for review." : "There are no listings yet."));
    return;
  }

  visibleListings.forEach((listing) => queue.append(createListingCard(listing)));
}

async function loadDashboard() {
  clearNotice();
  const results = await Promise.allSettled([
    adminFetch("/admin/dashboard"),
    adminFetch("/admin/listings")
  ]);
  const errors = [];

  if (results[0].status === "fulfilled") {
    renderStats(results[0].value);
  } else {
    errors.push(`Could not load dashboard statistics: ${results[0].reason.message}`);
  }

  if (results[1].status === "fulfilled") {
    listings = results[1].value;
    renderListings();
  } else {
    errors.push(`Could not load listings: ${results[1].reason.message}`);
    const queue = document.getElementById("listingQueue");
    queue.replaceChildren(createElement("p", "empty-state", errors[errors.length - 1]));
  }

  if (errors.length) showNotice(errors.join(" "));
  return errors.length === 0;
}

async function moderateListing(button) {
  const { listingAction, listingId } = button.dataset;
  if (!listingAction || !listingId) return;

  const verb = listingAction === "approve" ? "approve" : "reject";
  if (!window.confirm(`Are you sure you want to ${verb} this listing?`)) return;

  const card = button.closest(".listing-card");
  card?.querySelectorAll("button").forEach((actionButton) => { actionButton.disabled = true; });
  clearNotice();

  try {
    await adminFetch(`/listings/${encodeURIComponent(listingId)}/${listingAction}`, { method: "PUT" });
    const refreshed = await loadDashboard();
    if (refreshed) showNotice(`Listing ${listingAction === "approve" ? "approved" : "rejected"}.`, "success");
  } catch (error) {
    showNotice(error.message);
    card?.querySelectorAll("button").forEach((actionButton) => { actionButton.disabled = false; });
  }
}

function setupDashboard() {
  document.getElementById("refreshDashboard").addEventListener("click", loadDashboard);
  document.querySelectorAll("[data-listing-filter]").forEach((button) => {
    button.addEventListener("click", () => {
      listingFilter = button.dataset.listingFilter;
      document.querySelectorAll("[data-listing-filter]").forEach((tab) => {
        tab.classList.toggle("active", tab === button);
      });
      renderListings();
    });
  });
  document.getElementById("listingQueue").addEventListener("click", (event) => {
    const button = event.target.closest("[data-listing-action]");
    if (button) moderateListing(button);
  });
  loadDashboard();
}

let users = [];

function renderUsers() {
  const body = document.getElementById("usersTableBody");
  const query = document.getElementById("userSearch").value.trim().toLowerCase();
  const displayed = users.filter((user) =>
    `${user.fullName || ""} ${user.email || ""}`.toLowerCase().includes(query));

  document.getElementById("userCount").textContent = displayed.length;
  body.replaceChildren();
  if (displayed.length === 0) {
    const row = document.createElement("tr");
    const cell = createElement("td", "empty-state", users.length ? "No users match your search." : "No user accounts found.");
    cell.colSpan = 5;
    row.append(cell);
    body.append(row);
    return;
  }

  displayed.forEach((user) => {
    const row = document.createElement("tr");
    const name = createElement("td", "", user.fullName || "Name unavailable");
    const email = createElement("td", "", user.email || "Email unavailable");
    const roleCell = document.createElement("td");
    const role = String(user.role || "STUDENT").toUpperCase();
    roleCell.append(createElement("span", `role-badge role-${role.toLowerCase()}`, role));
    const id = createElement("td", "", `#${user.id}`);
    const actions = document.createElement("td");
    const deleteButton = createElement("button", "button button-delete", "Delete");
    deleteButton.type = "button";
    deleteButton.dataset.deleteUser = user.id;
    deleteButton.disabled = String(user.id) === String(adminUser.id);
    deleteButton.title = deleteButton.disabled ? "You cannot delete your own admin account." : "Delete this user";
    const icon = createElement("i", "fa-regular fa-trash-can");
    icon.setAttribute("aria-hidden", "true");
    deleteButton.prepend(icon);
    actions.append(deleteButton);
    row.append(name, email, roleCell, id, actions);
    body.append(row);
  });
}

async function loadUsers() {
  clearNotice();
  const body = document.getElementById("usersTableBody");
  body.replaceChildren();
  const loadingRow = document.createElement("tr");
  const loadingCell = createElement("td", "empty-state", "Loading users…");
  loadingCell.colSpan = 5;
  loadingRow.append(loadingCell);
  body.append(loadingRow);

  try {
    users = await adminFetch("/admin/users");
    renderUsers();
  } catch (error) {
    body.replaceChildren();
    const errorRow = document.createElement("tr");
    const errorCell = createElement("td", "empty-state", error.message);
    errorCell.colSpan = 5;
    errorRow.append(errorCell);
    body.append(errorRow);
    showNotice(`Could not load users: ${error.message}`);
  }
}

async function deleteUser(button) {
  const userId = button.dataset.deleteUser;
  const user = users.find((entry) => String(entry.id) === String(userId));
  if (!user || !window.confirm(`Delete the account for ${user.fullName || user.email || `#${userId}`}?`)) return;

  button.disabled = true;
  clearNotice();
  try {
    await adminFetch(`/admin/users/${encodeURIComponent(userId)}`, { method: "DELETE" });
    users = users.filter((entry) => String(entry.id) !== String(userId));
    renderUsers();
    showNotice("User account deleted.", "success");
  } catch (error) {
    button.disabled = false;
    showNotice(`Could not delete user: ${error.message}`);
  }
}

function setupUsers() {
  document.getElementById("refreshUsers").addEventListener("click", loadUsers);
  document.getElementById("userSearch").addEventListener("input", renderUsers);
  document.getElementById("createUserForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const submitButton = document.getElementById("createUserButton");
    const formData = new FormData(form);
    submitButton.disabled = true;
    clearNotice();

    try {
      const createdUser = await adminFetch("/admin/users", {
        method: "POST",
        body: JSON.stringify({
          fullName: formData.get("fullName"),
          email: formData.get("email"),
          password: formData.get("password")
        })
      });
      users.push(createdUser);
      users.sort((first, second) => Number(first.id) - Number(second.id));
      form.reset();
      document.getElementById("userSearch").value = "";
      renderUsers();
      showNotice("Student account added to the database.", "success");
    } catch (error) {
      showNotice(`Could not add user: ${error.message}`);
    } finally {
      submitButton.disabled = false;
    }
  });
  document.getElementById("usersTableBody").addEventListener("click", (event) => {
    const button = event.target.closest("[data-delete-user]");
    if (button) deleteUser(button);
  });
  loadUsers();
}

document.querySelectorAll("[data-logout]").forEach((button) => {
  button.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("loginAs");
    localStorage.removeItem("user");
    window.location.replace("index.html");
  });
});

if (adminToken && adminLoginAs === "ADMIN" && adminUser?.role === "ADMIN") {
  if (document.body.dataset.adminPage === "dashboard") setupDashboard();
  if (document.body.dataset.adminPage === "users") setupUsers();
}
