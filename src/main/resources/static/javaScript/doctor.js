window.doctorApp = {
    activeDoctor: null,
    currentUser: null,
    token: null,

    init: async function () {
        console.log("🔵 Doktor paneli başlatılıyor...");

        // Token ve kullanıcı bilgilerini al
        const userData = JSON.parse(localStorage.getItem("user") || '{}');
        this.token = userData.token || localStorage.getItem("token");
        this.currentUser = userData.user || userData;

        console.log("🔵 Token var mı?", this.token ? "EVET" : "HAYIR");
        console.log("🔵 Kullanıcı:", this.currentUser);

        // Token kontrolü
        if (!this.token) {
            alert("❌ Oturum bulunamadı! Lütfen tekrar giriş yapın.");
            window.location.href = "index.html";
            return;
        }

        // Rol kontrolü
        const role = this.currentUser.role?.replace("ROLE_", "");
        if (role !== "DOCTOR") {
            alert("❌ Bu sayfaya sadece doktorlar erişebilir!");
            window.location.href = "index.html";
            return;
        }

        // UI güncellemeleri
        this.updateUI();

        // Verileri yükle
        await this.loadDoctorData();
        this.renderUserDetails();
        this.renderProfile();
        this.fetchSlots();
        this.fetchAppointments();
        this.setupEventListeners();
        this.renderProfileEditForm();

        console.log("✅ Doktor paneli hazır!");
    },

    updateUI: function () {
        const nameHeader = document.getElementById('userNameDisplay');
        if (nameHeader) {
            nameHeader.innerText = `Dr. ${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`;
        }

        const sidebarName = document.getElementById('doctorNameSidebar');
        if (sidebarName) {
            sidebarName.innerText = `Dr. ${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`;
        }

        const avatar = document.getElementById('avatarInitial');
        if (avatar) {
            avatar.innerText = (this.currentUser.firstName || 'D').charAt(0).toUpperCase();
        }
    },

    loadDoctorData: async function () {
        const userId = this.currentUser?.id;
        if (!userId) return;

        try {
            const response = await fetch(`http://localhost:8080/api/doctors/user/${userId}`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (response.ok) {
                this.activeDoctor = await response.json();
                console.log("✅ Doktor verileri yüklendi:", this.activeDoctor);
            } else {
                this.activeDoctor = { specialization: "Belirtilmemiş", clinic: { name: "-" } };
            }
        } catch (error) {
            console.error("Doktor verileri yüklenemedi:", error);
            this.activeDoctor = { specialization: "Bağlantı Hatası", clinic: { name: "-" } };
        }
    },

    renderUserDetails: function () {
        const container = document.getElementById('user-details');
        if (container && this.currentUser) {
            container.innerHTML = `
                <div class="profile-grid">
                    <div class="info-item">
                        <i class="fas fa-id-card"></i>
                        <strong>TC Kimlik No</strong>
                        <span>${this.currentUser.tckn || 'Belirtilmemiş'}</span>
                    </div>
                    <div class="info-item">
                        <i class="fas fa-envelope"></i>
                        <strong>E-posta Adresi</strong>
                        <span>${this.currentUser.email || 'Belirtilmemiş'}</span>
                    </div>
                    <div class="info-item">
                        <i class="fas fa-user"></i>
                        <strong>Kullanıcı Adı</strong>
                        <span>${this.currentUser.username || 'Belirtilmemiş'}</span>
                    </div>
                </div>
            `;
        }
    },

    renderProfile: function () {
        const container = document.getElementById('doctor-details');
        if (container && this.activeDoctor) {
            container.innerHTML = `
                <div class="profile-grid">
                    <div class="info-item">
                        <i class="fas fa-stethoscope"></i>
                        <strong>Uzmanlık Alanı</strong>
                        <span>${this.activeDoctor.specialization || 'Belirtilmemiş'}</span>
                    </div>
                    <div class="info-item">
                        <i class="fas fa-hospital"></i>
                        <strong>Görevli Klinik</strong>
                        <span>${this.activeDoctor.clinic?.name || 'Belirtilmemiş'}</span>
                    </div>
                </div>
            `;
        }
    },

    fetchAppointments: async function () {
        const tbody = document.getElementById('appointments-table-body');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="5">Yükleniyor...<\/td><\/tr>';

        try {
            const response = await fetch("http://localhost:8080/api/appointments/doctor/my", {
                headers: { "Authorization": `Bearer ${this.token}` }
            });

            if (response.status === 401) {
                tbody.innerHTML = '<tr><td colspan="5">Oturum süresi doldu! <button onclick="window.doctorApp.logout()">Tekrar Giriş Yap<\/button><\/td><\/tr>';
                return;
            }

            if (!response.ok) throw new Error();

            const appointments = await response.json();

            if (appointments.length === 0) {
                tbody.innerHTML = '</tr><td colspan="5">📋 Randevu bulunamadı.<\/td><\/tr>';
                return;
            }

            tbody.innerHTML = appointments.map(app => `
                <tr>
                    <td>${app.patient?.user?.firstName || '-'} ${app.patient?.user?.lastName || ''}</td>
                    <td>${app.patient?.user?.tckn || '-'}</td>
                    <td>${this.formatDate(app.slot?.startTime)}</td>
                    <td><span class="status-badge">${app.status || 'SCHEDULED'}</span></td>
                    <td>
                        ${app.status === 'SCHEDULED' ?
                    `<button class="btn-not-attended" onclick="window.doctorApp.markAsNotAttended(${app.id})">❌ Gelmedi</button>` :
                    '<span>-</span>'}
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            console.error("Randevu hatası:", e);
            tbody.innerHTML = '<tr><td colspan="5">❌ Randevular yüklenemedi.</td></tr>';
        }
    },

    markAsNotAttended: async function (appointmentId) {
        if (!confirm("Hasta gelmedi olarak işaretlensin mi?")) return;

        try {
            const response = await fetch(`http://localhost:8080/api/appointments/${appointmentId}/not-attended`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (response.ok) {
                alert("✅ İşaretlendi!");
                this.fetchAppointments();
            } else {
                alert("❌ Hata oluştu!");
            }
        } catch (e) {
            alert("Sunucu hatası!");
        }
    },

    fetchSlots: async function () {
        const tbody = document.getElementById('slot-table-body');
        if (!tbody || !this.activeDoctor?.id) return;

        tbody.innerHTML = '<tr><td colspan="4">Yükleniyor...</td></tr>';

        try {
            const response = await fetch(`http://localhost:8080/api/slots/doctor/${this.activeDoctor.id}`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (!response.ok) throw new Error();

            const slots = await response.json();

            if (slots.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4">📅 Henüz slot oluşturulmamış.</td></tr>';
                return;
            }

            tbody.innerHTML = slots.map(slot => `
                <tr>
                    <td>${this.formatDate(slot.startTime)}</td>
                    <td>${this.formatDate(slot.endTime)}</td>
                    <td>${slot.available ? '✅ Boş' : '🔴 Dolu'}</td>
                    <td>
                        ${slot.available ?
                    `<button class="btn-cancel" onclick="window.doctorApp.deleteSlot(${slot.id})">🗑️ İptal</button>` :
                    '<span>-</span>'}
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            console.error("Slot hatası:", e);
            tbody.innerHTML = '<tr><td colspan="4">❌ Slotlar yüklenemedi.</td></tr>';
        }
    },

    deleteSlot: async function (slotId) {
        if (!confirm("Bu slotu iptal etmek istediğinizden emin misiniz?")) return;

        try {
            const response = await fetch(`http://localhost:8080/api/slots/${slotId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (response.ok) {
                alert("✅ Slot iptal edildi!");
                this.fetchSlots();
            } else {
                alert("❌ Slot iptal edilemedi!");
            }
        } catch (e) {
            alert("Sunucu hatası!");
        }
    },

    setupEventListeners: function () {
        const slotForm = document.getElementById('slot-form');
        if (slotForm) {
            slotForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                const startTime = document.getElementById('startTime')?.value;
                const endTime = document.getElementById('endTime')?.value;

                if (!startTime || !endTime) {
                    alert("Başlangıç ve bitiş zamanını giriniz!");
                    return;
                }

                const payload = {
                    startTime: startTime,
                    endTime: endTime,
                    doctor: { id: this.activeDoctor?.id },
                    available: true
                };

                try {
                    const response = await fetch('http://localhost:8080/api/slots', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${this.token}`
                        },
                        body: JSON.stringify(payload)
                    });

                    if (response.ok) {
                        alert("✅ Slot oluşturuldu!");
                        slotForm.reset();
                        this.fetchSlots();
                    } else {
                        alert("❌ Slot oluşturulamadı!");
                    }
                } catch (error) {
                    alert("Sunucu hatası!");
                }
            });
        }
    },

    handlePasswordUpdate: async function () {
        const curPass = document.getElementById('currentPassword')?.value;
        const newPass = document.getElementById('newPassword')?.value;
        const confPass = document.getElementById('confirmPassword')?.value;

        if (!curPass || !newPass) {
            alert("Mevcut şifre ve yeni şifre gereklidir!");
            return;
        }
        if (newPass !== confPass) {
            alert("Yeni şifreler eşleşmiyor!");
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/auth/reset-password-logged-in', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({
                    tckn: this.currentUser?.tckn,
                    currentPassword: curPass,
                    newPassword: newPass
                })
            });

            if (response.ok) {
                alert("✅ Şifre değişti! Lütfen tekrar giriş yapın.");
                this.logout();
            } else {
                alert("❌ Mevcut şifreniz hatalı!");
            }
        } catch (e) {
            alert("Bağlantı hatası!");
        }
    },

    updateDoctorProfile: async function () {
        const username = document.getElementById("editUsername")?.value.trim();
        const tckn = document.getElementById("editTckn")?.value.trim();
        const email = document.getElementById("editEmail")?.value.trim();

        const updateData = {};
        if (username) updateData.username = username;
        if (tckn) updateData.tckn = tckn;
        if (email) updateData.email = email;

        if (Object.keys(updateData).length === 0) {
            alert("Güncellemek için en az bir alan doldurun!");
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/doctors/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify(updateData)
            });

            if (response.ok) {
                alert("✅ Profil güncellendi! Lütfen tekrar giriş yapın.");
                this.logout();
            } else {
                alert("❌ Güncelleme başarısız!");
            }
        } catch (error) {
            alert("Profil güncellenirken hata oluştu!");
        }
    },

    renderProfileEditForm: function () {
        const container = document.getElementById('profile-edit-form');
        if (container && this.currentUser) {
            container.innerHTML = `
            <div class="form-group">
                <label><i class="fas fa-user"></i> Kullanıcı Adı</label>
                <input type="text" id="editUsername" class="form-input" value="${this.currentUser.username || ''}" placeholder="Kullanıcı adınız">
                <small>Kullanıcı adınızı değiştirebilirsiniz</small>
            </div>
            <div class="form-group">
                <label><i class="fas fa-id-card"></i> TC Kimlik No</label>
                <input type="text" id="editTckn" class="form-input" value="${this.currentUser.tckn || ''}" placeholder="11 haneli TCKN" maxlength="11">
                <small>TC Kimlik numaranızı giriniz (11 hane)</small>
            </div>
            <div class="form-group">
                <label><i class="fas fa-envelope"></i> E-posta Adresi</label>
                <input type="email" id="editEmail" class="form-input" value="${this.currentUser.email || ''}" placeholder="ornek@mail.com">
                <small>Geçerli bir email adresi giriniz</small>
            </div>
            <button type="button" class="btn-primary" onclick="window.doctorApp.updateDoctorProfile()">
                <i class="fas fa-save"></i> Bilgilerimi Güncelle
            </button>
        `;
        }

        // Uyarıları göster
        if (!this.currentUser?.tckn || this.currentUser.tckn === '') {
            const tcknWarning = document.getElementById('tcknWarning');
            if (tcknWarning) {
                tcknWarning.style.display = 'block';
                tcknWarning.innerHTML = '<i class="fas fa-exclamation-triangle"></i> ⚠️ TC Kimlik numaranız henüz kayıtlı değil! Lütfen güncelleyiniz.';
            }
        }

        if (this.currentUser?.email && this.currentUser.email.endsWith('@hastane.com')) {
            const emailWarning = document.getElementById('emailWarning');
            if (emailWarning) {
                emailWarning.style.display = 'block';
                emailWarning.innerHTML = '<i class="fas fa-exclamation-triangle"></i> ⚠️ Geçici bir email adresi kullanıyorsunuz. Lütfen güncelleyiniz!';
            }
        }
    },

    showTab: function (tabId, element) {
        document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        document.getElementById(tabId).classList.add('active');
        document.querySelectorAll('.sidebar nav ul li').forEach(l => l.classList.remove('active'));
        if (element) element.classList.add('active');

        if (tabId === 'slots') this.fetchSlots();
        if (tabId === 'appointments') this.fetchAppointments();
    },

    formatDate: function (d) {
        return d ? new Date(d).toLocaleString('tr-TR') : '-';
    },

    logout: function () {
        localStorage.clear();
        window.location.href = 'index.html';
    }
};

// Sayfa yüklendiğinde başlat
document.addEventListener('DOMContentLoaded', () => window.doctorApp.init());