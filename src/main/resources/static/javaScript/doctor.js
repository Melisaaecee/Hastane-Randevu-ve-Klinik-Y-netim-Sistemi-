window.doctorApp = {
    activeDoctor: null,
    // Test için sabit bir kullanıcı verisi (Daha önce oluşturduğumuz id:6 üzerinden güncelledim)
    currentUser: {
        id: 6, 
        firstName: "Ahmet",
        lastName: "Yılmaz",
        role: "DOCTOR"
    },
    
    init: async function() {
        console.log("Doktor Paneli Başlatıldı...");
        const userData = this.currentUser; 
        await this.fetchDoctorDetails(userData.id);
        
        // İlk açılışta verileri çek
        this.fetchSlots();
        this.fetchAppointments();
        
        this.setupEventListeners();
    },

    fetchDoctorDetails: async function(userId) {
        try {
            const response = await fetch(`http://localhost:8080/api/doctors/user/${userId}`);
            if (!response.ok) throw new Error("Doktor bilgileri alınamadı");
            
            this.activeDoctor = await response.json();
            console.log("Aktif Doktor:", this.activeDoctor);
            
            this.renderProfile();
        } catch (error) {
            console.error("Hata:", error);
            // Hata durumunda test verisi göster (Opsiyonel)
            this.activeDoctor = {
                id: 1,
                specialization: "Dahiliye",
                clinic: { id: 1, name: "Merkez Klinik" },
                user: this.currentUser
            };
            this.renderProfile();
        }
    },

    renderProfile: function() {
        if (!this.activeDoctor) return;
        const user = this.activeDoctor.user;
        const container = document.getElementById('doctor-details');
        const nameHeader = document.getElementById('userNameDisplay');

        if (nameHeader) nameHeader.innerText = `Dr. ${user.firstName} ${user.lastName}`;
        
        if (container) {
            container.innerHTML = `
                <div class="profile-grid">
                    <div class="info-item"><strong>TCKN:</strong> <span>${user.tckn || '12345678901'}</span></div>
                    <div class="info-item"><strong>E-posta:</strong> <span>${user.email || 'test@hastane.com'}</span></div>
                    <div class="info-item"><strong>Uzmanlık:</strong> <span>${this.activeDoctor.specialization}</span></div>
                    <div class="info-item"><strong>Klinik:</strong> <span>${this.activeDoctor.clinic.name}</span></div>
                </div>`;
        }
    },

    // YENİ: Giriş yapan doktorun randevularını çeken fonksiyon
    fetchAppointments: async function() {
        try {
            const response = await fetch('http://localhost:8080/api/appointments/doctor/my');
            if (!response.ok) throw new Error("Randevular yüklenemedi");
            
            const appointments = await response.json();
            this.renderAppointments(appointments);
        } catch (error) {
            console.error("Randevu listeleme hatası:", error);
            const tbody = document.getElementById('appointments-table-body');
            if (tbody) tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:red;">Randevular getirilemedi.</td></tr>';
        }
    },

    // YENİ: Randevuları tabloya basan fonksiyon
    renderAppointments: function(appointments) {
        const tbody = document.getElementById('appointments-table-body');
        if (!tbody) return;

        if (appointments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">Size alınmış aktif bir randevu bulunmamaktadır.</td></tr>';
            return;
        }

        tbody.innerHTML = appointments.map(app => `
            <tr>
                <td>${app.patient.firstName} ${app.patient.lastName}</td>
                <td>${app.patient.tckn}</td>
                <td>${this.formatDate(app.slot.startTime)}</td>
                <td><span class="status-badge status-${app.status.toLowerCase()}">${app.status}</span></td>
            </tr>
        `).join('');
    },

    fetchSlots: async function() {
        if (!this.activeDoctor) return;
        try {
            const response = await fetch(`http://localhost:8080/api/slots/doctor/${this.activeDoctor.id}`);
            const slots = await response.json();
            
            const tbody = document.getElementById('slot-table-body');
            if (!tbody) return;

            if (slots.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">Henüz tanımlı slot yok.</td></tr>';
                return;
            }

            tbody.innerHTML = slots.map(slot => `
                <tr>
                    <td>${this.formatDate(slot.startTime)}</td>
                    <td>${this.formatDate(slot.endTime)}</td>
                    <td><span class="status-badge status-${slot.status.toLowerCase()}">${slot.status}</span></td>
                    <td>
                        <button class="btn-cancel" onclick="window.doctorApp.cancelSlot(${slot.id})">
                            <i class="fas fa-times"></i> İptal Et
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            console.error("Slot listeleme hatası:", error);
        }
    },

    createSlot: async function(e) {
        e.preventDefault();
        
        if (!this.activeDoctor) {
            alert("Doktor bilgileri yüklenemedi, lütfen sayfayı yenileyin.");
            return;
        }

        const slotData = {
            startTime: document.getElementById('startTime').value,
            endTime: document.getElementById('endTime').value,
            status: "AVAILABLE",
            doctor: { id: this.activeDoctor.id },
            clinic: { id: this.activeDoctor.clinic.id }
        };

        try {
            const response = await fetch('http://localhost:8080/api/slots', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(slotData)
            });

            if (response.ok) {
                alert("Slot başarıyla oluşturuldu!");
                e.target.reset(); 
                this.fetchSlots(); 
            } else {
                const err = await response.json();
                alert("Hata: " + (err.message || "Slot oluşturulamadı"));
            }
        } catch (error) {
            alert("Sunucuya bağlanılamadı.");
        }
    },

    cancelSlot: async function(slotId) {
        if (!confirm("Bu slotu iptal etmek istediğinize emin misiniz?")) return;

        try {
            const response = await fetch(`http://localhost:8080/api/slots/${slotId}/cancel`, { 
                method: 'PUT'
            });

            if (response.ok) {
                alert("Slot başarıyla iptal edildi.");
                this.fetchSlots(); 
            } else {
                alert("İptal işlemi başarısız.");
            }
        } catch (error) {
            alert("Sunucuya bağlanılamadı.");
        }
    },

    setupEventListeners: function() {
        const form = document.getElementById('slot-form');
        if (form) {
            form.onsubmit = (e) => this.createSlot(e);
        }
    },

    formatDate: function(dateStr) {
        return new Date(dateStr).toLocaleString('tr-TR');
    },

    showTab: function(tabId, element) {
        document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        document.getElementById(tabId).classList.add('active');
        
        if(element) {
            document.querySelectorAll('.sidebar nav ul li').forEach(l => l.classList.remove('active'));
            element.classList.add('active');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => window.doctorApp.init());