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

### 📡 3. Seyirci Modu & "Akan Sohbet" (Interactive Spectator & Live Banter Chat)
* **Kendi Ekranından İzleme**: Masadaki seyirciler (yancılar), oyunu izlemek için telefonunuza eğilmek zorunda kalmaz.
* **QR Kod veya Manuel Giriş**: Anten butonuna basarak **Seyirci Odası** açabilir, yancılara QR kod okutabilir veya **6 haneli oda kodunu** doğrudan web sitesine yazdırarak bağlanabilirsiniz.
* **Milisaniyelik Canlı Yayın (SSE)**: `ntfy.sh` altyapısı ve Server-Sent Events sayesinde yancıların tarayıcısında açılan şık web sayfası (`https://hakanyilmazzz.github.io/okey101Scorer/`), masadaki skorları **anlık (real-time)** olarak yansıtır.
* **👤 Yancı Profilleri (Spectator Avatars)**: Web yayınına bağlanan seyircileri şık bir glassmorphism arayüzlü profil seçim ekranı karşılar. Seyirciler kendi isimlerini (maks 10 karakter) girdikten sonra kendilerine uygun eğlenceli rollerden birini seçerek masaya katılır:
  * ☕ *Çaycı Yancı*
  * 🧐 *Taktikçi Yancı*
  * 👼 *Şans Meleği*
  * ⚖️ *Hakem Yancı*
  * 🏎️ *Hızlı Yancı*
  * 🤫 *Casus Yancı*
* **💬 "Akan Sohbet" (Live Banter Chat)**: Seyirciler, web arayüzündeki tümleşik banter bar aracılığıyla masadakilere canlı laf atabilir ve taktik verebilir!
  * **Android Ekranında Kapsül Akışı**: Gelen sohbet mesajları, host'un Android ekranının sol alt köşesinde (klavyeyi ve skorları engellemeyecek şekilde) özel tasarlanmış cam efektli (glassmorphic) bildirim kartları halinde üst üste kayarak belirir. Ekranda aynı anda en fazla 3 aktif sohbet kartı barındırılır ve her kart 4 saniye sonra kendiliğinden süzülerek kaybolur.
* **👏 Canlı Reaksiyonlar & Uçan İsimlikler**: Yancılar web sayfasındaki dev emojilere dokunarak masaya canlı tepkiler gönderebilir! Gönderilen emojiler, host'un Android ekranında **sinüs dalgalı salınımlarla süzülerek yukarı uçar**. Emojinin hemen altında, reaksiyonu gönderen yancının **adı ve seçtiği karakterin avatarı (Örn: ☕ HAKAN)** şık bir kapsül pill içerisinde uçar!
* **🛡️ Akıllı Spam & Hız Sınırları (Spam-Proof Rates)**:
  * *Reaksiyon Sınırı*: Bir yancı 5 saniye içinde 12'den fazla emoji göndermeye çalışırsa, tarayıcı ekranına animasyonlu şık bir `"SAKİN OL ŞAMPİYON! 🏎️💨"` uyarısı fırlatılır ve istekler geçici olarak engellenir.
  * *Sohbet Sınırı*: Sohbet mesajı göndermek 3 saniyelik bir hız sınırına (rate-limit) tabidir. Seyirci spam atmak istediğinde sohbet girdisinin çerçevesi kırmızıya döner ve input kutusu yatayda titreme (shake) animasyonu yaparak kullanıcıyı uyarır.
  * *Host Koruma Limiti*: Host telefonunda kasma/donma yaşanmaması için ekranda aynı anda en fazla 15 uçan parçacık gösterilir, fazlası kuyruğa alınmadan filtrelenir!
* **Dinamik Neon Temalar & Otomatik Kapanış**: Seyirci ekranı, lider takıma göre yeşil veya kırmızı neon ışıklarla parlar. Oyun sahibi yayını durdurduğunda ise yancıların ekranları otomatik olarak sonlandırılır ve giriş ekranına yönlendirilir.

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
* **Asenkron Motor**: Kotlin Coroutines & Flow (StateFlow, SharedFlow)
* **Sensörler**: Android SensorManager (İvmeölçer ile Shake tespiti)

### Seyirci Web Uygulaması
* **Arayüz**: Vanilla HTML5, CSS3 (Glassmorphism & Neon FX)
* **Yazı Tipi**: Google Fonts (Outfit & JetBrains Mono)
* **Haberleşme**: EventSource (Server-Sent Events) ile `ntfy.sh` entegrasyonu
* **Hosting**: GitHub Pages (100% Ücretsiz ve Pratik)

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

### Seyirci Ekranını GitHub Pages Üzerinden Yayınlamak (10 Saniyede Hazır!)
Web arayüzümüz tamamen tek bir `index.html` dosyasına sığdırılmış ve ana dizine yerleştirilmiştir. Bu sayede hiçbir Vercel veya sunucu ayarı yapmadan doğrudan GitHub üzerinden ücretsiz yayınlayabilirsiniz:

1. Tarayıcınızdan GitHub'daki `okey101Scorer` deponuza gidin.
2. Sağ üstteki **Settings** (Ayarlar) sekmesine tıklayın.
3. Sol menüden **Pages** bölümüne gelin.
4. **Build and deployment** başlığı altındaki ayarları şu şekilde yapın:
   * **Source**: *Deploy from a branch*
   * **Branch**: *main* (veya master) / `/ (root)`
5. **Save** butonuna tıklayın.

Birkaç saniye içinde web siteniz dünya çapında tamamen ücretsiz olarak **`https://{{username}}.github.io/okey101Scorer/`** adresinde canlıya alınacaktır! 🎉

---

## 📄 Lisans
Bu proje kişisel gelişim ve eğlence amacıyla geliştirilmiştir. Dilediğiniz gibi geliştirebilir, okey masalarınızda kullanabilirsiniz! 😉
