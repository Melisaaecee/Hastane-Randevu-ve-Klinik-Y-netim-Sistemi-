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
let loginAttempts = 0; // Deneme sayacı

window.handleLogin = async function(event) {
    event.preventDefault();
    
    const tckn = document.getElementById('tckn').value;
    const password = document.getElementById('password').value;
    const errorBox = document.getElementById('errorBox');

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tckn, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('user', JSON.stringify(data));
            loginAttempts = 0; // Başarılı girişte sıfırla
            window.location.href = 'patient.html';
        } else {
            loginAttempts++; // Hatalı girişte artır
            errorBox.style.display = 'block';

            if (loginAttempts === 2) {
                // 2. Denemeden sonra özel mesaj
                errorBox.innerHTML = "Hatalı şifre! <strong>Şifrenizi mi unuttunuz?</strong> Bir sonraki hatalı denemede hesabınız kilitlenecektir.";
                // Şifremi unuttum linkini görsel olarak vurgulayalım
                const forgotLink = document.querySelector('.forgot-link');
                if (forgotLink) {
                    forgotLink.style.color = "#2563eb";
                    forgotLink.style.fontWeight = "bold";
                    forgotLink.style.textDecoration = "underline";
                }
            } else if (loginAttempts >= 3) {
                // 3. deneme ve sonrası (Backend zaten kilitliyor, biz sadece mesajı gösteriyoruz)
                errorBox.innerText = "Hesabınız çok fazla hatalı deneme nedeniyle kilitlenmiştir.";
            } else {
                // 1. Deneme
                errorBox.innerText = "TCKN veya Şifre hatalı!";
            }
        }
    } catch (error) {
        errorBox.style.display = 'block';
        errorBox.innerText = "Sunucu bağlantı hatası!";
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

window.switchTab = function (type) {
    const label = document.querySelector('label[for="tckn"]');
    const input = document.getElementById('tckn');
    if (type === 'STAFF') {
        label.innerText = "Kullanıcı Adı";
        input.placeholder = "Doktor/Admin kullanıcı adı";
    } else {
        label.innerText = "TC Kimlik Numarası";
        input.placeholder = "11 haneli TCKN";
    }
}

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

// Modalı Aç
window.openForgotModal = function() {
    document.getElementById('forgotModal').style.display = 'flex';
    document.getElementById('forgotMessage').style.display = 'none';
};

// Modalı Kapat
window.closeForgotModal = function() {
    document.getElementById('forgotModal').style.display = 'none';
};

// Sıfırlama Linki Gönder
window.sendResetLink = async function() {
    const email = document.getElementById('forgotEmail').value;
    const btn = document.getElementById('btnReset');
    const msg = document.getElementById('forgotMessage');

    if (!email || !email.includes('@')) {
        alert("Lütfen geçerli bir e-posta adresi giriniz.");
        return;
    }

    try {
        btn.disabled = true;
        btn.innerText = "Gönderiliyor...";

        const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email })
        });

        if (response.ok) {
            msg.style.color = "#059669";
            msg.innerText = "Sıfırlama linki e-posta adresinize gönderildi. Lütfen gelen kutunuzu (ve spam klasörünü) kontrol edin.";
            msg.style.display = 'block';
        } else {
            const data = await response.json();
            msg.style.color = "#dc2626";
            msg.innerText = data.message || "E-posta gönderilirken bir hata oluştu.";
            msg.style.display = 'block';
        }
    } catch (error) {
        msg.style.color = "#dc2626";
        msg.innerText = "Sunucuya ulaşılamadı.";
        msg.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.innerText = "Link Gönder";
    }
};