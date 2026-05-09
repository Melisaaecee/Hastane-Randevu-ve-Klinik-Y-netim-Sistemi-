document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const errorBox = document.getElementById('errorBox');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Önceki hataları gizle
        errorBox.style.display = 'none';

        // Form verilerini DTO yapısına uygun topla
        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            tckn: document.getElementById('tckn').value,
            email: document.getElementById('email').value,
            birthDate: document.getElementById('birthDate').value,
            bloodType: document.getElementById('bloodType').value,
            password: document.getElementById('password').value,
            username: null // Backend'de PATIENT için null olması gerektiğini belirtmiştik
        };

        try {
            const response = await fetch('http://localhost:8080/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok) {
                // Başarılı kayıt
                alert("Kaydınız başarıyla oluşturuldu! Şimdi giriş yapabilirsiniz.");
                window.location.href = 'index.html'; // Giriş ekranına yönlendir
            } else {
                // Backend'den gelen hata mesajı (BadRequestException vb.)
                showError(result.message || "Kayıt işlemi başarısız.");
            }
        } catch (err) {
            showError("Sunucuya bağlanılamadı. Backend'in çalıştığından emin olun.");
        }
    });

    function showError(msg) {
        errorBox.innerText = msg;
        errorBox.style.display = 'block';
    }
});