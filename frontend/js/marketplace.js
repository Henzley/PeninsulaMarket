const MARKET_API = `${window.location.protocol}//${window.location.hostname}:8080/api`;
const grid = document.getElementById("productGrid");
const searchInput = document.getElementById("listingSearch");
const resultsCount = document.getElementById("resultsCount");
let listings = [];
let activeCategory = "all";

function classifyListing(listing) {
  const text = `${listing.title || ""} ${listing.description || ""}`.toLowerCase();
  if (/textbook|book|notes|edition|novel/.test(text)) return "books";
  if (/laptop|phone|tablet|electronic|headphone|charger|computer|calculator/.test(text)) return "electronics";
  if (/study|stationery|pen|backpack|gear|uniform|lab coat/.test(text)) return "study";
  return "other";
}

function categoryLabel(category) {
  return {
    books: "Books & notes",
    electronics: "Electronics",
    study: "Study gear",
    other: "Campus find"
  }[category] || "Campus find";
}

function formatListingPrice(value) {
  const price = Number(value);
  return Number.isFinite(price)
    ? `R ${price.toLocaleString("en-ZA", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    : "Price on request";
}

function normalizePhone(value) {
  let digits = String(value || "").replace(/\D/g, "");
  if (digits.startsWith("0")) digits = `27${digits.slice(1)}`;
  return digits.length >= 9 && digits.length <= 15 ? digits : "";
}

function makeProductCard(listing) {
  const card = document.createElement("article");
  card.className = "product-card";
  const imageBox = document.createElement("div");
  imageBox.className = "product-image";

  const imageUrl = Array.isArray(listing.imageUrls) ? listing.imageUrls[0] : "";
  if (imageUrl) {
    const image = document.createElement("img");
    image.src = imageUrl;
    image.alt = listing.title ? `${listing.title} listing photo` : "Listing photo";
    image.loading = "lazy";
    image.addEventListener("error", () => {
      const placeholder = document.createElement("i");
      placeholder.className = "fa-regular fa-image";
      placeholder.setAttribute("aria-hidden", "true");
      imageBox.replaceChildren(placeholder);
    }, { once: true });
    imageBox.append(image);
  } else {
    const placeholder = document.createElement("i");
    placeholder.className = "fa-regular fa-image";
    placeholder.setAttribute("aria-hidden", "true");
    imageBox.append(placeholder);
  }

  const category = classifyListing(listing);
  const categoryChip = document.createElement("span");
  categoryChip.className = "product-category";
  categoryChip.textContent = categoryLabel(category);
  imageBox.append(categoryChip);

  const content = document.createElement("div");
  content.className = "product-body";
  const price = document.createElement("p");
  price.className = "product-price";
  price.textContent = formatListingPrice(listing.price);
  const title = document.createElement("h3");
  title.className = "product-title";
  title.textContent = listing.title || "Untitled listing";
  const description = document.createElement("p");
  description.className = "product-description";
  description.textContent = listing.description || "Contact the seller to find out more.";
  const meta = document.createElement("div");
  meta.className = "product-meta";

  const seller = document.createElement("span");
  const sellerIcon = document.createElement("i");
  sellerIcon.className = "fa-regular fa-user";
  sellerIcon.setAttribute("aria-hidden", "true");
  seller.append(sellerIcon, document.createTextNode(listing.seller?.fullName || "Student seller"));

  const location = document.createElement("span");
  const locationIcon = document.createElement("i");
  locationIcon.className = "fa-solid fa-location-dot";
  locationIcon.setAttribute("aria-hidden", "true");
  location.append(locationIcon, document.createTextNode(listing.location || "Campus"));
  meta.append(seller, location);

  const token = localStorage.getItem("token");
  const loginAs = (localStorage.getItem("loginAs") || "").toUpperCase();
  const contact = document.createElement("a");
  contact.className = "product-contact";
  if (token && loginAs === "BUYER" && listing.seller?.id && Number(listing.seller.id) !== Number(JSON.parse(localStorage.getItem("user") || "{}").id)) {
    contact.href = `messages.html?listingId=${encodeURIComponent(listing.id)}`;
    const messageIcon = document.createElement("i");
    messageIcon.className = "fa-regular fa-comments";
    messageIcon.setAttribute("aria-hidden", "true");
    contact.append(messageIcon, document.createTextNode("Message seller"));
  } else if (!token) {
    contact.href = "index.html";
    const messageIcon = document.createElement("i");
    messageIcon.className = "fa-regular fa-comments";
    messageIcon.setAttribute("aria-hidden", "true");
    contact.append(messageIcon, document.createTextNode("Sign in to message"));
  } else {
    contact.href = "messages.html";
    const messageIcon = document.createElement("i");
    messageIcon.className = "fa-regular fa-comments";
    messageIcon.setAttribute("aria-hidden", "true");
    contact.append(messageIcon, document.createTextNode(loginAs === "SELLER" ? "Open messages" : "Message seller"));
  }
  content.append(price, title, description, meta, contact);
  card.append(imageBox, content);
  return { card, category, searchable: `${listing.title || ""} ${listing.description || ""} ${listing.location || ""} ${listing.seller?.fullName || ""}`.toLowerCase() };
}

function renderListings() {
  const query = searchInput.value.trim().toLowerCase();
  const matching = listings.map(makeProductCard).filter((item, index) => {
    const listing = listings[index];
    const categoryMatches = activeCategory === "all" || item.category === activeCategory;
    const queryMatches = !query || item.searchable.includes(query)
      || String(listing.price || "").includes(query);
    return categoryMatches && queryMatches;
  });

  resultsCount.textContent = `${matching.length} ${matching.length === 1 ? "listing" : "listings"}`;
  grid.replaceChildren();
  if (!matching.length) {
    const message = document.createElement("p");
    message.className = "market-message";
    message.textContent = listings.length
      ? "No listings match those filters. Try a different search."
      : "No approved listings yet. Check back soon, or be the first to list an item.";
    grid.append(message);
    return;
  }
  matching.forEach((item) => grid.append(item.card));
}

async function loadListings() {
  const message = document.createElement("p");
  message.className = "market-message";
  message.textContent = "Loading approved listings…";
  grid.replaceChildren(message);
  resultsCount.textContent = "";

  try {
    const response = await fetch(`${MARKET_API}/listings`);
    const responseText = await response.text();
    let data;
    if (responseText) {
      try {
        data = JSON.parse(responseText);
      } catch {
        throw new Error(`The server returned an unreadable response (HTTP ${response.status}).`);
      }
    }
    if (!response.ok) {
      throw new Error(data?.error || `Could not load listings (HTTP ${response.status}).`);
    }
    listings = Array.isArray(data) ? data : [];
    renderListings();
  } catch (error) {
    const failure = document.createElement("p");
    failure.className = "market-message";
    failure.textContent = `${error.message} Check that the Student Marketplace server is running.`;
    grid.replaceChildren(failure);
  }
}

document.querySelectorAll("[data-category]").forEach((button) => {
  button.addEventListener("click", () => {
    activeCategory = button.dataset.category;
    document.querySelectorAll("[data-category]").forEach((filter) => {
      filter.classList.toggle("active", filter === button);
    });
    renderListings();
  });
});

searchInput.addEventListener("input", renderListings);
document.getElementById("clearSearch").addEventListener("click", () => {
  searchInput.value = "";
  renderListings();
  searchInput.focus();
});
document.getElementById("refreshListings").addEventListener("click", loadListings);
loadListings();
