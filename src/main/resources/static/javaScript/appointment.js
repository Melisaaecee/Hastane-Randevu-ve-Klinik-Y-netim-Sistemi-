const BASE_URL = "http://localhost:8080/api";
// appointment.js en üst kısım
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
    fetchData('/cities', (data) => fillSelect(selects.city, data, "Şehir Seçin"));
});

// ZİNCİRLEME ETKİLEŞİM (Event Listeners)

selects.city.addEventListener('change', () => {
    resetSelects(['district', 'hospital', 'clinic', 'doctor']);
    if (selects.city.value) {
        fetchData(`/districts/city/${selects.city.value}`, (data) => fillSelect(selects.district, data, "İlçe Seçin"));
    }
});

selects.district.addEventListener('change', () => {
    resetSelects(['hospital', 'clinic', 'doctor']);
    if (selects.district.value) {
        fetchData(`/hospitals/district/${selects.district.value}`, (data) => fillSelect(selects.hospital, data, "Hastane Seçin"));
    }
});

selects.hospital.addEventListener('change', () => {
    resetSelects(['clinic', 'doctor']);
    if (selects.hospital.value) {
        fetchData(`/clinics/hospital/${selects.hospital.value}`, (data) => fillSelect(selects.clinic, data, "Klinik Seçin"));
    }
});

selects.clinic.addEventListener('change', () => {
    resetSelects(['doctor']);
    if (selects.clinic.value) {
        fetchData(`/doctors/clinic/${selects.clinic.value}`, (data) => fillSelect(selects.doctor, data, "Doktor Seçin"));
    }
});

selects.doctor.addEventListener('change', () => {
    slotSection.style.display = 'none';
    if (selects.doctor.value) {
        fetchData(`/slots/doctor/${selects.doctor.value}`, renderSlots);
    }
});

// YARDIMCI FONKSİYONLAR

async function fetchData(endpoint, callback) {
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        callback(data);
    } catch (error) {
        console.error("Veri çekme hatası:", error);
    }
}
function fillSelect(selectElement, data, placeholder) {
    selectElement.innerHTML = `<option value="">${placeholder}</option>`;
    
    data.forEach(item => {
        let name = "";
        
        // Eğer bu bir Doktor objesi ise (user alanı varsa)
        if (item.user && item.user.firstName) {
            name = ` ${item.user.firstName} ${item.user.lastName} (${item.specialization || 'Uzm.'})`;
        } 
        // Eğer City/Hospital/Clinic gibi düz isim içeren bir objeyse
        else {
            name = item.name || "İsimsiz Kayıt";
        }

        selectElement.innerHTML += `<option value="${item.id}">${name}</option>`;
    });
    
    selectElement.disabled = false;
}

function resetSelects(keys) {
    keys.forEach(key => {
        selects[key].innerHTML = `<option value="">Bekleniyor...</option>`;
        selects[key].disabled = true;
    });
    slotSection.style.display = 'none';
    confirmBtn.style.display = 'none';
    selectedSlotId = null;
}

function renderSlots(slots) {
    slotGrid.innerHTML = '';
    // appointment.js içindeki hata mesajını basan yeri bul ve şununla değiştir:
if (slots.length === 0) {
    slotGrid.innerHTML = `
        <div class="no-slot-msg">
            <i class="fas fa-exclamation-circle"></i>
            Bu doktor için şu an uygun randevu saati bulunmamaktadır.
        </div>
    `;
    confirmBtn.style.display = 'none'; // Butonu da gizlemiş olursun
}
     else {
        slots.forEach(slot => {
            const date = new Date(slot.startTime);
            const timeStr = date.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
            
            const btn = document.createElement('button');
            btn.className = 'slot-btn';
            btn.innerHTML = `<i class="far fa-clock"></i> ${timeStr}`;
            btn.onclick = () => {
                document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                selectedSlotId = slot.id;
                confirmBtn.style.display = 'block';
            };
            slotGrid.appendChild(btn);
        });
    }
    slotSection.style.display = 'block';
}

// RANDEVU ONAYI (POST)
confirmBtn.addEventListener('click', async () => {
    // LocalStorage'dan hasta ID'sini almalısın (Login sırasında set etmiş olmalısın)
    const currentPatientId = authData?.user?.id;
    if (!currentPatientId|| !selectedSlotId) {
        console.log("Eksik veri:", { currentPatientId, selectedSlotId });
        alert("Lütfen tüm seçimleri yapın!");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/appointments?patientId=${currentPatientId}&slotId=${selectedSlotId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json(); // Backend'den dönen JSON'u (başarılı veya hatalı) alıyoruz

       // appointment.js içindeki fetch(save_appointment) bloğunun başarı (ok) kısmı:
if (response.ok) {
    alert("Randevunuz başarıyla oluşturuldu!");
    // Ana sayfadaki tabloyu güncelle ve oraya odaklan
    if (window.parent && typeof window.parent.showSection === 'function') {
        window.parent.showSection('appointments'); 
        window.parent.filterAppointments('all');
    }
    else {
        location.reload(); // Eğer iframe içinde değilse sayfayı yenile
        }

}   else {
            // --- BURASI KRİTİK: Ceza veya Çakışma mesajını gösterir ---
            // Backend'den gelen 'BadRequestException' mesajını alert olarak basıyoruz
            alert(data.message || "Randevu oluşturulamadı.");
        }

    } catch (error) {
        console.error("Hata detayı:", error);
        alert("Sunucuyla bağlantı kurulamadı veya sistem hatası oluştu.");
    }
});