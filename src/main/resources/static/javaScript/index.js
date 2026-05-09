// Sayfa yüklendiğinde çalışacak başlangıç ayarları
document.addEventListener('DOMContentLoaded', () => {
    // Eğer giriş yapmış bir kullanıcı varsa profil bilgilerini hemen ekrana bas
    window.updateProfileUI();

    // Formu dinlemeye başla
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', window.handleLogin);
    }
});

// Giriş İşlemi
window.handleLogin = async function (e) {
    e.preventDefault();

    const tcknInput = document.getElementById('tckn');
    const passwordInput = document.getElementById('password');
    const errorBox = document.getElementById('errorBox');

    if (errorBox) errorBox.style.display = 'none';

    // Veri toplama
    const payload = {
        tckn: tcknInput.value,
        password: passwordInput.value
    };

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok) {
            // Verileri sakla
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));

            // Yönlendir
            window.redirectByUserRole(data.user.role);
        } else {
            window.showError(data.message || "Giriş başarısız. Bilgilerinizi kontrol edin.");
        }
    } catch (err) {
        window.showError("Sunucuya bağlanılamadı. Lütfen Backend'in çalıştığından emin olun.");
    }
};

// Profil bilgilerini ekrana basan fonksiyon
window.updateProfileUI = function () {
    const rawData = localStorage.getItem('user');
    if (!rawData) return;

    try {
        const userData = JSON.parse(rawData);

        const nameDisplay = document.getElementById('userNameDisplay');
        const bloodGroupElem = document.getElementById('bloodGroup');
        const ageElem = document.getElementById('age');

        if (nameDisplay) nameDisplay.innerText = userData.firstName + " " + userData.lastName;

        // Backend'den bloodGroup ve age gelmiyorsa "Belirtilmedi" yazar
        if (bloodGroupElem) bloodGroupElem.innerText = userData.bloodGroup || "Belirtilmedi";
        if (ageElem) ageElem.innerText = userData.age || "??";
    } catch (e) {
        console.error("User verisi ayrıştırılamadı:", e);
    }
};

// Rol bazlı yönlendirme
window.redirectByUserRole = function (role) {
    if (role === 'ADMIN') {
        window.location.href = 'admin.html';
    } else if (role === 'DOCTOR') {
        window.location.href = 'doctor.html';
    } else {
        window.location.href = 'patient.html';
    }
};

// Hata gösterimi
window.showError = function (message) {
    const errorBox = document.getElementById('errorBox');
    if (errorBox) {
        errorBox.textContent = message;
        errorBox.style.display = 'block';
    } else {
        alert(message);
    }
};

// Çıkış işlemi
window.logout = function () {
    localStorage.clear();
    window.location.href = 'index.html';
};