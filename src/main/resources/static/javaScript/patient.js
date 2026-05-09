// 1. Önce Fonksiyonları Tanımla (Dosya yüklenir yüklenmez hazır olsunlar)
window.showSection = function (sectionId) {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    const target = document.getElementById(sectionId);
    const link = document.getElementById('link-' + sectionId);

    if (target) target.classList.add('active');
    if (link) link.classList.add('active');
};

window.sendEmailVerification = async function () {
    const email = document.getElementById('newEmail').value;
    const authData = JSON.parse(localStorage.getItem('user'));
    const tckn = authData.user.tckn;

    try {
        const response = await fetch('http://localhost:8080/api/auth/send-verification-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email,
                tckn: tckn
            })
        });

        if (response.ok) {
            alert("Doğrulama kodu mail adresinize gönderildi.");
            document.getElementById('emailStep1').style.display = 'none';
            document.getElementById('emailStep2').style.display = 'block';
        }
    } catch (error) {
        alert("Hata oluştu!");
    }
};

window.confirmEmailUpdate = async function () {
    const code = document.getElementById('emailVerifyCode').value;
    const newEmail = document.getElementById('newEmail').value;
    const authData = JSON.parse(localStorage.getItem('user'));

    // TCKN bilgisini localStorage'dan alıyoruz (Backend'e kim olduğumuzu söylemek için)
    const tckn = authData.user.tckn;

    try {
        const response = await fetch('http://localhost:8080/api/auth/verify-email-update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                code: code,
                newEmail: newEmail,
                tckn: tckn
            })
        });

        if (response.ok) {
            alert("E-posta veritabanında başarıyla güncellendi!");

            // LocalStorage'ı da güncelle ki sayfa yenilenince yeni mail kalsın
            authData.user.email = newEmail;
            localStorage.setItem('user', JSON.stringify(authData));

            location.reload(); // Bilgileri tazelemek için sayfayı yenile
        } else {
            const error = await response.json();
            alert("Hata: " + error.message);
        }
    } catch (error) {
        alert("Bağlantı hatası! Backend'in çalıştığından emin olun.");
    }
};

window.handlePasswordUpdate = async function () {
    const cpEl = document.getElementById('currentPassword');
    const npEl = document.getElementById('newPassword');
    const confEl = document.getElementById('confirmPassword');

    // Güvenlik kontrolü: Inputlar HTML'de var mı?
    if (!cpEl || !npEl || !confEl) {
        alert("Sistem hatası: Şifre alanları bulunamadı.");
        return;
    }

    const currentPassword = cpEl.value;
    const newPassword = npEl.value;
    const confirmPassword = confEl.value;

    // 1. Boş alan kontrolü
    if (!currentPassword || !newPassword || !confirmPassword) {
        alert("Lütfen tüm alanları doldurun!");
        return;
    }

    // 2. Şifre eşleşme kontrolü
    if (newPassword !== confirmPassword) {
        alert("Yeni şifreler birbiriyle eşleşmiyor!");
        return;
    }

    // 3. LocalStorage'dan kullanıcı verisini al
    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.user) {
        alert("Oturum bilgisi bulunamadı! Giriş sayfasına yönlendiriliyorsunuz.");
        window.location.href = 'index.html';
        return;
    }

    const tckn = authData.user.tckn;

    try {
        const response = await fetch('http://localhost:8080/api/auth/reset-password-logged-in', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                tckn: tckn,
                currentPassword: currentPassword,
                newPassword: newPassword
            })
        });

        if (response.ok) {
            // BAŞARILI DURUM
            alert("Şifreniz başarıyla güncellendi! Güvenliğiniz için tekrar giriş yapmalısınız.");

            // Oturumu temizle
            localStorage.clear();

            // Giriş sayfasına yönlendir
            window.location.href = 'index.html';

        } else {
            // HATA DURUMU (Backend'den gelen hata mesajı)
            const errorData = await response.json();
            alert("Hata: " + (errorData.message || "İşlem başarısız!"));
        }
    } catch (error) {
        console.error("Güncelleme hatası:", error);
        alert("Sunucuyla iletişim kurulamadı. Lütfen internet bağlantınızı ve backend'i kontrol edin.");
    }
};

window.logout = function () {
    if (confirm("Çıkış yapmak istiyor musunuz?")) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
};

// 2. En Son DOM Yüklendiğinde Bilgileri Doldur
document.addEventListener('DOMContentLoaded', () => {
    // LocalStorage'dan gelen ham paket (İçinde token ve user var)
    const authData = JSON.parse(localStorage.getItem('user'));

    if (!authData || !authData.user) {
        window.location.href = 'index.html';
        return;
    }

    // Gerçek kullanıcı verisi authData.user içinde!
    const user = authData.user;

    // Sidebar Bilgileri (Melisa Ece olarak görünecek)
    const fullName = `${user.firstName} ${user.lastName}`;
    const nameElements = [document.getElementById('patientFullName'), document.getElementById('infoName')];

    nameElements.forEach(el => {
        if (el) el.textContent = fullName;
    });

    if (document.getElementById('avatarInitial')) {
        document.getElementById('avatarInitial').textContent = user.firstName.trim().charAt(0).toUpperCase();
    }

    // Diğer Bilgiler
    const fields = {
        'infoTckn': user.tckn,
        'infoEmail': user.email,
        'infoBlood': user.bloodGroup || "Belirtilmedi",
        'infoAge': user.age || "Belirtilmedi"
    };

    for (const [id, value] of Object.entries(fields)) {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    }

    console.log("Dashboard başarıyla yüklendi: ", user.firstName);
});