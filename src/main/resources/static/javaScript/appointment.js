// Sayfa yüklendiğinde çalışacak ana tetikleyici
document.addEventListener('DOMContentLoaded', () => {
    window.initializeAppointmentPage();
});

// Sayfa Başlatma Fonksiyonu
window.initializeAppointmentPage = function() {
    const user = JSON.parse(localStorage.getItem('user'));
    
    // Oturum kontrolü
    if (!user) {
        window.location.href = 'index.html';
        return;
    }

    // Seçim kutularını (select) dinlemeye başla
    const clinicSelect = document.getElementById('clinicSelect');
    if (clinicSelect) {
        clinicSelect.addEventListener('change', window.handleClinicChange);
    }

    // Formu dinlemeye başla
    const appointmentForm = document.getElementById('appointmentForm');
    if (appointmentForm) {
        appointmentForm.addEventListener('submit', window.handleAppointmentSubmit);
    }
};

// Poliklinik değiştiğinde doktorları yükleyen fonksiyon
window.handleClinicChange = function(e) {
    const selectedClinic = e.target.value;
    const doctorSelect = document.getElementById('doctorSelect');
    
    // Örnek veri seti (Backend gelene kadar)
    const doctorsByClinic = {
        "Kardiyoloji": ["Prof. Dr. Ahmet Yılmaz", "Doç. Dr. Ayşe Kaya"],
        "Dahiliye": ["Uzm. Dr. Mehmet Öz", "Dr. Fatma Şahin"],
        "Göz Hastalıkları": ["Dr. Caner Bulut", "Prof. Dr. Elif Akın"],
        "Nöroloji": ["Doç. Dr. Serdar Tekin"]
    };

    const doctors = doctorsByClinic[selectedClinic] || [];

    // Listeyi temizle ve yeni doktorları ekle
    doctorSelect.innerHTML = '<option value="" disabled selected>Doktor seçiniz...</option>';
    
    doctors.forEach(doc => {
        const option = document.createElement('option');
        option.value = doc;
        option.textContent = doc;
        doctorSelect.appendChild(option);
    });

    doctorSelect.disabled = false;
};

// Randevu formunu gönderen fonksiyon
window.handleAppointmentSubmit = async function(e) {
    e.preventDefault();

    const user = JSON.parse(localStorage.getItem('user'));
    const clinic = document.getElementById('clinicSelect').value;
    const doctor = document.getElementById('doctorSelect').value;
    const date = document.getElementById('appointmentDate').value;

    const payload = {
        patientId: user.id,
        clinicName: clinic,
        doctorName: doctor,
        appointmentDate: date,
        status: "ONAYLANDI" // Varsayılan durum
    };

    try {
        const response = await fetch('http://localhost:8080/api/appointments/create', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Randevunuz başarıyla sisteme kaydedildi!");
            window.location.href = 'patient.html';
        } else {
            const errorData = await response.json();
            window.showErrorMessage(errorData.message || "Randevu alınamadı.");
        }
    } catch (err) {
        console.error("Bağlantı hatası:", err);
        // Backend çalışmıyorsa test amaçlı local kaydı tetikle
        window.saveToLocalMock(payload);
    }
};

// Hata mesajı gösterme fonksiyonu
window.showErrorMessage = function(msg) {
    const errorBox = document.getElementById('errorBox');
    if (errorBox) {
        errorBox.textContent = msg;
        errorBox.style.display = 'block';
    } else {
        alert(msg);
    }
};

// Backend kapalıyken çalışacak simülasyon fonksiyonu
window.saveToLocalMock = function(data) {
    let appointments = JSON.parse(localStorage.getItem('appointments') || '[]');
    appointments.push(data);
    localStorage.setItem('appointments', JSON.stringify(appointments));
    
    alert("Dikkat: Sunucuya bağlanılamadı. Veri tarayıcı hafızasına (Local) kaydedildi.");
    window.location.href = 'patient.html';
};