document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", handleLogin);
    }
});

window.switchTab = function (role) {
    const label = document.querySelector('label[for="tckn"]');
    const input = document.getElementById('tckn');
    const patientBtn = document.getElementById("btnPatient");
    const staffBtn = document.getElementById("btnStaff");

    if (role === "STAFF") {
        label.innerText = "Kullanıcı Adı / TCKN";
        input.placeholder = "Doktor/Admin kullanıcı adı";
        staffBtn.classList.add("active");
        patientBtn.classList.remove("active");
    } else {
        label.innerText = "TC Kimlik Numarası";
        input.placeholder = "11 haneli TCKN";
        patientBtn.classList.add("active");
        staffBtn.classList.remove("active");
    }
};

window.handleLogin = async function (e) {
    e.preventDefault();

    const tckn = document.getElementById("tckn").value;
    const password = document.getElementById("password").value;
    const errorBox = document.getElementById("errorBox");

    if (errorBox) errorBox.style.display = "none";

    try {
        const res = await fetch("https://medsoft.up.railway.app/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ tckn, password })
        });

        const data = await res.json();

        if (!res.ok) {
            showError(data.message || "Giriş bilgileri hatalı!");
            return;
        }

        localStorage.setItem("user", JSON.stringify(data));

        const rawRole = data.user?.role || data.role || "";
        const role = rawRole.replace("ROLE_", "");

        if (role === "ADMIN") location.href = "admin.html";
        else if (role === "DOCTOR") location.href = "doctor-dashboard.html";
        else if (role === "PATIENT") location.href = "patient.html";
        else showError("Bilinmeyen Yetki!");

    } catch (err) {
        showError("Sunucu bağlantı hatası!");
    }
};

window.openForgotModal = function () {
    document.getElementById("forgotModal").style.display = "flex";
};

window.closeForgotModal = function () {
    document.getElementById("forgotModal").style.display = "none";
};

window.showError = function (msg) {
    const box = document.getElementById("errorBox");
    if (box) {
        box.style.display = "block";
        box.textContent = msg;
    } else {
        alert(msg);
    }
};

window.sendResetLink = async function () {
    const email = document.getElementById("forgotEmail").value;
    const msg = document.getElementById("forgotMessage");

    if (!email.includes("@")) {
        msg.style.color = "#991b1b";
        msg.textContent = "Geçerli bir email giriniz.";
        msg.style.display = "block";
        return;
    }

    msg.style.display = "block";
    msg.style.color = "#1e40af";
    msg.textContent = "Gönderiliyor...";

    try {
        const res = await fetch("https://medsoft.up.railway.app/api/auth/forgot-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        if (res.ok) {
            msg.style.color = "#15803d";
            msg.textContent = "Sıfırlama bağlantısı gönderildi.";
        } else {
            msg.style.color = "#991b1b";
            msg.textContent = "E-posta bulunamadı.";
        }
    } catch (e) {
        msg.style.color = "#991b1b";
        msg.textContent = "Sistem hatası.";
    }
};