window.showSection = function (sectionId, element) {
    // 1. ADIM: Tüm içerik bölümlerini (section) bul ve 'active' sınıfını sil (Hepsini Gizle)
    const allSections = document.querySelectorAll('.tab-content');
    allSections.forEach(s => {
        s.classList.remove('active');
        s.style.display = 'none'; 
    });


    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

   
    const target = document.getElementById(sectionId);
    if (target) {
        target.classList.add('active');
        target.style.display = 'block'; 
    }


    if (element) {
        element.classList.add('active');
    } else {
        const link = document.getElementById('link-' + sectionId);
        if (link) link.classList.add('active');
    }

   
    if (sectionId === 'appointments') {
        window.filterAppointments('all');
    }
};
// 2. RANDEVU FİLTRELEME VE BACKEND ENTEGRASYONU
window.filterAppointments = async function (type) {

    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active-filter'));
    const activeBtn = document.getElementById('btn-' + type);
    if (activeBtn) activeBtn.classList.add('active-filter');

    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.token) return;

    let url = 'http://localhost:8080/api/appointments';


    if (type === 'past') {

        url += `/patient/${authData.user.id}/past`;
    } else if (type === 'active') {

        url += '/my';
    } else {

        url += '/my';
    }

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + authData.token,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error("Veri çekilemedi");

        let data = await response.json();


        if (type === 'active') {
            const now = new Date();
            data = data.filter(app => new Date(app.slot.startTime) >= now);
        }

        renderTable(data);
    } catch (error) {
        console.error("Filtreleme hatası:", error);
        renderTable([]);
    }
};

// 3. TABLO RENDER FONKSİYONU (Backend Entity Yapısına Uygun)
function renderTable(data) {
    const tbody = document.getElementById('appointmentTableBody');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 20px;">Kayıt bulunamadı.</td></tr>';
        return;
    }


    tbody.innerHTML = data.map(app => {
        const date = new Date(app.slot.startTime).toLocaleString('tr-TR');
        const isFuture = new Date(app.slot.startTime) >= new Date();

        return `
            <tr>
                <td>Dr. ${app.slot.doctor.user.firstName} ${app.slot.doctor.user.lastName}</td>
                <td>${app.slot.doctor.clinic.name}</td>
                <td>${date}</td>
                <td>
                    <span class="status-badge ${isFuture ? 'status-active' : 'status-past'}">
                        ${app.status} 
                    </span>
                </td>
            </tr>
        `;
    }).join('');
}

// 4. ŞİFRE VE PROFİL GÜNCELLEME (Mevcut Fonksiyonların Korundu)
window.handlePasswordUpdate = async function () {
    const cpEl = document.getElementById('currentPassword');
    const npEl = document.getElementById('newPassword');
    const confEl = document.getElementById('confirmPassword');

    if (npEl.value !== confEl.value) {
        alert("Yeni şifreler eşleşmiyor!");
        return;
    }

    const authData = JSON.parse(localStorage.getItem('user'));

    try {
        const response = await fetch('http://localhost:8080/api/auth/reset-password-logged-in', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + authData.token
            },
            body: JSON.stringify({
                tckn: authData.user.tckn,
                currentPassword: cpEl.value,
                newPassword: npEl.value
            })
        });

        if (response.ok) {
            alert("Şifre güncellendi, tekrar giriş yapınız.");
            window.logout();
        } else {
            const err = await response.json();
            alert("Hata: " + err.message);
        }
    } catch (error) {
        alert("Sunucu hatası!");
    }
};

// 5. SAYFA YÜKLENDİĞİNDE ÇALIŞACAK KISIM
document.addEventListener('DOMContentLoaded', () => {
    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.user) {
        window.location.href = 'index.html';
        return;
    }

    const user = authData.user;

    // --- YENİ EKLEDİĞİMİZ FORMATLAMA YARDIMCILARI ---
    const capitalize = (str) => str ? str.toLowerCase().replace(/\b\w/g, l => l.toUpperCase()) : "";
    const formatBloodGroup = (bg) => {
        const mapping = {
            'A_POSITIVE': 'A+', 'A_NEGATIVE': 'A-', 'B_POSITIVE': 'B+', 'B_NEGATIVE': 'B-',
            'AB_POSITIVE': 'AB+', 'AB_NEGATIVE': 'AB-', 'O_POSITIVE': '0+', 'O_NEGATIVE': '0-'
        };
        return mapping[bg] || bg;
    };

    // İsimleri formatlayarak birleştir
    const fullName = capitalize(`${user.firstName} ${user.lastName}`);

    if (document.getElementById('patientFullName')) {
        document.getElementById('patientFullName').textContent = fullName;
    }
    if (document.getElementById('infoName')) {
        document.getElementById('infoName').textContent = fullName;
    }

    if (document.getElementById('avatarInitial') && user.firstName) {
        document.getElementById('avatarInitial').textContent = user.firstName.charAt(0).toUpperCase();
    }

    // --- KİŞİSEL BİLGİLERİ DOLDURMA  ---
    const fields = {
        'infoTckn': user.tckn,
        'infoEmail': user.email,
        'infoBlood': formatBloodGroup(user.bloodGroup), // A_POSITIVE -> A+ çevrimi
        'infoAge': user.age || "--"
    };

    Object.entries(fields).forEach(([id, value]) => {
        const el = document.getElementById(id);
        if (el) el.textContent = value || "--";
    });
});

window.logout = function () {
    localStorage.clear();
    window.location.href = 'index.html';
};

// DOĞRULAMA KODU GÖNDERME
window.sendEmailVerification = async function () {
    const emailInput = document.getElementById('newEmail');
    const sendCodeBtn = document.getElementById('send-code-btn');
    const newEmail = emailInput.value.trim();


    const authData = JSON.parse(localStorage.getItem('user'));

    if (!newEmail || !newEmail.includes('@')) {
        alert('Lütfen geçerli bir e-posta adresi giriniz.');
        return;
    }

    if (!authData || !authData.user.tckn) {
        alert('Oturum bilgileri bulunamadı, lütfen tekrar giriş yapın.');
        return;
    }

    sendCodeBtn.disabled = true;
    sendCodeBtn.innerText = 'Gönderiliyor...';

    try {
        const response = await fetch('http://localhost:8080/api/auth/send-verification-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + authData.token
            },
            body: JSON.stringify({
                email: newEmail,
                tckn: authData.user.tckn
            })
        });

        if (response.ok) {
            alert('Doğrulama kodu ' + newEmail + ' adresine gönderildi!');
            document.getElementById('emailStep1').style.display = 'none';
            document.getElementById('emailStep2').style.display = 'block';
        } else {
            const error = await response.json();
            alert("Hata: " + (error.message || "Kod gönderilemedi"));
        }
    } catch (error) {
        console.error("Hata:", error);
        alert('Sunucuya bağlanılamadı.');
    } finally {
        sendCodeBtn.disabled = false;
        sendCodeBtn.innerText = 'Kod Gönder';
    }
};

// 2. ADIM: KODU DOĞRULAMA VE E-POSTA GÜNCELLEME
window.confirmEmailUpdate = async function () {
    const incomingCode = document.getElementById('emailVerifyCode').value;
    const newEmail = document.getElementById('newEmail').value;
    const authData = JSON.parse(localStorage.getItem('user'));

    if (!incomingCode) {
        alert("Lütfen doğrulama kodunu giriniz.");
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/auth/verify-email-update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + authData.token
            },
            body: JSON.stringify({
                code: incomingCode,
                newEmail: newEmail,
                tckn: authData.user.tckn
            })
        });

        const data = await response.json();

        if (response.ok) {
            alert("E-posta adresiniz başarıyla güncellendi.");
            authData.user.email = newEmail;
            localStorage.setItem('user', JSON.stringify(authData));
            location.reload();
        } else {
            alert("Hata: " + (data.message || "Doğrulama başarısız."));
        }
    } catch (error) {
        console.error("Hata:", error);
        alert("Bağlantı hatası.");
    }
};
// RANDEVU İPTAL ETME FONKSİYONU
window.cancelAppointment = async function (appointmentId) {
    if (!confirm("Bu randevuyu iptal etmek istediğinize emin misiniz? (24 saat kuralı geçerlidir)")) return;

    const authData = JSON.parse(localStorage.getItem('user'));

    try {
        const response = await fetch(`http://localhost:8080/api/appointments/${appointmentId}/cancel`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + authData.token,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            alert("Randevu başarıyla iptal edildi.");
            window.filterAppointments('all');
        } else {
            const errorText = await response.text();
            alert("Hata: " + errorText);
        }
    } catch (error) {
        console.error("İptal hatası:", error);
        alert("Bağlantı hatası oluştu.");
    }
};

// RenderTable fonksiyonunu şu şekilde güncelleyin:
function renderTable(data) {
    const tbody = document.getElementById('appointmentTableBody');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 20px;">Kayıt bulunamadı.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(app => {

        const doctorName = app.slot?.doctor?.user
            ? `Dr. ${app.slot.doctor.user.firstName} ${app.slot.doctor.user.lastName}`
            : "Bilinmeyen Doktor";

        const clinicName = app.slot?.doctor?.clinic?.name || "Bilinmeyen Klinik";
        const startTime = app.slot?.startTime ? new Date(app.slot.startTime) : new Date();
        const dateStr = startTime.toLocaleString('tr-TR');
        const now = new Date();

        const isCancelable = startTime > now && (app.status === 'PENDING' || app.status === 'CONFIRMED');

        return `
            <tr>
                <td>${doctorName}</td>
                <td>${clinicName}</td>
                <td>${dateStr}</td>
                <td>
                    <span class="status-badge ${startTime >= now ? 'status-active' : 'status-past'}">
                        ${app.status} 
                    </span>
                </td>
                <td>
                    ${isCancelable ?
                `<button onclick="cancelAppointment(${app.id})" class="btn-cancel-table">
                            <i class="fas fa-times"></i> İptal Et
                         </button>` :
                `<span style="color: #cbd5e1; font-size: 0.8rem;">İşlem Yapılamaz</span>`
            }
                </td>
            </tr>
        `;
    }).join('');
}