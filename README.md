# 🧾 e-participant (Etkinlik - Yoklama Takip Sistemi)

**e-participant**, kullanıcıların etkinlik oluşturabildiği, katılımcı listeleri üzerinden **günlük yoklama alabileceği** ve **katılımcı yoklamalarını kontrol edebip kaydedebildiği** modern bir **Android mobil uygulamasıdır.**  

Uygulama, **Firebase Authentication** ve **Firestore** altyapısı üzerinde çalışır; kullanıcı verileri, etkinlikler ve yoklamalar bulutta güvenli bir şekilde saklanır.

---

## 🎥 Demo Videosu
<div align="center">
  <video src="https://github.com/user-attachments/assets/015a568c-21d8-431b-a2a8-950a8cf98a67"
         controls
         muted
         loop
         playsinline
         width="360">
  </video>
</div>

## 🚀 Özellikler

### 👤 Kullanıcı İşlemleri
- E-posta ve şifre ile **kayıt olma / giriş yapma**
- Firebase Authentication ile güvenli kimlik doğrulama, güvenli çıkış yapabilme.

### 📅 Etkinlik Yönetimi
- Kullanıcı kendi etkinliklerini oluşturabilir  
- Her etkinlikte başlık, tarih ve açıklama bilgisi bulunur  
- Firestore yapısı:
  ```
  users/{userId}
    -email
    -name
    -uid
        /activities/{activityId}
            -id
            -title
            -dateCreated
                /attendance{attendanceId}
                    -id
                    -title
                    -date
                        /participantAttendance{participantAttendaceId}
                            -participant
                            -approval
                            -checkInTime
                            -checkOutTime
                /participants{participantId}
                    -id
                    -name
  ```

### 🧍 Katılımcı Yönetimi
- Etkinliğe manuel olarak katılımcı ekleme  
- Excel (yalnızca B sütunu "Kullancı isim Soyisim") dosyasıyla toplu katılımcı import etme  
- Katılımcı listesi görüntüleme ve silme  
- Her katılımcıya benzersiz bir ID atanır (1’den başlar)

### ✅ Yoklama (Attendance) Sistemi
- Seçilen etkinlik için yoklama başlatma  
- Yoklama kaydında:
  - **Tarih ve saat**
  - **approve = true/false** durumu  
  - **Kimlik/Kart/Manuel** ile doğrulama seçenekleri  
- Kamera ile kimlik doğrulama:
  - Eşleşen kullanıcıda kısa süreli **yeşil onay işareti (✔️)** gösterilir  
  - Eşleşme olmazsa onay verilmez  

### 📊 Dışa Aktarım ve Raporlama
- Yoklama kayıtlarını listeleme ve düzenleme  
- Excel formatında dışa aktarma (katılımcı bazlı yoklama listesi)
- Pdf formatında dışa aktarma (katılımcı bazlı yoklama listesi)

---

## 🏗️ Kullanılan Teknolojiler

| Katman | Teknoloji |
|--------|------------|
| **Dil** | Kotlin |
| **UI** | XML (Material Design 3) |
| **Mimari** | MVVM (Model–View–ViewModel) |
| **Veritabanı** | Firebase Firestore |
| **Kimlik Doğrulama** | Firebase Authentication |
| **Depolama** | Firebase Storage
| **Bağımlılıklar** | ViewModel, LiveData, Coroutines, RecyclerView, ActivityResultLauncher |
| **Ek Kütüphaneler** | Glide,Firebase, Material Components, OCR |

---

## 📂 Proje Yapısı

```
com.example.eparticipant
├── data
│   ├── model
│   ├── repository
│   ├── firebase
│
├── domain
│   ├── model
│   ├── usecase
│
├── ui
│   ├── login
│   ├── register
│   ├── adapters
│   ├── main
│       ├── activity
│       ├── participant
│       ├── attendance
│       ├── dialog
│
├── viewModel
│
└── utils
```

---

## ⚙️ Kurulum

1. **Projeyi klonla**
   ```bash
   git clone <https://github.com/ariftuncerr/e-participant.git>
   ```

2. **Android Studio ile aç**
   - `File > Open > e-participant`
   - Gradle bağımlılıkları otomatik olarak indirilecektir.

3. **Firebase bağlantısını yapılandır**
   - `google-services.json` dosyasını `/app` dizinine ekleyin  
   - Firebase Console üzerinden Authentication ve Firestore’u etkinleştirin

4. **Uygulamayı çalıştır**
   - Emülatör veya fiziksel cihaz seçip ▶️ tuşuna basın

---

## 🧩 Gelecek Güncellemeler

- 🔐 QR/NFC tabanlı yoklama sistemi  
- 📊 Katılımcı verilerini grafiklerle analiz etme  
- 🌐 Çoklu dil desteği (Türkçe / İngilizce)  
- ☁️ Offline senkronizasyon (Room + Firestore)

---

## 👨‍💻 Geliştirici

**👤 Arif Tunçer**  
📍 Karabük Üniversitesi — Bilgisayar Mühendisliği  
💬 Android Developer | Kotlin | Firebase | MVVM  
📧 [arif.61.tuncer@gmail.com](mailto:arif.61.tuncer@gmail.com)  
🔗 [LinkedIn Profilim](https://www.linkedin.com/in/arif-tuncer/)

