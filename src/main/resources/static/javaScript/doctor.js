window.doctorApp = {
    activeDoctor: null,
    currentUser: null,
    token: null,
    isFirstLogin: true,

    init: async function () {

        const userData = JSON.parse(localStorage.getItem("user") || '{}');
        this.token = userData.token || localStorage.getItem("token");
        this.currentUser = userData.user || userData;

        if (!this.token) {
            alert("❌ Oturum bulunamadı! Lütfen tekrar giriş yapın.");
            window.location.href = "index.html";
            return;
        }

        const role = this.currentUser.role?.replace("ROLE_", "");
        if (role !== "DOCTOR") {
            alert("❌ Bu sayfaya sadece doktorlar erişebilir!");
            window.location.href = "index.html";
            return;
        }

        this.updateUI();
        await this.loadDoctorData();
        this.renderUserDetails();
        this.renderProfile();
        this.fetchSlots();
        this.fetchAppointments();
        this.setupEventListeners();
        this.loadSettingsValues();

    },

    updateUI: function () {
        let fullName = `${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`.trim();

        const nameHeader = document.getElementById('userNameDisplay');
        if (nameHeader) {
            nameHeader.innerText = fullName;
        }

        const sidebarName = document.getElementById('doctorNameSidebar');
        if (sidebarName) {
            sidebarName.innerText = fullName;
        }

        const avatar = document.getElementById('avatarInitial');
        if (avatar) {
            let firstName = this.currentUser.firstName || '';
            let firstLetter = 'H';
            let nameParts = firstName.split(' ');
            for (let part of nameParts) {
                if (!part.includes('.') && part.length > 0) {
                    firstLetter = part.charAt(0).toUpperCase();
                    break;
                }
            }
            avatar.innerText = firstLetter;
        }
    },

    loadSettingsValues: function () {
        const usernameInput = document.getElementById('editUsernameSetting');
        const tcknInput = document.getElementById('editTcknSetting');
        const emailInput = document.getElementById('newEmail');

        if (usernameInput) usernameInput.value = this.currentUser.username || '';
        if (tcknInput) tcknInput.value = this.currentUser.tckn || '';
        if (emailInput) emailInput.value = this.currentUser.email || '';
    },

    updateUsernameFromSettings: async function () {
        const username = document.getElementById("editUsernameSetting")?.value.trim();
        const oldUsername = this.currentUser.username;
        const statusSpan = document.getElementById("usernameStatus");

        if (!username) {
            alert("Kullanıcı adı boş olamaz!");
            return;
        }
        if (username.length < 3) {
            alert("Kullanıcı adı en az 3 karakter olmalıdır!");
            return;
        }
        if (username.includes(" ")) {
            alert("Kullanıcı adı boşluk içeremez!");
            return;
        }
        if (username === oldUsername) {
            alert("⚠️ Kullanıcı adı değişmemiştir!");
            return;
        }

        if (statusSpan) {
            statusSpan.style.display = 'inline-block';
            statusSpan.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Güncelleniyor...';
            statusSpan.style.color = '#f59e0b';
        }

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/doctors/profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ username: username })
            });

            if (response.ok) {
                this.currentUser.username = username;
                this.updateUI();
                this.renderUserDetails();

                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> Güncellendi!';
                    statusSpan.style.color = '#10b981';
                    setTimeout(() => { statusSpan.style.display = 'none'; }, 3000);
                }

                alert("✅ Kullanıcı adı başarıyla güncellendi!");
                this.checkAllInfoUpdated();
            } else {
                const error = await response.json();
                alert("❌ Hata: " + (error.error || "Güncelleme başarısız"));
                if (statusSpan) statusSpan.style.display = 'none';
            }
        } catch (error) {
            console.error("Güncelleme hatası:", error);
            alert("Güncellenirken bir hata oluştu!");
            if (statusSpan) statusSpan.style.display = 'none';
        }
    },

    updateTcknFromSettings: async function () {
        const tckn = document.getElementById("editTcknSetting")?.value.trim();
        const oldTckn = this.currentUser.tckn;
        const statusSpan = document.getElementById("tcknStatus");

        if (!tckn) {
            alert("TC Kimlik No boş olamaz!");
            return;
        }
        if (tckn.length !== 11) {
            alert("TC Kimlik No 11 haneli olmalıdır!");
            return;
        }
        if (!/^\d+$/.test(tckn)) {
            alert("TC Kimlik No sadece rakamlardan oluşmalıdır!");
            return;
        }
        if (tckn === oldTckn) {
            alert("⚠️ TC Kimlik No değişmemiştir!");
            return;
        }

        if (statusSpan) {
            statusSpan.style.display = 'inline-block';
            statusSpan.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Güncelleniyor...';
            statusSpan.style.color = '#f59e0b';
        }

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/doctors/profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ tckn: tckn })
            });

            if (response.ok) {
                this.currentUser.tckn = tckn;
                this.renderUserDetails();

                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> Güncellendi!';
                    statusSpan.style.color = '#10b981';
                    setTimeout(() => { statusSpan.style.display = 'none'; }, 3000);
                }

                alert("✅ TC Kimlik No başarıyla güncellendi!");
                this.checkAllInfoUpdated();
            } else {
                const error = await response.json();
                alert("❌ Hata: " + (error.error || "Güncelleme başarısız"));
                if (statusSpan) statusSpan.style.display = 'none';
            }
        } catch (error) {
            console.error("Güncelleme hatası:", error);
            alert("Güncellenirken bir hata oluştu!");
            if (statusSpan) statusSpan.style.display = 'none';
        }
    },

    sendEmailVerification: async function () {
        const email = document.getElementById("newEmail")?.value;
        const statusSpan = document.getElementById("emailStatus");

        if (!email) {
            alert("Lütfen e-posta adresinizi giriniz!");
            return;
        }
        if (!this.isValidEmail(email)) {
            alert("Geçerli bir e-posta adresi giriniz!");
            return;
        }
        if (email === this.currentUser.email) {
            alert("⚠️ E-posta adresi değişmemiştir!");
            return;
        }

        if (statusSpan) {
            statusSpan.style.display = 'inline-block';
            statusSpan.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Kod gönderiliyor...';
            statusSpan.style.color = '#f59e0b';
        }

        try {
            const res = await fetch(`https://medsoft.up.railway.app/api/auth/send-verification-code`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ email: email, tckn: this.currentUser?.tckn })
            });

            if (res.ok) {
                document.getElementById('emailStep1').style.display = 'none';
                document.getElementById('emailStep2').style.display = 'block';

                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> Kod gönderildi!';
                    statusSpan.style.color = '#10b981';
                    setTimeout(() => { statusSpan.style.display = 'none'; }, 3000);
                }

                alert("📧 Doğrulama kodu e-posta adresinize gönderildi!");
            } else {
                alert("❌ Kod gönderilemedi!");
                if (statusSpan) statusSpan.style.display = 'none';
            }
        } catch (e) {
            console.error("Email doğrulama hatası:", e);
            alert("Hata!");
            if (statusSpan) statusSpan.style.display = 'none';
        }
    },

    confirmEmailUpdate: async function () {
        const code = document.getElementById("emailVerifyCode")?.value;
        const email = document.getElementById("newEmail")?.value;
        const statusSpan = document.getElementById("emailStatus");

        if (!code || code.length !== 6) {
            alert("Lütfen 6 haneli doğrulama kodunu giriniz!");
            return;
        }

        if (statusSpan) {
            statusSpan.style.display = 'inline-block';
            statusSpan.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Onaylanıyor...';
            statusSpan.style.color = '#f59e0b';
        }

        try {
            const res = await fetch(`https://medsoft.up.railway.app/api/auth/verify-email-update`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ code: code, newEmail: email, tckn: this.currentUser?.tckn })
            });

            if (res.ok) {
                this.currentUser.email = email;
                this.renderUserDetails();

                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> E-posta güncellendi!';
                    statusSpan.style.color = '#10b981';
                    setTimeout(() => { statusSpan.style.display = 'none'; }, 3000);
                }

                alert("✅ E-posta başarıyla güncellendi!");
                this.checkAllInfoUpdated();

                document.getElementById('emailStep2').style.display = 'none';
                document.getElementById('emailStep1').style.display = 'block';
                document.getElementById("emailVerifyCode").value = "";
            } else {
                alert("❌ Kod hatalı veya süresi dolmuş!");
                if (statusSpan) statusSpan.style.display = 'none';
            }
        } catch (e) {
            console.error("Email onaylama hatası:", e);
            alert("Hata!");
            if (statusSpan) statusSpan.style.display = 'none';
        }
    },

    handlePasswordUpdate: async function () {
        const curPass = document.getElementById('currentPassword')?.value;
        const newPass = document.getElementById('newPassword')?.value;
        const confPass = document.getElementById('confirmPassword')?.value;
        const statusSpan = document.getElementById("passwordStatus");

        if (!curPass || !newPass) {
            alert("Mevcut şifre ve yeni şifre gereklidir!");
            return;
        }
        if (newPass !== confPass) {
            alert("Yeni şifreler eşleşmiyor!");
            return;
        }
        if (newPass.length < 6) {
            alert("Yeni şifre en az 6 karakter olmalıdır!");
            return;
        }
        if (curPass === newPass) {
            alert("⚠️ Yeni şifre mevcut şifre ile aynı olamaz!");
            return;
        }

        if (statusSpan) {
            statusSpan.style.display = 'inline-block';
            statusSpan.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Güncelleniyor...';
            statusSpan.style.color = '#f59e0b';
        }

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/auth/reset-password-logged-in`, {
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

            const data = await response.json();

            if (response.ok) {
                if (statusSpan) {
                    statusSpan.innerHTML = '<i class="fas fa-check-circle"></i> Şifre değişti!';
                    statusSpan.style.color = '#10b981';
                }

                alert("✅ Şifre başarıyla değiştirildi!\n\nLütfen yeniden giriş yapın.");
                this.logout();
            } else {
                alert("❌ " + (data.message || data.error || "Mevcut şifreniz hatalı!"));
                if (statusSpan) statusSpan.style.display = 'none';
            }
        } catch (e) {
            console.error("Şifre değiştirme hatası:", e);
            alert("Bağlantı hatası! Lütfen tekrar deneyin.");
            if (statusSpan) statusSpan.style.display = 'none';
        }
    },

    isValidEmail: function (email) {
        const emailRegex = /^[^\s@]+@([^\s@.,]+\.)+[^\s@.,]{2,}$/;
        return emailRegex.test(email);
    },

    loadDoctorData: async function () {
        const userId = this.currentUser?.id;
        if (!userId) return;

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/doctors/user/${userId}`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            if (response.ok) {
                this.activeDoctor = await response.json();

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

            const response = await fetch(`https://medsoft.up.railway.app/api/appointments/doctor/my`, {
                method: 'GET',
                headers: {
                    "Authorization": `Bearer ${this.token}`,
                    "Content-Type": "application/json"
                }
            });

            if (response.status === 401) {
                tbody.innerHTML = '<tr><td colspan="5">Oturum süresi doldu! <button onclick="window.doctorApp.logout()">Tekrar Giriş Yap<\/button><\/td><\/tr>';
                return;
            }

            if (response.status === 403) {
                tbody.innerHTML = '<tr><td colspan="5">⚠️ Bu işlem için yetkiniz yok.<\/td><\/tr>';
                return;
            }

            if (response.status === 404) {
                tbody.innerHTML = '<td><td colspan="5">⚠️ Randevu servisi bulunamadı. Lütfen daha sonra tekrar deneyin.<\/td><\/tr>';
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const appointments = await response.json();

            if (!appointments || appointments.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5">📋 Henüz randevunuz bulunmamaktadır.<\/td><\/tr>';
                return;
            }

            tbody.innerHTML = appointments.map(app => {

                const patientName = app.patient?.user?.firstName && app.patient?.user?.lastName
                    ? `${app.patient.user.firstName} ${app.patient.user.lastName}`
                    : app.patient?.user?.firstName || app.patient?.user?.lastName || 'Belirtilmemiş';

                const patientTckn = app.patient?.user?.tckn || 'Belirtilmemiş';
                const appointmentDate = app.slot?.startTime ? this.formatDate(app.slot.startTime) : 'Belirtilmemiş';
                const status = app.status || 'SCHEDULED';

                return `
                <tr>
                    <td>${this.escapeHtml(patientName)}</span>
                    <td>${this.escapeHtml(patientTckn)}</span>
                    <td>${appointmentDate}</span>
                    <td><span class="status-badge status-${status.toLowerCase()}">${this.getStatusText(status)}</span></span>
                    <td>
                        ${status === 'SCHEDULED' ?
                        `<button class="btn-not-attended" onclick="window.doctorApp.markAsNotAttended(${app.id})">
                                <i class="fas fa-user-slash"></i> Gelmedi
                            </button>` :
                        '<span style="color:gray;">İşlem Kapalı</span>'}
                    </span>
                </tr>
            `;
            }).join('');

        } catch (e) {
            console.error("Randevu hatası:", e);
            tbody.innerHTML = '<tr><td colspan="5">❌ Randevular yüklenirken bir hata oluştu: ' + e.message + '<\/td><\/tr>';
        }
    },

    getStatusText: function (status) {
        const statusMap = {
            'SCHEDULED': 'Planlandı',
            'APPROVED': 'Onaylandı',
            'COMPLETED': 'Tamamlandı',
            'CANCELLED': 'İptal Edildi',
            'NOT_ATTENDED': 'Gelmedi'
        };
        return statusMap[status] || status;
    },

    escapeHtml: function (str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    markAsNotAttended: async function (appointmentId) {
        if (!confirm("Hasta gelmedi olarak işaretlensin mi?")) return;

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/appointments/${appointmentId}/not-attended`, {
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

        tbody.innerHTML = '<tr><td colspan="4">Yükleniyor...<\/td><\/tr>';

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/slots/doctor/${this.activeDoctor.id}`, {
                headers: { 'Authorization': `Bearer ${this.token}` }
            });

            console.log("Slot API yanıt durumu:", response.status);

            if (response.status === 401) {
                tbody.innerHTML = '<tr><td colspan="4">Oturum süresi doldu! <button onclick="window.doctorApp.logout()">Tekrar Giriş Yap<\/button><\/td><\/tr>';
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const slots = await response.json();
            console.log("Gelen slotlar:", slots);

            if (!slots || slots.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4">📅 Henüz slot oluşturulmamış.<\/td><\/tr>';
                return;
            }

            tbody.innerHTML = slots.map(slot => {
                const isAvailable = slot.status === 'AVAILABLE';
                const statusText = isAvailable ? '✅ Boş' : '🔴 Dolu';
                const statusClass = isAvailable ? 'status-available' : 'status-reserved';

                return `
                <tr>
                    <td>${this.formatDate(slot.startTime)}<\/td>
                    <td>${this.formatDate(slot.endTime)}<\/td>
                    <td><span class="status-badge ${statusClass}">${statusText}<\/span><\/td>
                    <td>
                        ${isAvailable ?
                        `<button class="btn-cancel" onclick="window.doctorApp.deleteSlot(${slot.id})">🗑️ İptal</button>` :
                        '<span style="color:gray;">İşlem Kapalı</span>'}
                    <\/td>
                </tr>
            `;
            }).join('');

        } catch (e) {
            console.error("Slot hatası:", e);
            tbody.innerHTML = '<tr><td colspan="4">❌ Slotlar yüklenemedi. Lütfen sayfayı yenileyin.<\/td><\/tr>';
        }
    },

    deleteSlot: async function (slotId) {
        if (!confirm("Bu slotu iptal etmek istediğinizden emin misiniz?")) return;

        try {
            const response = await fetch(`https://medsoft.up.railway.app/api/slots/${slotId}`, {
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

                if (!window.doctorApp.activeDoctor || !window.doctorApp.activeDoctor.id) {
                    alert("❌ Doktor verileri henüz yüklenmedi. Lütfen sayfayı yenileyip biraz bekleyin.");
                    return;
                }

                const startTime = document.getElementById('startTime')?.value;
                const endTime = document.getElementById('endTime')?.value;

                const payload = {
                    startTime: startTime,
                    endTime: endTime,
                    doctor: { id: parseInt(window.doctorApp.activeDoctor.id) },
                    status: "AVAILABLE"
                };

                console.log("🔵 Gönderilen veri:", payload);

                try {
                    const response = await fetch(`https://medsoft.up.railway.app/api/slots`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${this.token}`
                        },
                        body: JSON.stringify(payload)
                    });

                    console.log("🔵 Slot oluşturma yanıtı:", response.status);

                    if (response.ok) {
                        const result = await response.json();
                        console.log("✅ Slot oluşturuldu:", result);
                        alert("✅ Slot başarıyla oluşturuldu!");
                        slotForm.reset();
                        this.fetchSlots();
                    } else {
                        const error = await response.text();
                        console.error("❌ Slot hatası:", error);
                        alert("❌ Slot oluşturulamadı! " + error);
                    }
                } catch (error) {
                    console.error("❌ Slot hatası:", error);
                    alert("Sunucu hatası! Lütfen tekrar deneyin.");
                }
            });
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
    },
};

document.addEventListener('DOMContentLoaded', () => window.doctorApp.init());