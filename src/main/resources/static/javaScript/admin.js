const API_URL = "http://localhost:8080/api";

// ================= TOKEN & FETCH =================
async function fetchWithAuth(url, options = {}) {
    const rawData = localStorage.getItem("user");
    const userData = rawData ? JSON.parse(rawData) : null;
    const token = userData?.token || userData?.jwt || userData?.accessToken;

    if (!token) {
        window.location.href = "index.html";
        return null;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
            ...options.headers
        }
    });

    if (response.status === 401 || response.status === 403) {
        alert("Yetki hatası! Lütfen tekrar giriş yapın.");
        localStorage.clear();
        window.location.href = "index.html";
        return null;
    }
    return response;
}

// ================= TAB SYSTEM =================
window.switchTab = function (tabId, event) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    const targetTab = document.getElementById(tabId);
    if (targetTab) targetTab.classList.add('active');

    document.querySelectorAll('.sidebar nav ul li').forEach(li => li.classList.remove('active'));
    if (event) event.currentTarget.classList.add('active');

    const titles = {
        'dashboard': 'Dashboard', 'profile': 'Hesap Yönetimi', 'cities': 'Şehir Yönetimi',
        'districts': 'İlçe Yönetimi', 'hospitals': 'Hastane Yönetimi', 'clinics': 'Klinik Yönetimi',
        'doctors': 'Doktor Yönetimi', 'users': 'Kullanıcılar', 'appointments': 'Randevular', 'slots': 'Slotlar'
    };
    document.getElementById("page-title").textContent = titles[tabId] || 'Panel';

    if (tabId === 'dashboard') loadStats();
    if (tabId === 'cities') loadCities();
    if (tabId === 'districts') { loadCitiesForSelect('districtCitySelect'); loadDistricts(); }
    if (tabId === 'hospitals') { loadCitiesForSelect('hospitalCitySelect'); loadHospitals(); }
    if (tabId === 'clinics') { loadHospitalsForSelect('clinicHospitalSelect'); loadClinics(); }
    if (tabId === 'doctors') {
        loadHospitalsForSelect('doctorHospitalSelect');
        loadDoctors();
    }
    if (tabId === 'users') loadUsers();
    if (tabId === 'appointments') loadAppointments();
    if (tabId === 'slots') loadSlots();
    if (tabId === 'profile') loadMyProfile();
};

// ================= DASHBOARD =================
window.loadStats = async function () {
    try {
        const [u, h, d, a, c] = await Promise.all([
            fetchWithAuth(`${API_URL}/users`),
            fetchWithAuth(`${API_URL}/hospitals`),
            fetchWithAuth(`${API_URL}/doctors`),
            fetchWithAuth(`${API_URL}/appointments`),
            fetchWithAuth(`${API_URL}/clinics`)
        ]);

        if (u && u.ok) document.getElementById("count-users").textContent = (await u.json()).length;
        if (h && h.ok) document.getElementById("count-hospitals").textContent = (await h.json()).length;
        if (d && d.ok) document.getElementById("count-doctors").textContent = (await d.json()).length;
        if (a && a.ok) document.getElementById("count-appointments").textContent = (await a.json()).length;
        if (c && c.ok) document.getElementById("count-clinics").textContent = (await c.json()).length;
    } catch (error) {
        console.error("İstatistikler yüklenemedi:", error);
    }
};

// ================= CITIES =================
let citiesData = [], citySortAsc = true;
window.loadCities = async function () {
    const res = await fetchWithAuth(`${API_URL}/cities`);
    if (res) { citiesData = await res.json(); renderCities(); }
};
function renderCities() {
    let filtered = citiesData.filter(c => c.name.toLowerCase().includes((document.getElementById('citySearchInput')?.value || '').toLowerCase()));
    filtered.sort((a, b) => citySortAsc ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name));
    document.getElementById("cityTableBody").innerHTML = filtered.map(c => `
        <tr>
            <td>${c.id}</table>
            <td><input type="text" id="cityName_${c.id}" value="${c.name}" class="form-input" style="width:100%"></td>
            <td class="action-buttons">
                <button class="btn-update" onclick="updateCity(${c.id})">Güncelle</button>
                <button class="btn-delete" onclick="deleteCity(${c.id})">Sil</button>
            </td>
        </tr>
    `).join("");
}
window.filterCities = () => renderCities();
window.sortCities = () => { citySortAsc = !citySortAsc; renderCities(); };
window.addCity = async function () {
    const name = document.getElementById("cityNameInput").value.trim();
    if (!name) return alert("Şehir adı girin!");
    const res = await fetchWithAuth(`${API_URL}/cities`, { method: "POST", body: JSON.stringify({ name }) });
    if (res?.ok) { alert("Eklendi"); document.getElementById("cityNameInput").value = ""; loadCities(); }
};
window.updateCity = async function (id) {
    const name = document.getElementById(`cityName_${id}`).value.trim();
    const res = await fetchWithAuth(`${API_URL}/cities/${id}`, { method: "PUT", body: JSON.stringify({ name }) });
    if (res?.ok) { alert("Güncellendi"); loadCities(); }
};
window.deleteCity = async function (id) {
    if (!confirm("Silmek istiyor musunuz?")) return;
    const res = await fetchWithAuth(`${API_URL}/cities/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadCities(); }
};

// ================= DISTRICTS =================
let districtsData = [], districtSortAsc = true;
window.loadDistricts = async function () {
    const res = await fetchWithAuth(`${API_URL}/districts`);
    if (res) { districtsData = await res.json(); renderDistricts(); }
};
function renderDistricts() {
    let filtered = districtsData.filter(d => d.name.toLowerCase().includes((document.getElementById('districtSearchInput')?.value || '').toLowerCase()));
    filtered.sort((a, b) => districtSortAsc ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name));
    document.getElementById("districtTableBody").innerHTML = filtered.map(d => `
        <tr>
            <td>${d.id}</td>
            <td><input type="text" id="districtName_${d.id}" value="${d.name}" class="form-input"></td>
            <td>${d.city?.name || '-'}</td>
            <td class="action-buttons">
                <button class="btn-update" onclick="updateDistrict(${d.id})">Güncelle</button>
                <button class="btn-delete" onclick="deleteDistrict(${d.id})">Sil</button>
            </td>
        </tr>
    `).join("");
}
window.filterDistricts = () => renderDistricts();
window.sortDistricts = () => { districtSortAsc = !districtSortAsc; renderDistricts(); };
window.addDistrict = async function () {
    const name = document.getElementById("districtNameInput").value.trim();
    const cityId = document.getElementById("districtCitySelect").value;
    if (!name || !cityId) return alert("İlçe adı ve şehir seçin!");
    const res = await fetchWithAuth(`${API_URL}/districts`, { method: "POST", body: JSON.stringify({ name, city: { id: parseInt(cityId) } }) });
    if (res?.ok) { alert("Eklendi"); document.getElementById("districtNameInput").value = ""; loadDistricts(); }
};
window.updateDistrict = async function (id) {
    const name = document.getElementById(`districtName_${id}`).value.trim();
    const res = await fetchWithAuth(`${API_URL}/districts/${id}`, { method: "PUT", body: JSON.stringify({ name }) });
    if (res?.ok) { alert("Güncellendi"); loadDistricts(); }
};
window.deleteDistrict = async function (id) {
    if (!confirm("Sil?")) return;
    const res = await fetchWithAuth(`${API_URL}/districts/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadDistricts(); }
};

// ================= HOSPITALS =================
let hospitalsData = [], hospitalSortAsc = true;
window.loadHospitals = async function () {
    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals`);
        if (res && res.ok) {
            hospitalsData = await res.json();
            renderHospitals();
        } else {
            console.error("Hastaneler yüklenemedi");
            hospitalsData = [];
            renderHospitals();
        }
    } catch (error) {
        console.error("Hastane yükleme hatası:", error);
        hospitalsData = [];
        renderHospitals();
    }
};
function renderHospitals() {
    const searchTerm = (document.getElementById('hospitalSearchInput')?.value || '').toLowerCase();
    let filtered = hospitalsData.filter(h => h.name && h.name.toLowerCase().includes(searchTerm));
    filtered.sort((a, b) => hospitalSortAsc ? (a.name || '').localeCompare(b.name || '') : (b.name || '').localeCompare(a.name || ''));
    const tbody = document.getElementById("hospitalTableBody");
    if (!tbody) return;
    tbody.innerHTML = filtered.map(h => `
        <tr>
            <td>${h.id || '-'}</td>
            <td><input type="text" id="hospitalName_${h.id}" value="${escapeHtml(h.name || '')}" class="form-input" style="width:100%"></td>
            <td>${escapeHtml(h.city?.name || '-')}</td>
            <td>${escapeHtml(h.district?.name || '-')}</td>
            <td class="action-buttons">
                <button class="btn-update" onclick="updateHospital(${h.id})">Güncelle</button>
                <button class="btn-delete" onclick="deleteHospital(${h.id})">Sil</button>
            </td>
        </tr>
    `).join("");
}
window.filterHospitals = () => renderHospitals();
window.sortHospitals = () => { hospitalSortAsc = !hospitalSortAsc; renderHospitals(); };
window.addHospital = async function () {
    const name = document.getElementById("hospitalNameInput").value.trim();
    const cityId = document.getElementById("hospitalCitySelect").value;
    const districtId = document.getElementById("hospitalDistrictSelect").value;
    if (!name) return alert("Hastane adı girin!");
    if (!cityId) return alert("Şehir seçin!");
    const hospitalData = { name: name, city: { id: parseInt(cityId) } };
    if (districtId && districtId !== "") { hospitalData.district = { id: parseInt(districtId) }; }
    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals`, { method: "POST", body: JSON.stringify(hospitalData) });
        if (res && res.ok) {
            alert("Hastane eklendi");
            document.getElementById("hospitalNameInput").value = "";
            document.getElementById("hospitalDistrictSelect").innerHTML = '<option value="">İlçe Seç</option>';
            await loadHospitals();
        } else { alert("Hata: Hastane eklenemedi"); }
    } catch (error) { alert("Hastane eklenirken hata oluştu"); }
};
window.updateHospital = async function (id) {
    const nameInput = document.getElementById(`hospitalName_${id}`);
    if (!nameInput) return;
    const name = nameInput.value.trim();
    if (!name) return alert("Hastane adı boş olamaz!");
    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals/${id}`, { method: "PUT", body: JSON.stringify({ name: name }) });
        if (res && res.ok) { alert("Hastane güncellendi"); await loadHospitals(); }
        else { alert("Güncelleme başarısız"); }
    } catch (error) { alert("Güncelleme sırasında hata oluştu"); }
};
window.deleteHospital = async function (id) {
    if (!confirm("Bu hastaneyi silmek istediğinizden emin misiniz?")) return;
    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals/${id}`, { method: "DELETE" });
        if (res && res.ok) { alert("Hastane silindi"); await loadHospitals(); }
        else { alert("Silme başarısız"); }
    } catch (error) { alert("Silme sırasında hata oluştu"); }
};

// ================= CLINICS =================
let clinicsData = [], clinicSortAsc = true;
window.loadClinics = async function () {
    const res = await fetchWithAuth(`${API_URL}/clinics`);
    if (res) { clinicsData = await res.json(); renderClinics(); }
};
function renderClinics() {
    let filtered = clinicsData.filter(c => c.name.toLowerCase().includes((document.getElementById('clinicSearchInput')?.value || '').toLowerCase()));
    filtered.sort((a, b) => clinicSortAsc ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name));
    document.getElementById("clinicTableBody").innerHTML = filtered.map(c => `
        <tr>
            <td>${c.id}</td>
            <td><input type="text" id="clinicName_${c.id}" value="${c.name}" class="form-input"></td>
            <td>${c.hospital?.name || '-'}</td>
            <td class="action-buttons">
                <button class="btn-update" onclick="updateClinic(${c.id})">Güncelle</button>
                <button class="btn-delete" onclick="deleteClinic(${c.id})">Sil</button>
             </td>
        </tr>
    `).join("");
}
window.filterClinics = () => renderClinics();
window.sortClinics = () => { clinicSortAsc = !clinicSortAsc; renderClinics(); };
window.addClinic = async function () {
    const name = document.getElementById("clinicNameInput").value.trim();
    const hospitalId = document.getElementById("clinicHospitalSelect").value;
    if (!name || !hospitalId) return alert("Klinik adı ve hastane seçin!");
    const res = await fetchWithAuth(`${API_URL}/clinics`, { method: "POST", body: JSON.stringify({ name, hospital: { id: parseInt(hospitalId) } }) });
    if (res?.ok) { alert("Eklendi"); document.getElementById("clinicNameInput").value = ""; loadClinics(); }
};
window.updateClinic = async function (id) {
    const name = document.getElementById(`clinicName_${id}`).value.trim();
    const res = await fetchWithAuth(`${API_URL}/clinics/${id}`, { method: "PUT", body: JSON.stringify({ name }) });
    if (res?.ok) { alert("Güncellendi"); loadClinics(); }
};
window.deleteClinic = async function (id) {
    if (!confirm("Sil?")) return;
    const res = await fetchWithAuth(`${API_URL}/clinics/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadClinics(); }
};

// ================= DOCTORS =================
let doctorsData = [];
window.loadDoctors = async function () {
    console.log("🔵 Doktorlar yükleniyor...");
    try {
        const res = await fetchWithAuth(`${API_URL}/doctors`);
        if (res && res.ok) {
            doctorsData = await res.json();
            console.log("✅ Doktorlar yüklendi:", doctorsData.length);
            renderDoctors();
        } else {
            console.error("❌ Doktorlar yüklenemedi");
            doctorsData = [];
            renderDoctors();
        }
    } catch (error) {
        console.error("❌ Doktor yükleme hatası:", error);
        doctorsData = [];
        renderDoctors();
    }
};

function renderDoctors() {
    let searchTerm = (document.getElementById('doctorSearchInput')?.value || '').toLowerCase();

    let filtered = doctorsData.filter(d => {
        let fullName = `${d.user?.firstName || ''} ${d.user?.lastName || ''}`.toLowerCase();
        return fullName.includes(searchTerm);
    });

    const tbody = document.getElementById("doctorTableBody");
    if (!tbody) return;

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">📋 Doktor bulunamadı</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(d => {
        // Uzmanlık alanını düzenle
        let specialty = d.specialization || d.specialty || '-';
        if (specialty !== '-' && specialty !== 'Uzmanlık Belirtilmemiş') {
            // Uzmanlık varsa ilk 3 harfini al
            let shortSpec = specialty.length >= 3 ? specialty.substring(0, 3).toUpperCase() : specialty.toUpperCase();
            specialty = `${shortSpec}. Uzm.`;
        } else if (specialty === 'Uzmanlık Belirtilmemiş') {
            specialty = 'Uzm. Yok';
        }

        return `
            <tr>
                <td>${d.id}</td>
                <td>${escapeHtml(d.user?.firstName || '-')} ${escapeHtml(d.user?.lastName || '-')}</td>
                <td>${escapeHtml(specialty)}</td>
                <td>${escapeHtml(d.clinic?.hospital?.name || d.clinic?.name || '-')}</td>
                <td>${escapeHtml(d.clinic?.name || '-')}</td>
                <td class="action-buttons">
                    <button class="btn-delete" onclick="deleteDoctor(${d.id})">Sil</button>
                </td>
            </tr>
        `;
    }).join('');
}

window.filterDoctors = () => renderDoctors();
window.deleteDoctor = async function (id) {
    console.log("🔵 Silinecek ID:", id);

    if (!confirm(`ID: ${id} numaralı doktoru silmek istediğinizden emin misiniz?`)) return;

    try {
        const token = localStorage.getItem("token") || JSON.parse(localStorage.getItem("user"))?.token;

        const response = await fetch(`http://localhost:8080/api/doctors/${id}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        console.log("🔵 Response status:", response.status);

        if (response.ok) {
            const result = await response.json();
            console.log("🔵 Response:", result);
            alert("✅ Doktor başarıyla silindi!");
            await loadDoctors(); // Listeyi yenile
        } else {
            const error = await response.text();
            console.error("🔴 Hata:", error);
            alert("❌ Silinemedi! Status: " + response.status + "\n" + error);
        }
    } catch (error) {
        console.error("🔴 Silme hatası:", error);
        alert("❌ Hata: " + error.message);
    }
};
// ================= USERS, APPOINTMENTS, SLOTS =================
window.loadUsers = async function () {
    const res = await fetchWithAuth(`${API_URL}/users`);
    if (res) {
        const data = await res.json();
        document.getElementById("userTableBody").innerHTML = data.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${u.tckn}</td>
                <td>${u.role}</td>
                <td><button class="btn-delete" onclick="deleteUser(${u.id})">Sil</button></td>
            </tr>
        `).join("");
    }
};
window.deleteUser = async function (id) {
    if (!confirm("Sil?")) return;
    const res = await fetchWithAuth(`${API_URL}/users/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadUsers(); }
};
window.loadAppointments = async function () {
    const res = await fetchWithAuth(`${API_URL}/appointments`);
    if (res) {
        const data = await res.json();
        document.getElementById("appointmentTableBody").innerHTML = data.map(a => `
            <tr>
                <td>${a.id}</td>
                <td>${a.patient?.user?.firstName || '-'}</td>
                <td>${a.slot?.doctor?.user?.firstName || '-'}</td>
                <td>${a.status || '-'}</td>
                <td><button class="btn-delete" onclick="cancelAppointment(${a.id})">İptal</button></td>
            </tr>
        `).join("");
    }
};
window.cancelAppointment = async function (id) {
    await fetchWithAuth(`${API_URL}/appointments/${id}/cancel`, { method: "PUT" });
    loadAppointments();
};
window.loadSlots = async function () {
    const res = await fetchWithAuth(`${API_URL}/slots`);
    if (res) {
        const data = await res.json();
        document.getElementById("slotTableBody").innerHTML = data.map(s => `
            <tr>
                <td>${s.id}</td>
                <td>${s.doctor?.user?.firstName || '-'}</td>
                <td>${new Date(s.startTime).toLocaleString('tr-TR')}</td>
                <td><span class="status-badge ${s.available ? 'status-available' : 'status-reserved'}">${s.available ? "Boş" : "Dolu"}</span></td>
            </tr>
        `).join("");
    }
};

// ================= PROFILE =================
window.loadMyProfile = async function () {
    const res = await fetchWithAuth(`${API_URL}/users/me`);
    if (res) {
        const user = await res.json();
        document.getElementById("me-fullname").textContent = `${user.firstName || ''} ${user.lastName || ''}`.trim() || '-';
        document.getElementById("me-tckn").textContent = user.tckn || '-';
        document.getElementById("me-username").textContent = user.username || user.tckn;
        document.getElementById("me-role").textContent = user.role ? user.role.replace("ROLE_", "") : '-';
        document.getElementById("avatarInitial").textContent = (user.firstName || 'A').charAt(0).toUpperCase();
        document.getElementById("adminFullNameDisplay").textContent = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    }
};
window.handleUsernameUpdate = async function () {
    const newUsername = document.getElementById("new-username-input").value.trim();
    if (!newUsername) return alert("Kullanıcı adı girin!");
    const res = await fetchWithAuth(`${API_URL}/users/me`, { method: "PUT", body: JSON.stringify({ username: newUsername }) });
    if (res?.ok) { alert("Güncellendi! Tekrar giriş yapın."); logout(); }
};
window.handlePasswordUpdate = async function () {
    const current = document.getElementById("currentPassword").value;
    const newPass = document.getElementById("newPassword").value;
    const confirm = document.getElementById("confirmPassword").value;
    if (!current || !newPass) return alert("Şifreleri doldurun!");
    if (newPass !== confirm) return alert("Yeni şifreler eşleşmiyor!");
    const user = JSON.parse(localStorage.getItem("user"));
    const res = await fetch(`${API_URL}/auth/reset-password-logged-in`, {
        method: "POST", headers: { "Content-Type": "application/json", "Authorization": `Bearer ${user?.token}` },
        body: JSON.stringify({ tckn: user?.tckn, currentPassword: current, newPassword: newPass })
    });
    if (res.ok) { alert("Şifre güncellendi! Tekrar giriş yapın."); logout(); }
    else alert("Hata! Mevcut şifrenizi kontrol edin.");
};

// ================= HELPERS =================
async function loadCitiesForSelect(selectId) {
    try {
        const res = await fetchWithAuth(`${API_URL}/cities`);
        if (res && res.ok) {
            const cities = await res.json();
            const select = document.getElementById(selectId);
            if (select) {
                select.innerHTML = '<option value="">Şehir Seç</option>' + cities.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
            }
        }
    } catch (error) { console.error("Şehir yükleme hatası:", error); }
}
async function loadHospitalsForSelect(selectId) {
    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals`);
        if (res && res.ok) {
            const hospitals = await res.json();
            const select = document.getElementById(selectId);
            if (select) {
                select.innerHTML = '<option value="">Hastane Seç</option>' + hospitals.map(h => `<option value="${h.id}">${h.name} (${h.city?.name || '-'})</option>`).join('');
            }
        }
    } catch (error) { console.error("Hastane yükleme hatası:", error); }
}
async function loadDistrictsByCity(cityId, selectElement) {
    if (!cityId || cityId === "") { selectElement.innerHTML = '<option value="">İlçe Seç</option>'; return; }
    try {
        const res = await fetchWithAuth(`${API_URL}/cities/${cityId}/districts`);
        if (res && res.ok) {
            const districts = await res.json();
            selectElement.innerHTML = '<option value="">İlçe Seç</option>' + districts.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
        }
    } catch (error) { console.error("İlçe yükleme hatası:", error); }
}
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}
window.logout = () => { localStorage.clear(); window.location.href = "index.html"; };

// ================= DOCTOR ADD FUNCTION =================
window.addDoctor = async function () {
    const firstName = document.getElementById("doctorFirstName").value.trim();
    const lastName = document.getElementById("doctorLastName").value.trim();
    const specialty = document.getElementById("doctorSpecialty").value.trim();
    const clinicId = document.getElementById("doctorClinicSelect").value;
    if (!firstName) return alert("Doktor adı giriniz!");
    if (!lastName) return alert("Doktor soyadı giriniz!");
    if (!clinicId) return alert("Klinik seçiniz!");
    try {
        const params = new URLSearchParams();
        params.append("firstName", firstName);
        params.append("lastName", lastName);
        if (specialty) params.append("specialization", specialty);
        params.append("clinicId", clinicId);
        const token = localStorage.getItem("token") || JSON.parse(localStorage.getItem("user"))?.token;
        const response = await fetch(`http://localhost:8080/api/doctors/create-with-user?${params.toString()}`, {
            method: 'POST', headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const result = await response.json();
            alert(`✅ Doktor eklendi!\n👨‍⚕️ ${result.firstName} ${result.lastName}\n🔑 Kullanıcı Adı: ${result.username}\n🔐 Şifre: ${result.temporaryPassword}\n🏥 Klinik: ${result.clinicName}`);
            document.getElementById("doctorFirstName").value = "";
            document.getElementById("doctorLastName").value = "";
            document.getElementById("doctorSpecialty").value = "";
            document.getElementById("doctorHospitalSelect").value = "";
            document.getElementById("doctorClinicSelect").innerHTML = '<option value="">Önce hastane seçin</option>';
            await loadDoctors();
        } else { alert("❌ Doktor eklenemedi"); }
    } catch (error) { alert("Doktor eklenirken hata oluştu!"); }
};

// ================= DOMContentLoaded =================
document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadCities();
    loadDistricts();
    loadHospitals();
    loadClinics();
    loadDoctors();
    loadUsers();
    loadAppointments();
    loadSlots();
    loadMyProfile();

    const hospitalCitySelect = document.getElementById("hospitalCitySelect");
    if (hospitalCitySelect) {
        hospitalCitySelect.addEventListener("change", async function () {
            const districtSelect = document.getElementById("hospitalDistrictSelect");
            if (districtSelect) await loadDistrictsByCity(this.value, districtSelect);
        });
    }

    const doctorHospitalSelect = document.getElementById("doctorHospitalSelect");
    if (doctorHospitalSelect) {
        doctorHospitalSelect.addEventListener("change", async function () {
            const hospitalId = this.value;
            const clinicSelect = document.getElementById("doctorClinicSelect");
            if (!hospitalId) { clinicSelect.innerHTML = '<option value="">Önce hastane seçin</option>'; return; }
            try {
                const res = await fetchWithAuth(`${API_URL}/clinics/hospital/${hospitalId}`);
                if (res && res.ok) {
                    const clinics = await res.json();
                    clinicSelect.innerHTML = '<option value="">Klinik Seç</option>' + clinics.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
                } else {
                    const allClinicsRes = await fetchWithAuth(`${API_URL}/clinics`);
                    if (allClinicsRes && allClinicsRes.ok) {
                        const allClinics = await allClinicsRes.json();
                        const filtered = allClinics.filter(c => c.hospital?.id === parseInt(hospitalId));
                        clinicSelect.innerHTML = '<option value="">Klinik Seç</option>' + filtered.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
                    }
                }
            } catch (error) { console.error("Klinik yükleme hatası:", error); }
        });
    }
});