window.doctorApp = {
    activeDoctor: null,
    currentUser: JSON.parse(localStorage.getItem('user'))?.user || null,
    
    init: async function() {


        // Giriş yapmamış veya doktor olmayan kullanıcıları engelle
        /*if (!this.currentUser || this.currentUser.role !== 'DOCTOR') {
            alert("Lütfen doktor hesabınızla giriş yapın.");
            window.location.href = 'index.html'; // Giriş yapmamışsa yönlendir
            return;
        }*/

            // Eğer localStorage'da veri yoksa sayfayı boş görmemek için burayı elle doldur:
    if (!this.currentUser) {
        this.currentUser = {
            id: 1, // Kendi veritabanındaki bir ID
            firstName: "İrem",
            lastName: "Geliştirici",
            tckn: "12345678901",
            email: "irem@medsoft.com",
            role: "DOCTOR"
        };
    }
        
        console.log("Doktor Paneli Başlatıldı...");
        const nameHeader = document.getElementById('userNameDisplay');
        if (nameHeader) nameHeader.innerText = `Dr. ${this.currentUser.firstName} ${this.currentUser.lastName}`;

        await this.fetchDoctorDetails(this.currentUser.id);
        this.fetchSlots();
        this.fetchAppointments();
        this.setupEventListeners();
    },

    fetchDoctorDetails: async function(userId) {
        try {
            const response = await fetch(`http://localhost:8080/api/doctors/user/${userId}`);
            if (!response.ok) {
                this.activeDoctor = { id: 99, specialization: "Uzmanlık Belirtilmemiş", clinic: { id: 1, name: "Genel Klinik" }, user: this.currentUser };
            } else {
                this.activeDoctor = await response.json();
            }
            this.renderProfile();
        } catch (error) {
            this.activeDoctor = { id: 99, specialization: "Bağlantı Yok", clinic: { id: 0, name: "Sunucuya Bağlanılamadı" }, user: this.currentUser };
            this.renderProfile();
        }
    },

    renderProfile: function() {
        const user = this.currentUser;
        const container = document.getElementById('doctor-details');
        if (container && this.activeDoctor) {
            container.innerHTML = `
                <div class="profile-grid">
                    <div class="info-item"><i class="fas fa-id-card"></i><strong>TC Kimlik No</strong><span>${user.tckn}</span></div>
                    <div class="info-item"><i class="fas fa-envelope"></i><strong>E-posta Adresi</strong><span>${user.email}</span></div>
                    <div class="info-item"><i class="fas fa-stethoscope"></i><strong>Uzmanlık Alanı</strong><span>${this.activeDoctor.specialization}</span></div>
                    <div class="info-item"><i class="fas fa-hospital"></i><strong>Görevli Klinik</strong><span>${this.activeDoctor.clinic?.name || 'Belirtilmemiş'}</span></div>
                </div>`;
        }
    },

    fetchAppointments: async function() {
        const tbody = document.getElementById('appointments-table-body');
        if (!tbody) return;
        try {
        const response = await fetch('http://localhost:8080/api/appointments/doctor/my', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
        });
            if (!response.ok) throw new Error();
            const appointments = await response.json();
            
            if (appointments.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Randevu bulunamadı.</td></tr>';
                return;
            }
            tbody.innerHTML = appointments.map(app => `
                <tr>
                    <td>${app.patient.firstName} ${app.patient.lastName}</td>
                    <td>${app.patient.tckn}</td>
                    <td>${this.formatDate(app.slot.startTime)}</td>
                    <td><span class="status-badge status-${app.status.toLowerCase()}">${app.status}</span></td>
                    <td>
                        ${app.status === 'SCHEDULED' ? 
                            `<button class="btn-not-attended" onclick="window.doctorApp.markAsNotAttended(${app.id})">Gelmedi</button>` 
                            : `<span style="color:gray; font-size:0.8rem;">İşlem Kapalı</span>`}
                    </td>
                </tr>`).join('');
        } catch (e) { 
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Liste alınamadı (Yetki Hatası veya Bağlantı Yok).</td></tr>';
        }
    },

    markAsNotAttended: async function(appointmentId) {
        if (!confirm("Hasta gelmedi olarak işaretlensin mi? (Ceza uygulanır)")) return;
        try {
            const response = await fetch(`http://localhost:8080/api/appointments/${appointmentId}/not-attended`, {
                method: 'PUT',
                headers: { 
                'Authorization': `Bearer ${localStorage.getItem("token")}` // BU SATIRI EKLE
            }
            });
            if (response.ok) {
                alert("İşaretlendi!");
                this.fetchAppointments(); 
            } else if (response.status === 403) {
            alert("Bu işlemi yapmaya yetkiniz yok.");
        }
            else {
                alert("Hata oluştu.");
            }
        } catch (e) { alert("Sunucu hatası."); }
    },

    fetchSlots: async function() {
        const tbody = document.getElementById('slot-table-body');
        if (!tbody || !this.activeDoctor) return;
       try {
        const response = await fetch(`http://localhost:8080/api/slots/doctor/${this.activeDoctor.id}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
        });
            if (!response.ok) throw new Error();
            const slots = await response.json();
            tbody.innerHTML = slots.map(slot => `
                <tr>
                    <td>${this.formatDate(slot.startTime)}</td>
                    <td>${this.formatDate(slot.endTime)}</td>
                    <td>${slot.status}</td>
                    <td><button class="btn-cancel" onclick="alert('İptal sistemi hazır değil')">İptal</button></td>
                </tr>`).join('');
        } catch (e) { tbody.innerHTML = '<tr><td colspan="4">Slotlar yüklenemedi.</td></tr>'; }
    },

    handlePasswordUpdate: async function () {
        const curPass = document.getElementById('currentPassword').value;
        const newPass = document.getElementById('newPassword').value;
        const confPass = document.getElementById('confirmPassword').value;
        if (newPass !== confPass) return alert("Şifreler eşleşmiyor!");
        try {
            const response = await fetch('http://localhost:8080/api/auth/reset-password-logged-in', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tckn: this.currentUser.tckn, currentPassword: curPass, newPassword: newPass })
            });
            if (response.ok) { alert("Şifre değişti."); this.logout(); } else { alert("Hata!"); }
        } catch (e) { alert("Bağlantı hatası!"); }
    },

    sendEmailVerification: async function () {
        const email = document.getElementById('newEmail').value;
        try {
            const res = await fetch('http://localhost:8080/api/auth/send-verification-code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, tckn: this.currentUser.tckn })
            });
            if (res.ok) { document.getElementById('emailStep1').style.display = 'none'; document.getElementById('emailStep2').style.display = 'block'; }
        } catch (e) { alert("Hata!"); }
    },

    confirmEmailUpdate: async function () {
        const code = document.getElementById('emailVerifyCode').value;
        const email = document.getElementById('newEmail').value;
        try {
            const res = await fetch('http://localhost:8080/api/auth/verify-email-update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code: code, newEmail: email, tckn: this.currentUser.tckn })
            });
            if (res.ok) { alert("E-posta güncellendi."); location.reload(); }
        } catch (e) { alert("Hata!"); }
    },

    showTab: function(tabId, element) {
        document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        document.getElementById(tabId).classList.add('active');
        document.querySelectorAll('.sidebar nav ul li').forEach(l => l.classList.remove('active'));
        if(element) element.classList.add('active');
    },

    logout: function() { localStorage.clear(); window.location.href = 'index.html'; },
    formatDate: function(d) { return d ? new Date(d).toLocaleString('tr-TR') : "-"; },
    setupEventListeners: function() {
    const slotForm = document.getElementById('slot-form');
    if (slotForm) {
        slotForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const startTime = document.getElementById('startTime').value;
            const endTime = document.getElementById('endTime').value;
            const token = localStorage.getItem("token"); // Token'ı alıyoruz

            // Backend'deki Slot entity yapısına uygun payload
            const payload = {
                startTime: startTime.includes(":") && startTime.split(":").length === 2 ? startTime + ":00" : startTime,
                endTime: endTime.includes(":") && endTime.split(":").length === 2 ? endTime + ":00" : endTime,
                doctor: { id: this.activeDoctor.id }, // Doktor nesnesini bağlıyoruz
                status: "AVAILABLE"
            };

            try {
                const response = await fetch('http://localhost:8080/api/slots', {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}` // Güvenlik için şart
                    },
                    body: JSON.stringify(payload)
                });

                if (response.ok) {
                    alert("Slot başarıyla oluşturuldu!");
                    slotForm.reset(); // Formu temizle
                    this.fetchSlots(); // Listeyi anlık güncelle
                } else {
                    const errorData = await response.json();
                    alert("Hata: " + (errorData.message || "Slot oluşturulamadı. Zaman çakışması olabilir."));
                }
            } catch (error) {
                console.error("Slot hatası:", error);
                alert("Sunucuya bağlanılamadı!");
            }
        });
    }
}
};

document.addEventListener('DOMContentLoaded', () => window.doctorApp.init());