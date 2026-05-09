// 1. TEMEL NAVİGASYON VE GÖRÜNÜM FONKSİYONLARI
window.showSection = function (sectionId) {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    const target = document.getElementById(sectionId);
    const link = document.getElementById('link-' + sectionId);

    if (target) target.classList.add('active');
    if (link) link.classList.add('active');

    // Randevularım sekmesine geçildiyse verileri tazele
    if (sectionId === 'appointments') {
        window.filterAppointments('all');
    }
};

// 2. RANDEVU FİLTRELEME VE BACKEND ENTEGRASYONU
window.filterAppointments = async function (type) {
    // Butonların görsel durumunu güncelle
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active-filter'));
    const activeBtn = document.getElementById('btn-' + type);
    if (activeBtn) activeBtn.classList.add('active-filter');

    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.token) return;

    let url = 'http://localhost:8080/api/appointments';

    // Backend'deki AppointmentController ve AppointmentService yapılarına göre yönlendirme
    if (type === 'past') {
        // getPatientPastAppointments metodunu tetikler
        url += `/patient/${authData.user.id}/past`;
    } else if (type === 'active') {
        // Aktif randevular için genel /my endpoint'ini kullanıp JS tarafında filtreleyeceğiz 
        // veya backend'e yeni bir /my/active endpoint'i ekleyebilirsin.
        url += '/my';
    } else {
        // getMyAppointments metodunu tetikler
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

        // Eğer 'active' seçildiyse frontend tarafında anlık zaman kontrolü yap
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

    // Backend'den gelen Appointment nesnesi içindeki Slot ve Doctor hiyerarşisine göre:
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
document.addEventListener('DOMContentLoaded', function () {
    const sendCodeBtn = document.getElementById('send-code-btn');
    const emailInput = document.getElementById('new-email');

    if (sendCodeBtn) {
        sendCodeBtn.addEventListener('click', async function () {
            const newEmail = emailInput.value.trim();

            if (!newEmail || !newEmail.includes('@')) {
                alert('Lütfen geçerli bir e-posta adresi giriniz.');
                return;
            }

            sendCodeBtn.disabled = true;
            sendCodeBtn.innerText = 'Gönderiliyor...';

            try {
              
                console.log("Kod gönderiliyor:", newEmail);

                // Simülasyon (Gerçek API geldiğinde burayı güncelleyeceksiniz)
                setTimeout(() => {
                    alert('Doğrulama kodu e-posta adresinize gönderildi!');
                    sendCodeBtn.disabled = false;
                    sendCodeBtn.innerText = 'Kod Gönder';
                }, 1500);

            } catch (error) {
                console.error("Hata:", error);
                alert('Bir hata oluştu, lütfen tekrar deneyin.');
                sendCodeBtn.disabled = false;
                sendCodeBtn.innerText = 'Kod Gönder';
            }
        });
    }
});

window.logout = function () {
    localStorage.clear();
    window.location.href = 'index.html';
};



// RANDEVU İPTAL ETME FONKSİYONU
window.cancelAppointment = async function (appointmentId) {
    if (!confirm("Bu randevuyu iptal etmek istediğinize emin misiniz? (24 saat kuralı geçerlidir)")) return;

    const authData = JSON.parse(localStorage.getItem('user'));

    try {
        const response = await fetch(`http://localhost:8080/api/appointments/${appointmentId}/cancel`, {
            method: 'PUT', // Controller'da @PutMapping olarak tanımlı
            headers: {
                'Authorization': 'Bearer ' + authData.token,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            alert("Randevu başarıyla iptal edildi.");
            window.filterAppointments('all'); // Tabloyu tazele
        } else {
            const errorText = await response.text();
            // Backend'den gelen "24 saat kuralı" veya "Geçmiş iptal edilemez" mesajlarını gösterir
            alert("Hata: " + errorText);
        }
    } catch (error) {
        console.error("İptal hatası:", error);
        alert("Bağlantı hatası oluştu.");
    }
};
function renderTable(data) {
    const tbody = document.getElementById('appointmentTableBody');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 20px;">Kayıt bulunamadı.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(app => {
        const appointmentDate = new Date(app.slot.startTime);
        const dateStr = appointmentDate.toLocaleString('tr-TR');
        const now = new Date();

        // Sadece gelecekteki ve 'PENDING' veya 'CONFIRMED' durumdaki randevular iptal edilebilir
        const isCancelable = appointmentDate > now && (app.status === 'PENDING' || app.status === 'CONFIRMED');

        return `
            <tr>
                <td>Dr. ${app.slot.doctor.user.firstName} ${app.slot.doctor.user.lastName}</td>
                <td>${app.slot.doctor.clinic.name}</td>
                <td>${dateStr}</td>
                <td>
                    <span class="status-badge ${appointmentDate >= now ? 'status-active' : 'status-past'}">
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