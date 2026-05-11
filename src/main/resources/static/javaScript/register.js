document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const errorBox = document.getElementById('errorBox');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        errorBox.style.display = 'none';

        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            tckn: document.getElementById('tckn').value,
            email: document.getElementById('email').value,
            birthDate: document.getElementById('birthDate').value,
            bloodType: document.getElementById('bloodType').value,
            password: document.getElementById('password').value,
            username: null
        };

        try {
            const response = await fetch('https://medsoft.up.railway.app/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok) {
                alert("Kaydınız başarıyla oluşturuldu! Şimdi giriş yapabilirsiniz.");
                window.location.href = 'index.html';
            } else {
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