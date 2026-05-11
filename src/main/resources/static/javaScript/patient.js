window.showSection = function (sectionId, element) {
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

    if (sectionId === 'get-appointment') {
        const frame = document.getElementById('appointmentFrame');
        if (frame) {
            frame.contentWindow.location.reload();
        }
    }
};

window.filterAppointments = async function (type) {
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active-filter'));
    const activeBtn = document.getElementById('btn-' + type);
    if (activeBtn) activeBtn.classList.add('active-filter');

    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.token) {
        console.error("Kullanıcı oturum verisi bulunamadı!");
        return;
    }

    const userId = authData.user.id;
    const BASE_URL = 'https://medsoft.up.railway.app/api/appointments';
    let url = '';

    if (type === 'past') {
        url = `${BASE_URL}/patient/${userId}/past`;
    } else {
        url = `${BASE_URL}/patient/${userId}`;
    }

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + authData.token,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(`Sunucu hatası: ${response.status}`);

        let data = await response.json();

        if (type === 'active') {
            data = data.filter(app => app.canCancel === true);
        } else if (type === 'past' && !url.includes('/past')) {
            data = data.filter(app => app.canCancel === false);
        }

        renderTable(data);

    } catch (error) {
        console.error("Filtreleme sırasında hata oluştu:", error);
        renderTable([]);
    }
};

function renderTable(data) {
    const tbody = document.getElementById('appointmentTableBody');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 20px;">Kayıt bulunamadı.</span></tr>';
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
                <td>${doctorName}</span>
                <td>${clinicName}</span>
                <td>${dateStr}</span>
                <td>
                    <span class="status-badge ${startTime >= now ? 'status-active' : 'status-past'}">
                        ${app.status} 
                    </span>
                </span>
                <td>
                    ${isCancelable ?
                `<button onclick="cancelAppointment(${app.id})" class="btn-cancel-table">
                            <i class="fas fa-times"></i> İptal Et
                         </button>` :
                `<span style="color: #cbd5e1; font-size: 0.8rem;">İşlem Yapılamaz</span>`
            }
                </span>
            </tr>
        `;
    }).join('');
}

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
        const response = await fetch('https://medsoft.up.railway.app/api/auth/reset-password-logged-in', {
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

document.addEventListener('DOMContentLoaded', () => {
    const authData = JSON.parse(localStorage.getItem('user'));
    if (!authData || !authData.user) {
        window.location.href = 'index.html';
        return;
    }

    const user = authData.user;

    const capitalize = (str) => str ? str.toLowerCase().replace(/\b\w/g, l => l.toUpperCase()) : "";
    const formatBloodGroup = (bg) => {
        const mapping = {
            'A_POSITIVE': 'A+', 'A_NEGATIVE': 'A-', 'B_POSITIVE': 'B+', 'B_NEGATIVE': 'B-',
            'AB_POSITIVE': 'AB+', 'AB_NEGATIVE': 'AB-', 'O_POSITIVE': '0+', 'O_NEGATIVE': '0-'
        };
        return mapping[bg] || bg;
    };

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

    const fields = {
        'infoTckn': user.tckn,
        'infoEmail': user.email,
        'infoBlood': formatBloodGroup(user.bloodGroup),
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

window.sendEmailVerification = async function () {
    const email = document.getElementById("newEmail")?.value;
    const errorBox = document.getElementById("errorBox");
    const sendBtn = document.getElementById("sendVerificationBtn");
    const step1Div = document.getElementById("emailStep1");
    const step2Div = document.getElementById("emailStep2");
    const verifyCodeInput = document.getElementById("emailVerifyCode");

    if (!email) {
        showError("Lütfen e-posta adresinizi giriniz!");
        return;
    }

    if (!email.includes("@") || !email.includes(".")) {
        showError("Geçerli bir e-posta adresi giriniz!");
        return;
    }

    if (sendBtn) {
        sendBtn.disabled = true;
        sendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Gönderiliyor...';
    }

    try {
        const token = localStorage.getItem("token");
        const user = JSON.parse(localStorage.getItem("user"));
        const tckn = user?.user?.tckn || user?.tckn;

        const res = await fetch('https://medsoft.up.railway.app/api/auth/send-verification-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ email: email, tckn: tckn })
        });

        const data = await res.json();

        if (res.ok) {
            if (step1Div) step1Div.style.display = 'none';
            if (step2Div) step2Div.style.display = 'block';
            if (verifyCodeInput) verifyCodeInput.value = "";
            showSuccess("✅ Doğrulama kodu e-posta adresinize gönderildi! 15 dakika içinde kullanınız.");

            setTimeout(() => {
                if (verifyCodeInput) {
                    showError("⏰ Kodun süresi doldu! Lütfen yeni kod isteyin.");
                    if (step1Div) step1Div.style.display = 'block';
                    if (step2Div) step2Div.style.display = 'none';
                }
            }, 14 * 60 * 1000);
        } else {
            showError(data.message || "❌ Kod gönderilemedi!");
        }
    } catch (e) {
        console.error("Email doğrulama hatası:", e);
        showError("❌ Bağlantı hatası! Lütfen tekrar deneyin.");
    } finally {
        if (sendBtn) {
            sendBtn.disabled = false;
            sendBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Kod Gönder';
        }
    }
};

window.confirmEmailUpdate = async function () {
    const code = document.getElementById("emailVerifyCode")?.value;
    const email = document.getElementById("newEmail")?.value;
    const errorBox = document.getElementById("errorBox");
    const confirmBtn = document.getElementById("confirmEmailBtn");

    if (!code || code.length !== 6) {
        showError("Lütfen 6 haneli doğrulama kodunu giriniz!");
        return;
    }

    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Onaylanıyor...';
    }

    try {
        const token = localStorage.getItem("token");
        const user = JSON.parse(localStorage.getItem("user"));
        const tckn = user?.user?.tckn || user?.tckn;

        const res = await fetch('https://medsoft.up.railway.app/api/auth/verify-email-update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ code: code, newEmail: email, tckn: tckn })
        });

        const data = await res.json();

        if (res.ok) {
            alert("✅ E-posta başarıyla güncellendi! Lütfen tekrar giriş yapın.");
            localStorage.clear();
            window.location.href = "index.html";
        } else {
            showError(data.message || data.error || "❌ Kod hatalı veya süresi dolmuş! Lütfen yeni kod isteyin.");
            document.getElementById("emailStep1").style.display = 'block';
            document.getElementById("emailStep2").style.display = 'none';
        }
    } catch (e) {
        console.error("Email onaylama hatası:", e);
        showError("❌ Bağlantı hatası!");
    } finally {
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = '<i class="fas fa-check"></i> Onayla';
        }
    }
};

window.cancelAppointment = async function (appointmentId) {
    if (!confirm("Bu randevuyu iptal etmek istediğinize emin misiniz? (24 saat kuralı geçerlidir)")) return;

    const authData = JSON.parse(localStorage.getItem('user'));

    try {
        const response = await fetch(`https://medsoft.up.railway.app/api/appointments/${appointmentId}/cancel`, {
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

function showError(msg) {
    const errorBox = document.getElementById("errorBox");
    if (errorBox) {
        errorBox.style.display = "block";
        errorBox.style.backgroundColor = "#fee2e2";
        errorBox.style.color = "#991b1b";
        errorBox.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${msg}`;
        setTimeout(() => {
            errorBox.style.display = "none";
        }, 5000);
    } else {
        alert(msg);
    }
}

function showSuccess(msg) {
    const errorBox = document.getElementById("errorBox");
    if (errorBox) {
        errorBox.style.display = "block";
        errorBox.style.backgroundColor = "#d1fae5";
        errorBox.style.color = "#065f46";
        errorBox.innerHTML = `<i class="fas fa-check-circle"></i> ${msg}`;
        setTimeout(() => {
            errorBox.style.display = "none";
        }, 5000);
    } else {
        alert(msg);
    }
}