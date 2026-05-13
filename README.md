
# 🏥 Hastane Randevu ve Klinik Yönetim Sistemi

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Bu proje, hastaların online randevu alabilmesini, doktorların takvimlerini yönetebilmesini ve hastane personelinin tüm operasyonel süreçleri dijital ortamda takip edebilmesini sağlayan **Full-Stack** bir hastane yönetim çözümüdür.

## 🚀 Öne Çıkan Özellikler

*   **Dinamik Slot Sistemi:** Randevu çakışmalarını %100 engelleyen, doktor bazlı müsaitlik takvimi.
*   **Gelişmiş Yetkilendirme:** RBAC (Role Based Access Control) ile Admin, Personel, Doktor ve Hasta rolleri.
*   **Güvenli Kimlik Doğrulama:** Spring Security ve JWT (JSON Web Token) ile stateless oturum yönetimi.
*   **IDOR Koruması:** Kullanıcıların sadece kendi verilerine erişebilmesini sağlayan `SecurityUtil` entegrasyonu.
*   **Otomatik Bildirimler:** JavaMailSender ile kayıt onayı ve randevu bilgilendirmeleri.
*   **Responsive Arayüz:** Tüm cihazlarda sorunsuz çalışan modern ve dinamik frontend yapısı.

## 🛠 Teknolojiler

### Backend
*   **Framework:** Java / Spring Boot
*   **Security:** Spring Security & JWT
*   **Veri Erişimi:** Spring Data JPA / Hibernate
*   **Veritabanı:** MySQL 
*   **Mail:** JavaMailSender API

### Frontend
*   **Temel:** HTML5, CSS3, JavaScript (ES6+)
*   **İletişim:** Fetch API
*   **Depolama:** LocalStorage (Token yönetimi için)

## 🏗 Sistem Mimarisi

Proje **Katmanlı Mimari (Layered Architecture)** prensiplerine göre organize edilmiştir:
*   `Controller`: API Endpoint yönetimi.
*   `Service`: İş mantığı ve validasyonlar.
*   `Repository`: Veritabanı CRUD işlemleri.
*   `Entity`: Veritabanı tablo modelleri.
*   `DTO`: Veri transfer objeleri.

## 🔑 Kullanıcı Rolleri ve Yetkiler

| Rol | Yetkiler |
| :--- | :--- |
| **ADMIN** | Sistemdeki tüm kullanıcı, doktor ve randevu verilerini yönetir.  Kayıt işlemleri, personel yönetimi ve genel sistem takibi.|
| **DOCTOR** | Kendisine atanan randevuları ve çalışma slotlarını görüntüleme. |
| **PATIENT** | Doktor seçimi, müsait slotları görüntüleme ve randevu alma/iptal. |

## 📡 API Endpoint Örnekleri

| Metod | Endpoint | Açıklama |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Kullanıcı girişi ve JWT üretimi |
| `GET` | `/api/doctors/slots` | Doktorun müsait randevu saatlerini getirir |
| `POST` | `/api/appointments/create` | Yeni bir randevu oluşturur (Slot durumunu günceller) |
| `PUT` | `/api/appointments/cancel` | Randevuyu iptal eder (Slotu tekrar açar) |

## 💻 Kurulum ve Çalıştırma

1.  **Depoyu Klonlayın:**
    ```bash
    git clone [https://github.com/kullaniciadi/hastane-randevu-sistemi.git](https://github.com/kullaniciadi/hastane-randevu-sistemi.git)
    ```
2.  **Veritabanı Ayarları:**
    `src/main/resources/application.properties` dosyasındaki veritabanı
