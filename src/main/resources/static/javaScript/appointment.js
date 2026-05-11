const authData = JSON.parse(localStorage.getItem('user'));
const token = authData ? authData.token : null;
const patientId = authData ? authData.user.id : null;

if (!token) {
    console.error("Token bulunamadı!");
}

// Element tanımlamaları
const selects = {
    city: document.getElementById('citySelect'),
    district: document.getElementById('districtSelect'),
    hospital: document.getElementById('hospitalSelect'),
    clinic: document.getElementById('clinicSelect'),
    doctor: document.getElementById('doctorSelect')
};
const slotSection = document.getElementById('slotSection');
const slotGrid = document.getElementById('slotGrid');
const confirmBtn = document.getElementById('confirmBtn');

let selectedSlotId = null;

// Sayfa yüklendiğinde Şehirleri getir
document.addEventListener('DOMContentLoaded', () => {
    fetchData('https://medsoft.up.railway.app/api/cities', (data) => fillSelect(selects.city, data, "Şehir Seçin"));
});

// ZİNCİRLEME ETKİLEŞİM (Event Listeners)

selects.city.addEventListener('change', () => {
    resetSelects(['district', 'hospital', 'clinic', 'doctor']);
    if (selects.city.value) {
        fetchData(`https://medsoft.up.railway.app/api/districts/city/${selects.city.value}`, (data) => fillSelect(selects.district, data, "İlçe Seçin"));
    }
});

selects.district.addEventListener('change', () => {
    resetSelects(['hospital', 'clinic', 'doctor']);
    if (selects.district.value) {
        fetchData(`https://medsoft.up.railway.app/api/hospitals/district/${selects.district.value}`, (data) => fillSelect(selects.hospital, data, "Hastane Seçin"));
    }
});

selects.hospital.addEventListener('change', () => {
    resetSelects(['clinic', 'doctor']);
    if (selects.hospital.value) {
        fetchData(`https://medsoft.up.railway.app/api/clinics/hospital/${selects.hospital.value}`, (data) => fillSelect(selects.clinic, data, "Klinik Seçin"));
    }
});

selects.clinic.addEventListener('change', () => {
    resetSelects(['doctor']);
    if (selects.clinic.value) {
        fetchData(`https://medsoft.up.railway.app/api/doctors/clinic/${selects.clinic.value}`, (data) => fillSelect(selects.doctor, data, "Doktor Seçin"));
    }
});

selects.doctor.addEventListener('change', () => {
    if (selects.doctor.value) {
        loadSlots(selects.doctor.value);
    }
});

// API Çağrısı
async function fetchData(endpoint, callback) {
    try {
        const response = await fetch(endpoint, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        callback(data);
    } catch (error) {
        console.error("Veri çekme hatası:", error);
        callback([]);
    }
}

function fillSelect(select, data, defaultText) {
    select.innerHTML = `<option value="">${defaultText}</option>`;
    if (!data || data.length === 0) {
        select.disabled = true;
        return;
    }
    select.disabled = false;
    data.forEach(item => {
        const name = item.name || item.fullName || `${item.firstName || ''} ${item.lastName || ''}`.trim();
        const option = document.createElement('option');
        option.value = item.id;
        option.textContent = name;
        select.appendChild(option);
    });
}

function resetSelects(ids) {
    ids.forEach(id => {
        const select = selects[id];
        if (select) {
            select.innerHTML = `<option value="">${getDefaultText(id)}</option>`;
            select.disabled = true;
        }
    });
    hideSlots();
}

function getDefaultText(id) {
    const texts = {
        district: "İlçe Seçin",
        hospital: "Hastane Seçin",
        clinic: "Klinik Seçin",
        doctor: "Doktor Seçin"
    };
    return texts[id] || "Seçin";
}

async function loadSlots(doctorId) {
    try {
        const response = await fetch(`https://medsoft.up.railway.app/api/slots/doctor/${doctorId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error("Slotlar alınamadı");
        const slots = await response.json();
        displaySlots(slots);
    } catch (error) {
        console.error("Slot hatası:", error);
        showToast("Slotlar yüklenirken hata oluştu", "error");
    }
}

function displaySlots(slots) {
    if (!slots || slots.length === 0) {
        slotGrid.innerHTML = '<p style="text-align:center;">Bu doktor için müsait slot bulunamadı.</p>';
        slotSection.style.display = 'block';
        confirmBtn.style.display = 'none';
        return;
    }

    const availableSlots = slots.filter(slot => slot.status === 'AVAILABLE');
    if (availableSlots.length === 0) {
        slotGrid.innerHTML = '<p style="text-align:center;">Bu doktorun tüm slotları dolu.</p>';
        slotSection.style.display = 'block';
        confirmBtn.style.display = 'none';
        return;
    }

    slotGrid.innerHTML = '';
    availableSlots.forEach(slot => {
        const startTime = new Date(slot.startTime);
        const formattedTime = startTime.toLocaleString('tr-TR', {
            day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'
        });
        const button = document.createElement('button');
        button.className = 'slot-btn';
        button.textContent = formattedTime;
        button.onclick = () => selectSlot(slot.id, button);
        slotGrid.appendChild(button);
    });

    slotSection.style.display = 'block';
    confirmBtn.style.display = 'block';
}

function selectSlot(slotId, button) {
    document.querySelectorAll('.slot-btn').forEach(btn => btn.classList.remove('selected'));
    button.classList.add('selected');
    selectedSlotId = slotId;
}

function hideSlots() {
    slotSection.style.display = 'none';
    confirmBtn.style.display = 'none';
    selectedSlotId = null;
}

confirmBtn.addEventListener('click', async () => {
    if (!selectedSlotId) {
        alert("Lütfen bir saat seçin!");
        return;
    }

    const appointmentData = {
        patient: { id: patientId },
        slot: { id: selectedSlotId },
        status: "SCHEDULED"
    };

    try {
        const response = await fetch(`https://medsoft.up.railway.app/api/appointments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(appointmentData)
        });

        if (response.ok) {
            showToast("✅ Randevunuz başarıyla oluşturuldu!", "success");
            setTimeout(() => {
                window.parent.location.href = 'patient.html';
            }, 2000);
        } else {
            const error = await response.text();
            showToast("❌ Randevu oluşturulamadı: " + error, "error");
        }
    } catch (error) {
        console.error("Randevu hatası:", error);
        showToast("Bağlantı hatası!", "error");
    }
});

function showToast(message, type) {
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);

        const style = document.createElement('style');
        style.textContent = `
            .toast-container {
                position: fixed;
                bottom: 20px;
                left: 50%;
                transform: translateX(-50%);
                z-index: 9999;
                animation: slideUp 0.3s ease;
            }
            .toast-content {
                background: #1e293b;
                color: white;
                padding: 12px 24px;
                border-radius: 12px;
                display: flex;
                align-items: center;
                gap: 12px;
                box-shadow: 0 10px 25px -5px rgba(0,0,0,0.2);
            }
            .toast-content i {
                font-size: 1.2rem;
            }
            @keyframes slideUp {
                from { opacity: 0; transform: translate(-50%, 20px); }
                to { opacity: 1; transform: translate(-50%, 0); }
            }
        `;
        document.head.appendChild(style);
    }

    toastContainer.innerHTML = `
        <div class="toast-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}" style="color: ${type === 'success' ? '#10b981' : '#ef4444'}"></i>
            <span>${message}</span>
        </div>
    `;

    setTimeout(() => {
        if (toastContainer) toastContainer.innerHTML = '';
    }, 3000);
}