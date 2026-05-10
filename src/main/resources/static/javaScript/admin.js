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
    if (tabId === 'doctors') { loadClinicsForSelect('doctorClinicSelect'); loadDoctors(); }
    if (tabId === 'users') loadUsers();
    if (tabId === 'appointments') loadAppointments();
    if (tabId === 'slots') loadSlots();
    if (tabId === 'profile') loadMyProfile();
};

// ================= DASHBOARD =================
window.loadStats = async function () {
    const [u, h, d, a, c] = await Promise.all([
        fetchWithAuth(`${API_URL}/users`), fetchWithAuth(`${API_URL}/hospitals`),
        fetchWithAuth(`${API_URL}/doctors`), fetchWithAuth(`${API_URL}/appointments`),
        fetchWithAuth(`${API_URL}/clinics`)
    ]);
    if (u) document.getElementById("count-users").textContent = (await u.json()).length;
    if (h) document.getElementById("count-hospitals").textContent = (await h.json()).length;
    if (d) document.getElementById("count-doctors").textContent = (await d.json()).length;
    if (a) document.getElementById("count-appointments").textContent = (await a.json()).length;
    if (c) document.getElementById("count-clinics").textContent = (await c.json()).length;
};

// ================= CITIES (CRUD + SEARCH + SORT) =================
let citiesData = [], citySortAsc = true;
window.loadCities = async function () {
    const res = await fetchWithAuth(`${API_URL}/cities`);
    if (res) { citiesData = await res.json(); renderCities(); }
};

function renderCities() {
    let filtered = citiesData.filter(c => c.name.toLowerCase().includes((document.getElementById('citySearchInput')?.value || '').toLowerCase()));
    filtered.sort((a, b) => citySortAsc ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name));
    document.getElementById("cityTableBody").innerHTML = filtered.map(c => `
                <tr><td>${c.id}</td><td><input type="text" id="cityName_${c.id}" value="${c.name}" class="form-input" style="width:100%"></td>
                <td class="action-buttons">
    <button class="btn-update" onclick="updateCity(${c.id})">Güncelle</button>
    <button class="btn-delete" onclick="deleteCity(${c.id})">Sil</button>
</td></tr>
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
                <tr><td>${d.id}</td><td><input type="text" id="districtName_${d.id}" value="${d.name}" class="form-input"></td>
                <td>${d.city?.name || '-'}</td>
                <td class="action-buttons"><button class="btn-primary" style="padding:6px 12px;width:auto" onclick="updateDistrict(${d.id})">Güncelle</button>
                <button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="deleteDistrict(${d.id})">Sil</button></td></tr>
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
    const res = await fetchWithAuth(`${API_URL}/hospitals`);
    if (res) { hospitalsData = await res.json(); renderHospitals(); }
};
function renderHospitals() {
    let filtered = hospitalsData.filter(h => h.name.toLowerCase().includes((document.getElementById('hospitalSearchInput')?.value || '').toLowerCase()));
    filtered.sort((a, b) => hospitalSortAsc ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name));
    document.getElementById("hospitalTableBody").innerHTML = filtered.map(h => `
                <tr><td>${h.id}</td><td><input type="text" id="hospitalName_${h.id}" value="${h.name}" class="form-input"></td>
                <td>${h.city?.name || '-'}</td><td>${h.district?.name || '-'}</td>
                <td class="action-buttons"><button class="btn-primary" style="padding:6px 12px;width:auto" onclick="updateHospital(${h.id})">Güncelle</button>
                <button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="deleteHospital(${h.id})">Sil</button></td></tr>
            `).join("");
}
window.filterHospitals = () => renderHospitals();
window.sortHospitals = () => { hospitalSortAsc = !hospitalSortAsc; renderHospitals(); };
window.addHospital = async function () {
    const name = document.getElementById("hospitalNameInput").value.trim();
    const cityId = document.getElementById("hospitalCitySelect").value;
    const districtId = document.getElementById("hospitalDistrictSelect").value;
    if (!name || !cityId) return alert("Hastane adı ve şehir girin!");
    const res = await fetchWithAuth(`${API_URL}/hospitals`, { method: "POST", body: JSON.stringify({ name, city: { id: parseInt(cityId) }, district: districtId ? { id: parseInt(districtId) } : null }) });
    if (res?.ok) { alert("Eklendi"); document.getElementById("hospitalNameInput").value = ""; loadHospitals(); }
};
window.updateHospital = async function (id) {
    const name = document.getElementById(`hospitalName_${id}`).value.trim();
    const res = await fetchWithAuth(`${API_URL}/hospitals/${id}`, { method: "PUT", body: JSON.stringify({ name }) });
    if (res?.ok) { alert("Güncellendi"); loadHospitals(); }
};
window.deleteHospital = async function (id) {
    if (!confirm("Sil?")) return;
    const res = await fetchWithAuth(`${API_URL}/hospitals/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadHospitals(); }
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
                <tr><td>${c.id}</td><td><input type="text" id="clinicName_${c.id}" value="${c.name}" class="form-input"></td>
                <td>${c.hospital?.name || '-'}</td>
                <td class="action-buttons"><button class="btn-primary" style="padding:6px 12px;width:auto" onclick="updateClinic(${c.id})">Güncelle</button>
                <button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="deleteClinic(${c.id})">Sil</button></td></tr>
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
    const res = await fetchWithAuth(`${API_URL}/doctors`);
    if (res) { doctorsData = await res.json(); renderDoctors(); }
};
function renderDoctors() {
    let filtered = doctorsData.filter(d =>
        (d.user?.firstName + ' ' + d.user?.lastName).toLowerCase().includes((document.getElementById('doctorSearchInput')?.value || '').toLowerCase())
    );
    document.getElementById("doctorTableBody").innerHTML = filtered.map(d => `
                <tr><td>${d.id}</td><td>${d.user?.firstName || '-'} ${d.user?.lastName || '-'}</td>
                <td>${d.specialty || '-'}</td><td>${d.clinic?.name || '-'}</td>
                <td class="action-buttons"><button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="deleteDoctor(${d.id})">Sil</button></td></tr>
            `).join("");
}
window.filterDoctors = () => renderDoctors();
window.addDoctor = async function () {
    const firstName = document.getElementById("doctorFirstName").value.trim();
    const lastName = document.getElementById("doctorLastName").value.trim();
    const specialty = document.getElementById("doctorSpecialty").value.trim();
    const clinicId = document.getElementById("doctorClinicSelect").value;
    if (!firstName || !clinicId) return alert("Ad ve klinik seçin!");
    const res = await fetchWithAuth(`${API_URL}/doctors`, {
        method: "POST",
        body: JSON.stringify({ firstName, lastName, specialty, clinic: { id: parseInt(clinicId) } })
    });
    if (res?.ok) { alert("Eklendi"); loadDoctors(); }
};
window.deleteDoctor = async function (id) {
    if (!confirm("Sil?")) return;
    const res = await fetchWithAuth(`${API_URL}/doctors/${id}`, { method: "DELETE" });
    if (res?.ok) { alert("Silindi"); loadDoctors(); }
};

// ================= USERS, APPOINTMENTS, SLOTS =================
window.loadUsers = async function () {
    const res = await fetchWithAuth(`${API_URL}/users`);
    if (res) {
        const data = await res.json();
        document.getElementById("userTableBody").innerHTML = data.map(u => `
                    <tr><td>${u.id}</td><td>${u.tckn}</td><td>${u.role}</td>
                    <td><button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="deleteUser(${u.id})">Sil</button></td></tr>
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
                    <tr><td>${a.id}</td><td>${a.patient?.user?.firstName || '-'}</td>
                    <td>${a.slot?.doctor?.user?.firstName || '-'}</td><td>${a.status || '-'}</td>
                    <td><button class="btn-primary" style="padding:6px 12px;width:auto;background:#ef4444" onclick="cancelAppointment(${a.id})">İptal</button></td></tr>
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
                    <tr><td>${s.id}</td><td>${s.doctor?.user?.firstName || '-'}</td>
                    <td>${new Date(s.startTime).toLocaleString('tr-TR')}</td>
                    <td><span class="status-badge ${s.available ? 'status-available' : 'status-reserved'}">${s.available ? "Boş" : "Dolu"}</span></td></tr>
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
    const res = await fetchWithAuth(`${API_URL}/cities`);
    if (res) {
        const cities = await res.json();
        const select = document.getElementById(selectId);
        if (select) select.innerHTML = '<option value="">Şehir Seç</option>' + cities.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }
}
async function loadHospitalsForSelect(selectId) {
    const res = await fetchWithAuth(`${API_URL}/hospitals`);
    if (res) {
        const hospitals = await res.json();
        const select = document.getElementById(selectId);
        if (select) select.innerHTML = '<option value="">Hastane Seç</option>' + hospitals.map(h => `<option value="${h.id}">${h.name}</option>`).join('');
    }
}
async function loadClinicsForSelect(selectId) {
    const res = await fetchWithAuth(`${API_URL}/clinics`);
    if (res) {
        const clinics = await res.json();
        const select = document.getElementById(selectId);
        if (select) select.innerHTML = '<option value="">Klinik Seç</option>' + clinics.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }
}
document.getElementById("hospitalCitySelect")?.addEventListener("change", async function () {
    const cityId = this.value;
    if (cityId) {
        const res = await fetchWithAuth(`${API_URL}/cities/${cityId}/districts`);
        if (res) {
            const districts = await res.json();
            const districtSelect = document.getElementById("hospitalDistrictSelect");
            districtSelect.innerHTML = '<option value="">İlçe Seç</option>' + districts.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
        }
    }
});

window.logout = () => { localStorage.clear(); window.location.href = "index.html"; };
document.addEventListener("DOMContentLoaded", () => { loadStats(); loadCities(); loadDistricts(); loadHospitals(); loadClinics(); loadDoctors(); loadUsers(); loadAppointments(); loadSlots(); loadMyProfile(); });