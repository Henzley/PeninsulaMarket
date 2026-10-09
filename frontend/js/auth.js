const API_BASE = `${window.location.protocol}//${window.location.hostname}:8080/api`;

let currentMode = "login";
let currentRole = "buyer";

const roleDescriptions = {
  buyer: "Find campus deals & chat via WhatsApp",
  seller: "List products & receive WhatsApp leads",
  admin: "Moderate listings & campus security"
};

function switchMode(mode) {
  currentMode = mode;
  document.getElementById("tabLogin").classList.toggle("active", mode === "login");
  document.getElementById("tabRegister").classList.toggle("active", mode === "register");

  const title = document.getElementById("formTitle");
  const subtitle = document.getElementById("formSubtitle");
  const roleLabel = document.getElementById("roleLabel");

  if (mode === "login") {
    title.textContent = "Welcome back";
    subtitle.textContent = "Log in to explore campus deals or manage your listings.";
    roleLabel.textContent = "Select Sign In Mode";
  } else {
    title.textContent = "Create your account";
    subtitle.textContent = "Join the verified campus marketplace.";
    roleLabel.textContent = "Register As";
  }

  clearAlert();
  updateFormFields();
}

function selectRole(role) {
  currentRole = role;
  document.querySelectorAll(".role-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.role === role);
  });
  document.getElementById("roleDescription").textContent = roleDescriptions[role];
  clearAlert();
  updateFormFields();
}

function updateFormFields() {
  const isRegister = currentMode === "register";

  document.getElementById("groupFullName").classList.toggle("hidden", !isRegister);
  document.getElementById("groupConfirmPassword").classList.toggle("hidden", !isRegister);

  const showAdminKey = isRegister && currentRole === "admin";
  document.getElementById("groupAdminKey").classList.toggle("hidden", !showAdminKey);

  const actionText = isRegister ? "Register" : "Sign In";
  document.getElementById("submitText").textContent = `${actionText} as ${currentRole.toUpperCase()}`;
}

async function handleAuth(event) {
  event.preventDefault();
  clearAlert();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const submitBtn = document.getElementById("submitBtn");

  submitBtn.disabled = true;
  submitBtn.style.opacity = "0.7";

  try {
    if (currentMode === "register") {
      const fullName = document.getElementById("fullName").value.trim();
      const confirmPassword = document.getElementById("confirmPassword").value;

      if (!fullName) throw new Error("Full name is required.");
      if (password !== confirmPassword) throw new Error("Passwords do not match.");

      let url = `${API_BASE}/auth/register`;
      const headers = { "Content-Type": "application/json" };

      if (currentRole === "admin") {
        const secretKey = document.getElementById("adminSecretKey").value.trim();
        if (!secretKey) throw new Error("Admin passcode is required.");
        url = `${API_BASE}/auth/register-admin`;
        headers["X-Admin-Registration-Key"] = secretKey;
      }

      const response = await fetch(url, {
        method: "POST",
        headers,
        body: JSON.stringify({ fullName, email, password })
      });

      const responseText = await response.text();
      let data = null;
      if (responseText) {
        try {
          data = JSON.parse(responseText);
        } catch {
          data = null;
        }
      }
      if (!response.ok) {
        throw new Error(data?.error || responseText || `Registration failed (HTTP ${response.status}).`);
      }

      showAlert("Account created successfully! Please sign in.", "success");
      setTimeout(() => switchMode("login"), 1200);

    } else {
      const payload = { email, password };

      if (currentRole !== "admin") {
        payload.loginAs = currentRole.toUpperCase();
      }

      const response = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      const responseText = await response.text();
      let data = null;
      if (responseText) {
        try {
          data = JSON.parse(responseText);
        } catch {
          data = null;
        }
      }
      if (!response.ok) {
        throw new Error(data?.error || responseText || `Invalid email or password (HTTP ${response.status}).`);
      }

      localStorage.setItem("token", data.token);
      localStorage.setItem("loginAs", data.loginAs);
      localStorage.setItem("user", JSON.stringify(data.user));

      showAlert(`Authenticated as ${data.loginAs}! Redirecting...`, "success");

      setTimeout(() => {
        if (data.loginAs === "ADMIN") {
          window.location.href = "admin.html";
        } else if (data.loginAs === "SELLER") {
          window.location.href = "seller.html";
        } else {
          window.location.href = "marketplace.html";
        }
      }, 800);
    }
  } catch (err) {
    showAlert(err.message, "error");
  } finally {
    submitBtn.disabled = false;
    submitBtn.style.opacity = "1";
  }
}

function showAlert(message, type) {
  const box = document.getElementById("authAlert");
  box.textContent = message;
  box.className = `alert-box ${type}`;
}

function clearAlert() {
  const box = document.getElementById("authAlert");
  box.className = "alert-box hidden";
  box.textContent = "";
}

updateFormFields();