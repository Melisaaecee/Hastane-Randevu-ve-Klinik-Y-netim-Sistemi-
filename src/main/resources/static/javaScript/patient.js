// Sayfa yüklendiğinde çalışacak ana kurulum
document.addEventListener('DOMContentLoaded', () => {
    window.initializePatientPage();
});

// Sayfayı hazırlayan fonksiyon
window.initializePatientPage = function () {
    const user = JSON.parse(localStorage.getItem('user'));

    if (!user) {
        window.location.href = 'index.html';
        return;
    }

    // Bilgileri ekrana bas
    const firstName = user.firstName || "İsimsiz";
    const lastName = user.lastName || "";

    document.getElementById('patientFullName').textContent = `${firstName} ${lastName}`;
    document.getElementById('welcomeName').textContent = firstName;
    document.getElementById('avatarInitial').textContent = firstName.charAt(0).toUpperCase();
    document.getElementById('patientBlood').textContent = user.bloodType || "Belirtilmedi";

    // Yaş hesaplama işlemini window üzerinden çağır
    document.getElementById('patientAge').textContent = window.calculateAge(user.birthDate);
};

// Yaş hesaplama işlemini global yapalım
window.calculateAge = function (birthDateString) {
    if (!birthDateString) return "??";
    const today = new Date();
    const birthDate = new Date(birthDateString);
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
};

// Çıkış yapma işlemini global yapalım (HTML'deki onclick="window.logout()" için)
window.logout = function () {
    if (confirm("Sistemden çıkış yapmak üzeresiniz. Emin misiniz?")) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
};

// Yeni randevu açma işlemini global yapalım
window.openNewAppointment = function () {
    alert("Yeni randevu alma modülü yakında eklenecek!");
    // Buraya modal açma kodları gelecek
};