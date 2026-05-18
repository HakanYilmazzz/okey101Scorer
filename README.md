# 🎴 Okey 101 Scorer

Okey 101 oyunlarınız için tasarlanmış, geleneksel kağıt-kalem skor tutma derdine son veren, ultra modern arayüze ve benzersiz sosyal özelliklere sahip **premium bir Android skor hesaplama ve canlı yayın uygulamasıdır.**

Bu uygulama, sadece basit bir skor tablosu olmanın ötesinde, masadaki yancılardan WhatsApp paylaşımlarına kadar oyun deneyimini tamamen dijitalleştiren yenilikçi çözümler sunar.

---

## ✨ Öne Çıkan Özellikler

### 📱 1. Premium & Dinamik Arayüz (Dark Mode)
* **Modern Tipografi**: Google Fonts *Outfit* yazı tipi ile göz yormayan, premium bir tasarım.
* **Akıllı Skor Alanları**: Çifte gitme durumunda `-101` ve `-202` cezaları için özel olarak renklendirilmiş (neon kırmızı) hücre tasarımları.
* **Otomatik Tur İlerleme**: Her iki takımın da skoru girildiğinde otomatik olarak yeni el (satır) açan akıllı algoritma.

### 💾 2. Yerel Kayıt Güvencesi (Offline Persistence)
* **Asla Kaybolmayan Skorlar**: Oyunu oynarken telefonun şarjı bitse, uygulama kapansa veya kazara kapatsanız bile skorlarınız `SharedPreferences` altyapısı sayesinde anında kaydedilir. Uygulamayı açtığınızda kaldığınız yerden devam edersiniz.

### 📡 3. Seyirci Modu (Yancılar İçin Canlı Yayın)
* **Kendi Ekranından İzleme**: Masadaki seyirciler (yancılar), oyunu izlemek için telefonunuza eğilmek zorunda kalmaz.
* **QR Kod Canlı Yayın**: Uygulamadaki anten butonuna basarak bir **Seyirci Odası** açabilir ve üretilen QR kodu yancılara okutabilirsiniz.
* **Milisaniyelik SSE (Server-Sent Events) Teknolojisi**: Hiçbir uygulama yüklemeden yancıların tarayıcısında açılan şık web sayfası (`https://okey101scorer.vercel.app`), `ntfy.sh` sunucuları üzerinden skorlarınızı **anlık (real-time)** olarak yansıtır.
* **Dinamik Neon Temalar**: Seyirci ekranı, önde olan takıma göre otomatik olarak yeşil veya kırmızı neon ışıklarla parlar!

### 📳 4. Sallama (Shake) İle Fark Hesaplama Animasyonu
* **Dramatik Deneyim**: Telefonu salladığınızda masada heyecan yaratacak **2 aşamalı bir animasyon** devreye girer.
* Önce ekranda *"Fark Hesaplanıyor..."* ibaresiyle şık bir yükleme halkası döner, ardından 1 saniye sonra kazanan takımın durumuna göre (yeşil, kırmızı veya gri neon çerçeveli) büyük bir **Sonuç Kartı** yay efektiyle ekrana fırlayarak puan farkını gösterir.

### 📲 5. Akıllı Ekran Görüntüsü ve Özet Paylaşımı
* Tek tuşla skor tablosunun yüksek çözünürlüklü bir ekran görüntüsünü alır.
* Paylaşım esnasında WhatsApp'ta otomatik olarak şu şekilde **Markdown formatlı, emojili şık bir özet metin** oluşturur:
  > 🎴 **OKEY 101 SKOR TABLOSU** 🎴
  > 
  > 🟢 **BİZ**: 250
  > 🔴 **ONLAR**: 450
  > 
  > ⚖️ **Puan Farkı**: 200
  > 🏆 **Durum**: BİZ ÖNDE! 😎

---

## 🛠️ Kullanılan Teknolojiler

### Android Uygulaması
* **Dil**: Kotlin
* **Arayüz**: Jetpack Compose (Modern Bildirimsel UI)
* **Mimari**: MVVM (AndroidViewModel ile yerel veri yönetimi)
* **Asenkron Motor**: Kotlin Coroutines & Flow (StateFlow)
* **Sensörler**: Android SensorManager (İvmeölçer ile Shake tespiti)

### Seyirci Web Uygulaması
* **Arayüz**: Vanilla HTML5, CSS3 (Glassmorphism & Neon FX)
* **Yazı Tipi**: Google Fonts (Outfit & JetBrains Mono)
* **Haberleşme**: EventSource (Server-Sent Events) ile `ntfy.sh` entegrasyonu
* **Hosting**: Vercel (100% Ücretsiz & Gizli Depoları Destekler)

---

## 🚀 Nasıl Çalıştırılır?

### Android Uygulamasını Derlemek
1. Bu depoyu bilgisayarınıza klonlayın:
   ```bash
   git clone https://github.com/HakanYilmazzz/okey101Scorer.git
   ```
2. **Android Studio**'yu açın ve projeyi içe aktarın (Import).
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Cihazınızı bağlayın veya emülatör seçip **Run** butonuna basın.

### Seyirci Ekranını Canlıya Almak (Vercel ile Ücretsiz)
1. **vercel.com** adresine giderek ücretsiz bir hesap oluşturun ve GitHub hesabınızı bağlayın.
2. **Add New > Project** seçeneğine tıklayarak `okey101Scorer` deponuzu (Private olsa dahi) seçip **Import** edin.
3. Proje ayarları ekranında:
   * **Root Directory** kısmını `spectator_web` olarak seçin.
   * **Project Name** kısmını `okey101scorer` yapın (böylece adresiniz `okey101scorer.vercel.app` olur).
4. **Deploy** butonuna basın. Yaklaşık 20 saniye içinde siteniz tamamen ücretsiz ve gizli olarak yayına girecektir!

---

## 📄 Lisans
Bu proje kişisel gelişim ve eğlence amacıyla geliştirilmiştir. Dilediğiniz gibi geliştirebilir, okey masalarınızda kullanabilirsiniz! 😉
