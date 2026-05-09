// Sayfa yüklendiğinde bugünün tarihini minimum yap
document.getElementById('appointmentDate').min = new Date().toISOString().split("T")[0];

const doctorData = {
    kardiyoloji: ["Dr. Mehmet Öz", "Dr. Canan Karatay"],
    goz: ["Dr. Sinan Akçıl", "Dr. Merve Aydın"],
    dahiliye: ["Dr. Ali Veli", "Dr. Ayşe Yılmaz"],
    ortopedi: ["Dr. Hasan Tahsin"]
};

function loadDoctors() {
    const clinic = document.getElementById('clinicSelect').value;
    const docSelect = document.getElementById('doctorSelect');
    
    docSelect.innerHTML = '<option value="">Doktor Seçiniz...</option>';
    
    if (clinic) {
        docSelect.disabled = false;
        doctorData[clinic].forEach(doc => {
            let opt = document.createElement('option');
            opt.value = doc;
            opt.innerHTML = doc;
            docSelect.appendChild(opt);
        });
    } else {
        docSelect.disabled = true;
    }
}

function prepareSummary() {
    const clinic = document.getElementById('clinicSelect').value;
    const doctor = document.getElementById('doctorSelect').value;
    const date = document.getElementById('appointmentDate').value;
    const errorBox = document.getElementById('errorMessage');

    if (!clinic || !doctor || !date) {
        errorBox.style.display = 'block';
        return;
    }

    errorBox.style.display = 'none';
    
    // Sağ taraftaki özeti güncelle ve aktif et
    document.getElementById('sumClinic').innerText = clinic.toUpperCase();
    document.getElementById('sumDoctor').innerText = doctor;
    document.getElementById('sumDate').innerText = date;

    const summaryCard = document.getElementById('summaryCard');
    summaryCard.style.opacity = "1";
    summaryCard.style.pointerEvents = "auto";
    summaryCard.style.transform = "scale(1.02)";
}

function confirmAppointment() {
    // Burada Backend API'sine (POST /api/appointments) istek atılacak
    alert("Tebrikler! Randevunuz başarıyla oluşturuldu.");
    window.location.reload(); // Sayfayı sıfırla
}