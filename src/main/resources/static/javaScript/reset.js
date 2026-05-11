document.getElementById('resetPasswordForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');

    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        alert("Şifreler eşleşmiyor!");
        return;
    }

    try {
        const response = await fetch('https://medsoft.up.railway.app/api/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                token: token,
                newPassword: newPassword
            })
        });

        if (response.ok) {
            alert("Şifreniz başarıyla güncellendi! Giriş sayfasına yönlendiriliyorsunuz.");
            window.location.href = 'index.html';
        } else {
            const errorData = await response.json();
            const cleanMessage = errorData.message || "İşlem sırasında bir hata oluştu.";
            alert(cleanMessage);
        }
    } catch (error) {
        alert("Bağlantı hatası! Lütfen internetinizi veya sunucuyu kontrol edin.");
    }
});