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




function showToast(message, type = 'success') {
    const toast = document.getElementById('toastNotification');
    const msgSpan = document.getElementById('toastMessage');
    const icon = document.getElementById('toastIcon');

    if (!toast || !msgSpan || !icon) return; 

    msgSpan.innerText = message;
    
    // Sınıfları temizle ve yeni tipi ekle
    toast.className = 'toast-container';
    toast.classList.add(type === 'success' ? 'toast-success' : 'toast-error');
    
    // İkonu ayarla
    icon.className = type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-triangle';

    // Göster
    toast.classList.add('show');

    // 3 saniye sonra gizle
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

/**
 * RANDEVU ONAYI (POST)
 */
confirmBtn.addEventListener('click', async () => {
    const currentPatientId = authData?.user?.id;
    
    // Basit bir kontrol: ID veya Slot seçimi yoksa durdur
    if (!currentPatientId || !selectedSlotId) {
        showToast("Lütfen önce bir randevu saati seçiniz!", "error");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/appointments?userId=${currentPatientId}&slotId=${selectedSlotId}`, {
            method: 'POST',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json' 
            }
        });

        // Backend'den gelen cevabı JSON olarak al
        const data = await response.json();

        if (response.ok) {
            // BAŞARILI DURUM
            showToast("✅ Randevunuz başarıyla oluşturuldu!", "success");

            // Kullanıcı bildirimi görsün diye 2 saniye bekleyip yönlendiriyoruz
            setTimeout(() => {
                if (window.parent && typeof window.parent.showSection === 'function') {
                    // Ana sayfadaki Randevularım sekmesine geç
                    window.parent.showSection('appointments'); 
                    // Tabloyu güncelle
                    if (typeof window.parent.filterAppointments === 'function') {
                        window.parent.filterAppointments('all');
                    }
                } else {
                    location.reload(); 
                }
            }, 2000);

        } else {
            // HATALI DURUM (Backend'den gelen mesaj: Ceza, Çakışma vb.)
            showToast(data.message || "Randevu oluşturulamadı.", "error");
        }

    } catch (error) {
        console.error("Hata detayı:", error);
        showToast("🚀 Sunucuyla bağlantı kurulamadı veya sistem hatası.", "error");
    }
});

