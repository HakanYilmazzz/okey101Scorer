# 🎴 Okey 101 Scorer

Okey 101 oyunlarınız için tasarlanmış, geleneksel kağıt-kalem skor tutma derdine son veren, ultra modern arayüze ve benzersiz sosyal özelliklere sahip **premium bir Android skor hesaplama ve canlı yayın uygulamasıdır.**

Bu uygulama, sadece basit bir skor tablosu olmanın ötesinde, masadaki yancılardan WhatsApp paylaşımlarına kadar oyun deneyimini tamamen dijitalleştiren yenilikçi çözümler sunar.

---

## ✨ Öne Çıkan Özellikler

### 📱 1. Premium & Dinamik Arayüz (Dark Mode & Aura)
* **Skora Göre Değişen Dinamik Aura (Mesh Gradients)**: Arka planda kazanan takımın tarafı yeşil, kaybeden tarafı ise kırmızı renkte "nefes alan" sisli bir ışık hüzmesi (aura) ile parlar. Skordaki fark açıldıkça ışığın şiddeti artar.
* **Modern Tipografi**: Google Fonts *Outfit* yazı tipi ile göz yormayan, premium bir tasarım.
* **Akıllı Skor Alanları**: Çifte gitme durumunda `-101` ve `-202` cezaları için özel olarak renklendirilmiş (neon kırmızı) hücre tasarımları.
* **Otomatik Tur İlerleme**: Her iki takımın da skoru girildiğinde otomatik olarak yeni el (satır) açan akıllı algoritma.

### 💾 2. Yerel Kayıt Güvencesi (DataStore & Serialization)
* **Asenkron ve Güvenilir Kayıt**: Uygulama içi veriler, modern `Preferences DataStore` mimarisi kullanılarak arka planda (asenkron) kaydedilir. Arayüzde hiçbir bloklanma veya takılma yaşanmaz.
* **Veri Bütünlüğü**: Skorlar ve oyun durumları `Kotlinx Serialization` ile güvenli bir şekilde JSON formatına çevrilerek saklanır. Şarjınız bitse veya uygulama kapansa bile oyununuz saniyesi saniyesine güvendedir.

### 🎲 3. Masa Etkinlik Motoru (Party Game Mechanics)
Okey 101 oyununu düz bir skor takibinden çıkarıp tam bir parti oyununa dönüştüren **rastgele olaylar sistemi**:
* **Kader Çarkı (Yancı İhtilali)**: Masada ansızın beliren ve seyircilerle ortak çalışan dönen bir çark! Şanssız takıma saniyeler içinde +101 ceza yazar.
* **Gizemli Kutu (Mystery Box)**: Rastgele bir takıma beklenmedik sürpriz bir ödül (-202) veya devasa bir ceza (+202) çıkarır.
* **Büyük Takas (Great Swap)**: Ekranda bomba patlayarak herkesin ıstakasını devretmesini söyleyen, oyunun kaderini değiştiren kaos kuralı.
* **Çifte Kumar (Yazı-Tura)**: Eli kazanan takıma el bitiminde sunulan büyük risk! Havada dönen sanal madeni para (🪙) ile kazandığın eksi puanı ikiye katlama (x2) ya da tamamen sıfırlama (0) heyecanı.
* **Kaos Eli**: Bu eldeki tüm cezaların ve ödüllerin otomatik 2 katı hesaplanmasını sağlayan gaddarlık sistemi.
* **Görsel Şölen**: Her kural tetiklendiğinde telefon ekranında ve **eşzamanlı olarak yancıların web ekranında** çıkan animasyonlu, gecikmeli ve gerilimli tam ekran diyaloglar!

### 📡 4. Seyirci Modu & "Akan Sohbet" (Interactive Spectator & Live Banter Chat)
* **Kendi Ekranından İzleme**: Masadaki seyirciler (yancılar), oyunu izlemek için telefonunuza eğilmek zorunda kalmaz.
* **Yerel (Local) QR Kod ile Hızlı Bağlantı**: Dış bir API'ye bağımlı olmadan uygulamanın kendi içinde (`ZXing Core` ile) oluşturduğu karekod ile seyirciler (yancılar) saniyeler içinde odaya katılabilir. İnternet yavaşlasa dahi QR kodunuz anında hazırdır.
* **Manuel Giriş**: İstenirse **6 haneli oda kodunu** doğrudan web sitesine girerek de bağlanabilirsiniz.
* **Milisaniyelik Canlı Yayın (Firebase)**: `Firebase Realtime Database` altyapısı sayesinde yancıların tarayıcısında açılan şık web sayfası (`https://hakanyilmazzz.github.io/okey101Scorer/`), masadaki skorları **anlık (real-time)** olarak yansıtır. Tamamen limitsiz ve anında senkronize olur.
* **👤 Seyirci & Oyuncu Profilleri (Avatars)**: Web yayınına bağlanan kişileri şık bir glassmorphism arayüzlü profil seçim ekranı karşılar. Seyirciler kendi isimlerini girdikten sonra kendilerine uygun eğlenceli rollerden birini seçerek masaya katılır:
  * 👑 *Üstad*
  * 🪨 *Taş Çalan*
  * 🎲 *Şanslı*
  * ☕ *Çaycı*
  * 🧐 *Taktikçi*
  * ⚖️ *Hakem*
* **💬 "Akan Sohbet" (Live Banter Chat Stream)**: 
  * **Tüm Ekranlarda Ortak Sohbet**: Masaya atılan laflar ve taktikler sadece Host'un telefonunda değil, odadaki tüm yancıların web tarayıcılarında da sol alt köşede "Akan Sohbet" olarak canlı belirir ve 6 saniye sonra eriyerek kaybolur.
  * **Android Ekranında Kapsül Akışı**: Gelen mesajlar host telefonunda klavyeyi engellemeyecek şekilde cam efektli (glassmorphic) kartlar halinde üst üste kayarak belirir.
* **👏 Uçan Reaksiyonlar & İsimlikler**: Yancılar dev emojilere dokunarak masaya canlı tepkiler gönderebilir! Gönderilen emojiler, host'un Android ekranında 4-6 saniyelik çok estetik sinüs dalgalı salınımlarla süzülerek yukarı uçar. Emojinin altında kişinin avatarı uçar!
* **🛡️ Akıllı Spam & Hız Sınırları (Spam-Proof Rates)**:
  * *Reaksiyon Sınırı*: Bir yancı 5 saniye içinde 12'den fazla emoji göndermeye çalışırsa, tarayıcı ekranına animasyonlu şık bir `"SAKİN OL ŞAMPİYON! 🏎️💨"` uyarısı fırlatılır ve istekler geçici olarak engellenir.
  * *Sohbet Sınırı*: Sohbet mesajı göndermek 3 saniyelik bir hız sınırına (rate-limit) tabidir. Seyirci spam atmak istediğinde sohbet girdisinin çerçevesi kırmızıya döner ve input kutusu yatayda titreme (shake) animasyonu yaparak kullanıcıyı uyarır.
  * *Host Koruma Limiti*: Host telefonunda kasma/donma yaşanmaması için ekranda aynı anda en fazla 15 uçan parçacık gösterilir, fazlası kuyruğa alınmadan filtrelenir!
* **Dinamik Aura & Otomatik Kapanış**: Seyirci ekranı da tıpkı uygulama gibi skora göre dinamik "Aura" efektleriyle parlar. Oyun sahibi yayını durdurduğunda ise yancıların ekranlarında uyarı çıkar ve 3 saniye sonra otomatik olarak odadan atılıp ana menüye (oda giriş ekranına) yönlendirilirler.

### 📳 5. Sallama (Shake) İle Fark Hesaplama Animasyonu
* **Dramatik Deneyim**: Telefonu salladığınızda masada heyecan yaratacak **2 aşamalı bir animasyon** devreye girer.
* Önce ekranda *"Fark Hesaplanıyor..."* ibaresiyle şık bir yükleme halkası döner, ardından 1 saniye sonra kazanan takımın durumuna göre (yeşil, kırmızı veya gri neon çerçeveli) büyük bir **Sonuç Kartı** yay efektiyle ekrana fırlayarak puan farkını gösterir.

### ⚡ 6. Üst Düzey Performans (Compose Optimizasyonları)
* **Bileşen Mimarisi (Componentization)**: Binlerce satırlık ana ekran, küçük ve tekrar kullanılabilir bileşenlere (`TeamHeader`, `RoundList`, `SumBar`) bölünerek yalnızca değişen alanların yeniden çizilmesi (recomposition) sağlandı.
* **Durum İzolasyonu (State Isolation) & derivedStateOf**: Numpad gibi klavye girişleri izole edilmiş, aura renkleri ve fark hesaplamaları gibi pahalı işlemler `derivedStateOf` ile sınırlandırılmıştır. Gereksiz hiçbir hesaplama veya çizim yapılmaz.
* **Görünüm Önbellekleme (View Caching)**: Numpad menüsü silinip tekrar oluşturulmak yerine `graphicsLayer` ile GPU üzerinde kaydırılarak (TranslationY) donanımsal ivmelendirmeyle çalışır.
* **Asenkron I/O**: Diske yazma işlemleri (`DataStore`) arka planda çalışarak ana iş parçacığını (Main Thread) meşgul etmez. Uygulama her daim sabit 60 FPS / 120 FPS akıcılığında tepki verir.

### 📲 7. Akıllı Ekran Görüntüsü ve Özet Paylaşımı
* Tek tuşla skor tablosunun yüksek çözünürlüklü bir ekran görüntüsünü alır.
* Paylaşım esnasında WhatsApp'ta otomatik olarak şu şekilde **Markdown formatlı, emojili şık bir özet metin** oluşturur:
  > 🎴 **OKEY 101 SKOR TABLOSU** 🎴
  > 
  > 🟢 **BİZ**: 250
  > 🔴 **ONLAR**: 450
  > 
  > ⚖️ **Puan Farkı**: 200
  > 🏆 **Durum**: BİZ ÖNDE! 😎

### 🎨 8. Kod Tabanlı Animasyonlu Açılış Ekranı (AnimatedSplashScreen)
* **Logosuz Giriş Şovu**: Cold-start (soğuk çalıştırma) aşamasında dahi Android sistem logolarını kapatan transparan tema altyapısı.
* **Dinamik Aura**: `rememberInfiniteTransition` ile yayılan, nefes alıp veren neon kırmızı ve indigo Mesh Gradient arka plan.
* **Neon Kırpışma Efekti**: "101 COCKPIT" başlığının ilk 600ms boyunca sanki elektrik alan bir neon tabela gibi titreşerek ve arkasında neon gölgesiyle (`Shadow`) parlayarak açılması.
* **Süzülen Alt Metin**: "Eğlence Modu Başlatılıyor..." ifadesinin pürüzsüzce aşağıdan yukarıya doğru süzülüp yerleşmesi.
* **NavHost Entegrasyonu**: Minimum 2000ms oynama garantili, Firebase/DataStore verileri hazır olunca (`isReady`) ana ekrana (`MainScreen`) geçiş sağlayan ve açılış ekranını backstack'ten tamamen atan güvenli geçiş yapısı.

---

## 🛠️ Kullanılan Teknolojiler

### Android Uygulaması
* **Dil**: Kotlin
* **Arayüz**: Jetpack Compose (Modern Bildirimsel UI)
* **Mimari**: MVVM (AndroidViewModel ile yerel veri yönetimi)
* **Veri Katmanı**: Preferences DataStore & Kotlinx Serialization
* **Karekod Üretimi**: ZXing Core (Yerel/Tam Bağımsız)
* **Asenkron Motor**: Kotlin Coroutines & Flow (StateFlow, SharedFlow)
* **Sensörler**: Android SensorManager (İvmeölçer ile Shake tespiti)

### Seyirci Web Uygulaması
* **Arayüz**: Vanilla HTML5, CSS3 (Glassmorphism & Neon FX)
* **Yazı Tipi**: Google Fonts (Outfit & JetBrains Mono)
* **Haberleşme**: Firebase JS SDK (Realtime Database Listener)
* **Hosting**: GitHub Pages (100% Ücretsiz ve Pratik)

### Sunucu & Veritabanı
* **Altyapı**: Firebase Realtime Database (Google Cloud)
* **Senkronizasyon**: WebSockets tabanlı canlı dinleme ve yazma

---

## 🚀 Nasıl Çalıştırılır?

### Android Uygulamasını Derlemek
1. Bu depoyu bilgisayarınıza klonlayın:
   ```bash
   git clone https://github.com/HakanYilmazzz/okey101Scorer.git
   ```
2. **Android Studio**'yu açın ve projeyi içe aktarın (Import).
3. Firebase Console üzerinden ücretsiz bir proje oluşturup `google-services.json` dosyasını `app/` klasörüne yapıştırın.
4. Gradle senkronizasyonunun tamamlanmasını bekleyin.
5. Cihazınızı bağlayın veya emülatör seçip **Run** butonuna basın.

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
