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
window.switchTab = async function (tabId, event) {
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
    if (tabId === 'districts') { await loadCitiesForSelect('districtCitySelect'); loadDistricts(); }
    if (tabId === 'hospitals') {
        await loadCitiesForSelect('hospitalCitySelect');
        await loadHospitals();
    }
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

    tbody.innerHTML = filtered.map(h => {
        // Şehir bilgisini district üzerinden al
        const cityName = h.district?.city?.name || h.city?.name || '-';
        const districtName = h.district?.name || '-';

        return `
        <tr>
            <td>${h.id || '-'}</td>
            <td><input type="text" id="hospitalName_${h.id}" value="${escapeHtml(h.name || '')}" class="form-input" style="width:100%"></td>
            <td>${escapeHtml(cityName)}</span></td>
            <td>${escapeHtml(districtName)}</span></td>
            <td class="action-buttons">
                <button class="btn-update" onclick="updateHospital(${h.id})">Güncelle</button>
                <button class="btn-delete" onclick="deleteHospital(${h.id})">Sil</button>
            </span>
        </span>
    `}).join('');
}

window.filterHospitals = () => renderHospitals();

window.sortHospitals = () => { hospitalSortAsc = !hospitalSortAsc; renderHospitals(); };
window.addHospital = async function () {
    const name = document.getElementById("hospitalNameInput").value.trim();
    const cityId = document.getElementById("hospitalCitySelect").value;
    const districtId = document.getElementById("hospitalDistrictSelect").value;

    if (!name) return alert("Hastane adı girin!");
    if (!cityId) return alert("Şehir seçin!");
    if (!districtId) return alert("İlçe seçin!");  // ← İLÇE ZORUNLU

    const hospitalData = {
        name: name,
        district: { id: parseInt(districtId) }  // ← district objesi gönder
    };

    try {
        const res = await fetchWithAuth(`${API_URL}/hospitals`, {
            method: "POST",
            body: JSON.stringify(hospitalData)
        });

        if (res && res.ok) {
            alert("Hastane eklendi");
            document.getElementById("hospitalNameInput").value = "";
            document.getElementById("hospitalCitySelect").value = "";
            document.getElementById("hospitalDistrictSelect").innerHTML = '<option value="">İlçe Seç</option>';
            await loadHospitals();
        } else {
            const errorText = await res?.text();
            alert("Hata: " + (errorText || "Hastane eklenemedi"));
        }
    } catch (error) {
        console.error("Ekleme hatası:", error);
        alert("Hastane eklenirken bir hata oluştu");
    }
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

    try {
        const res = await fetchWithAuth(`${API_URL}/doctors`);
        if (res && res.ok) {
            doctorsData = await res.json();
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
        // AD SOYAD: Sadece "Ad Soyad" olarak göster (ünvan ekleme)
        // Backend'den gelen firstName "Uzm. Dr. Ahmet" formatında olabilir
        // Biz sadece isim ve soyadı alalım
        const firstName = d.user?.firstName || '';
        const lastName = d.user?.lastName || '';

        // İsimden sadece adı al (ünvanı temizle)
        let cleanFirstName = firstName;
        // Eğer "Uzm. Dr. Ahmet" gibi bir format varsa, sadece "Ahmet" kısmını al
        if (cleanFirstName.includes('Dr.')) {
            // "Uzm. Dr. Ahmet" -> "Ahmet"
            const parts = cleanFirstName.split('Dr.');
            if (parts.length > 1) {
                cleanFirstName = parts[1].trim();
            }
        }

        const fullName = `${cleanFirstName} ${lastName}`.trim();

        // UZMANLIK: Ünvanı göster (Uzm, Op, Prof, Doç)
        let specialty = d.specialization || '';
        let displayTitle = '';
        if (specialty && specialty !== '' && specialty !== 'Uzmanlık Belirtilmemiş') {
            displayTitle = specialty;  // "Uzm", "Op", "Prof", "Doç"
        } else {
            displayTitle = '-';
        }

        // HASTANE ve KLİNİK
        const hospitalName = d.clinic?.hospital?.name || d.clinic?.name || '-';
        const clinicName = d.clinic?.name || '-';

        return `
            <tr>
                <td>${d.id}</td>
                <td>${escapeHtml(fullName)}</td>
                <td>${escapeHtml(displayTitle)}</td>
                <td>${escapeHtml(hospitalName)}</td>
                <td>${escapeHtml(clinicName)}</td>
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
            await loadDoctors();
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
        document.getElementById("slotTableBody").innerHTML = data.map(s => {
            // DOKTOR ADINI DOĞRU ŞEKİLDE AL
            let doctorName = '-';
            if (s.doctor) {
                // Önce doctor.user üzerinden dene
                if (s.doctor.user) {
                    const firstName = s.doctor.user.firstName || '';
                    const lastName = s.doctor.user.lastName || '';
                    doctorName = `${firstName} ${lastName}`.trim();
                }
                // Alternatif: s.doctor.firstName, s.doctor.lastName direkt varsa
                else if (s.doctor.firstName || s.doctor.lastName) {
                    doctorName = `${s.doctor.firstName || ''} ${s.doctor.lastName || ''}`.trim();
                }
            }

            // ZAMAN formatını düzelt (saat dakika saniye)
            let formattedTime = '-';
            if (s.startTime) {
                const date = new Date(s.startTime);
                formattedTime = date.toLocaleString('tr-TR', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                });
            }

            // DURUM
            const statusText = s.available ? "Boş" : "Dolu";
            const statusClass = s.available ? 'status-available' : 'status-reserved';

            return `
                <tr>
                    <td>${s.id}</td>
                    <td>${escapeHtml(doctorName)}</span>
                    <td>${formattedTime}</span>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></span>
                </tr>
            `;
        }).join('');
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
// ================= İLÇE YÜKLEME FONKSİYONU =================
async function loadDistrictsByCity(cityId, selectElement) {
    if (!cityId || cityId === "") {
        selectElement.innerHTML = '<option value="">İlçe Seç</option>';
        return;
    }

    try {

        const res = await fetchWithAuth(`${API_URL}/districts/city/${cityId}`);

        if (res && res.ok) {
            const districts = await res.json();
            if (districts && districts.length > 0) {
                selectElement.innerHTML = '<option value="">İlçe Seç</option>' +
                    districts.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join('');
            } else {
                selectElement.innerHTML = '<option value="">Bu şehirde ilçe bulunamadı</option>';
            }
        } else {
            selectElement.innerHTML = '<option value="">İlçe yüklenemedi</option>';
        }
    } catch (error) {
        console.error("İlçe yükleme hatası:", error);
        selectElement.innerHTML = '<option value="">İlçe yüklenemedi</option>';
    }
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
            const cityId = this.value;
            const districtSelect = document.getElementById("hospitalDistrictSelect");

            if (!districtSelect) return;

            if (!cityId || cityId === "") {
                districtSelect.innerHTML = '<option value="">Önce şehir seçin</option>';
                return;
            }

            await loadDistrictsByCity(cityId, districtSelect);
        });
    }

    const doctorHospitalSelect = document.getElementById("doctorHospitalSelect");
    if (doctorHospitalSelect) {
        doctorHospitalSelect.addEventListener("change", async function () {
            const hospitalId = this.value;
            const clinicSelect = document.getElementById("doctorClinicSelect");
            if (!hospitalId) {
                clinicSelect.innerHTML = '<option value="">Önce hastane seçin</option>';
                return;
            }
            try {
                const res = await fetchWithAuth(`${API_URL}/clinics/hospital/${hospitalId}`);
                if (res && res.ok) {
                    const clinics = await res.json();
                    clinicSelect.innerHTML = '<option value="">Klinik Seç</option>' +
                        clinics.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
                } else {
                    const allClinicsRes = await fetchWithAuth(`${API_URL}/clinics`);
                    if (allClinicsRes && allClinicsRes.ok) {
                        const allClinics = await allClinicsRes.json();
                        const filtered = allClinics.filter(c => c.hospital?.id === parseInt(hospitalId));
                        clinicSelect.innerHTML = '<option value="">Klinik Seç</option>' +
                            filtered.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
                    }
                }
            } catch (error) {
                console.error("Klinik yükleme hatası:", error);
            }
        });
    }
});