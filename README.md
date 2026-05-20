# 🎴 Okey 101 Scorer

Okey 101 oyunlarınız için tasarlanmış, geleneksel kağıt-kalem skor tutma derdine son veren, ultra modern arayüze, gelişmiş parti oyunu mekaniklerine ve benzersiz seyirci etkileşimine sahip **premium bir Android skor hesaplama ve canlı yayın uygulamasıdır.**

Bu uygulama, sadece basit bir skor tablosu olmanın ötesinde, masadaki yancılardan WhatsApp paylaşımlarına kadar oyun deneyimini tamamen dijitalleştiren yenilikçi çözümler sunar.

---

## 🚀 Hızlı Bakış ve Özellikler

### 🎨 1. Premium & Dinamik Tasarım Sistemi (Aura Tasarım)
* **Skora Göre Değişen Dinamik Aura (Mesh Gradients)**: Arka planda kazanan taraf yeşil, kaybeden taraf ise kırmızı renkte "nefes alan" sisli bir ışık hüzmesi (aura) ile parlar. Skordaki fark açıldıkça ışığın şiddeti otomatik olarak artar (800 puan farkta maksimuma ulaşır).
* **Dinamik Sütun Renklendirmesi**: İki takım arasındaki fark açıldıkça, geride kalan takımın skor sütunları pürüzsüz bir animasyonla kırmızı renge boyanarak yenilgi hissini görselleştirir.
* **Modern Tipografi**: Google Fonts *Outfit* yazı tipi ile göz yormayan, premium bir arayüz tasarımı.
* **Akıllı Skor Alanları**: Çifte gitme durumunda `-101` ve `-202` cezaları için özel olarak renklendirilmiş (neon kırmızı) hücre tasarımları.
* **Otomatik Tur İlerleme**: Her iki takımın da skoru girildiğinde otomatik olarak yeni el (satır) açan akıllı algoritma.

### 💾 2. Yerel Kayıt Güvencesi (DataStore & Serialization)
* **Asenkron ve Güvenilir Kayıt**: Uygulama içi veriler, modern `Preferences DataStore` mimarisi kullanılarak arka planda (asenkron) kaydedilir. Arayüzde hiçbir bloklanma veya takılma yaşanmaz.
* **Veri Bütünlüğü**: Skorlar ve oyun durumları `Kotlinx Serialization` ile güvenli bir şekilde JSON formatına çevrilerek saklanır. Şarjınız bitse veya uygulama kapansa bile oyununuz saniyesi saniyesine güvendedir.

### 🎲 3. Masa Etkinlik Motoru (Party Game Mechanics)
Okey 101 oyununu düz bir skor takibinden çıkarıp tam bir parti oyununa dönüştüren **rastgele olaylar sistemi** (%85 normal el, %15 etkinlikli el):
* **Kader Çarkı (Yancı İhtilali)**: Masada ansızın beliren ve seyircilerle ortak çalışan dönen bir çark! Şanssız takıma saniyeler içinde +101 ceza yazar.
* **Gizemli Kutu (Mystery Box)**: Rastgele bir takıma beklenmedik sürpriz bir ödül (-202) veya devasa bir ceza (+202) çıkarır.
* **Büyük Takas (Great Swap)**: Ekranda bomba patlayarak herkesin ıstakasını devretmesini söyleyen, oyunun kaderini değiştiren kaos kuralı.
* **Çifte Kumar (Yazı-Tura)**: Eli kazanan takıma el bitiminde sunulan büyük risk! Havada dönen sanal madeni para (🪙) ile kazandığın eksi puanı ikiye katlama (x2) ya da tamamen sıfırlama (0) heyecanı.
* **Kaos Eli**: Bu eldeki tüm cezaların ve ödüllerin otomatik 2 katı hesaplanmasını sağlayan gaddarlık sistemi.
* **Gaddarlık Filtresi**: Eğer puan farkı 350'den azsa, adaleti çok erken sarsmamak adına `Great Swap` havuzdan çıkarılır ve ağırlığı `Mystery Box`'a aktarılır.
* **Görsel Şölen**: Her kural tetiklendiğinde telefon ekranında ve **eşzamanlı olarak yancıların web ekranında** çıkan animasyonlu, gecikmeli ve gerilimli tam ekran diyaloglar!

### 📡 4. Seyirci Modu & "Akan Sohbet" (Interactive Spectator & Live Banter Chat)
* **Kendi Ekranından İzleme**: Masadaki seyirciler (yancılar), oyunu izlemek için telefonunuza eğilmek zorunda kalmaz.
* **Yerel QR Kod ile Hızlı Bağlantı**: Dış bir API'ye bağımlı olmadan uygulamanın kendi içinde (`ZXing Core` ile) oluşturduğu karekod ile seyirciler (yancılar) saniyeler içinde odaya katılabilir.
* **6 Haneli Oda Kodu**: Karekod okutmak istemeyen seyirciler, 6 haneli oda kodunu web sitesine doğrudan girerek de bağlanabilirler.
* **Milisaniyelik Canlı Yayın (Firebase)**: `Firebase Realtime Database` altyapısı sayesinde yancıların tarayıcısında açılan şık web sayfası, masadaki skorları **anlık (real-time)** olarak yansıtır.
* **👤 Seyirci & Oyuncu Profilleri (Avatars)**: Web yayınına bağlanan kişileri şık bir glassmorphism arayüzlü profil seçim ekranı karşılar. Seyirciler kendi isimlerini girdikten sonra eğlenceli rollerden birini seçerek masaya katılır:
  * 👑 *Üstad*
  * 🪨 *Taş Çalan*
  * 🎲 *Şanslı*
  * ☕ *Çaycı*
  * 🧐 *Taktikçi*
  * ⚖️ *Hakem*
* **💬 "Akan Sohbet" (Live Banter Chat Stream)**: 
  * **Android Ekranında Kapsül Akışı**: Gelen mesajlar host telefonunda klavyeyi engellemeyecek şekilde cam efektli (glassmorphic) kartlar halinde üst üste kayarak belirir ve 6 saniye sonra eriyerek kaybolur.
* **👏 Uçan Reaksiyonlar & İsimlikler**: Yancılar dev emojilere dokunarak masaya canlı tepkiler gönderebilir! Gönderilen emojiler, host'un Android ekranında sinüs dalgalı salınımlarla süzülerek yukarı uçar. Emojinin altında gönderen kişinin avatarı uçar!
* **🛡️ Akıllı Spam & Hız Sınırları**:
  * *Reaksiyon Sınırı*: Bir yancı 5 saniye içinde 12'den fazla emoji göndermeye çalışırsa, tarayıcı ekranına animasyonlu şık bir `"SAKİN OL ŞAMPİYON! 🏎️💨"` uyarısı fırlatılır ve istekler geçici olarak engellenir.
  * *Sohbet Sınırı*: Sohbet mesajı göndermek 3 saniyelik bir hız sınırına (rate-limit) tabidir. Seyirci spam atmak istediğinde sohbet girdisinin çerçevesi kırmızıya döner ve input kutusu yatayda titreme (shake) animasyonu yaparak kullanıcıyı uyarır.
  * *Host Koruma Limiti*: Host telefonunda kasma/donma yaşanmaması için ekranda aynı anda en fazla 15 uçan parçacık gösterilir, fazlası kuyruğa alınmadan filtrelenir!
* **Yayın Kapatma Güvenliği**: Yayın durdurulduğunda yancıların ekranlarında uyarı çıkar ve 3 saniye sonra otomatik olarak odadan atılıp ana menüye yönlendirilirler.

### 📳 5. Sallama (Shake) İle Fark Hesaplama
* **Dramatik Deneyim**: Telefonu salladığınızda masada heyecan yaratacak **2 aşamalı bir animasyon** devreye girer.
* Önce ekranda *"Fark Hesaplanıyor..."* ibaresiyle şık bir yükleme halkası döner, ardından 1 saniye sonra kazanan takımın durumuna göre (yeşil, kırmızı veya gri neon çerçeveli) büyük bir **Sonuç Kartı** yay efektiyle ekrana fırlayarak puan farkını gösterir.

### 🧹 6. Dinamik Satır Silme ve Geri Alma (Swipe-To-Delete & Undo)
* **Pratik Kullanım**: Yanlış girilen el skorlarını satırı sola veya sağa kaydırarak kolayca silebilirsiniz.
* **Geri Alma Desteği**: Silme işleminden sonra ekranda beliren Snackbar üzerindeki "Geri Al" butonu yardımıyla silinen eli ve skorlarını anında kurtarabilirsiniz.

### ⚡ 7. Üst Düzey Performans (Compose Optimizasyonları)
* **Bileşen Mimarisi (Componentization)**: Yeniden çizim (recomposition) yükünü azaltmak amacıyla ekran bileşenleri (`TeamHeader`, `RoundList`, `SumBar`, `CustomNumpad`) izole edilmiştir.
* **derivedStateOf**: Aura renkleri ve fark hesaplamaları gibi yoğun işlemler `derivedStateOf` ile sınırlandırılmıştır. Gereksiz hiçbir hesaplama veya çizim yapılmaz.
* **Görünüm Önbellekleme (View Caching)**: Numpad menüsü silinip tekrar oluşturulmak yerine `graphicsLayer` ile GPU üzerinde kaydırılarak (TranslationY) donanımsal ivmelendirmeyle çalışır.
* **Asenkron I/O**: Diske yazma işlemleri (`DataStore`) arka planda çalışarak ana iş parçacığını (Main Thread) meşgul etmez.

### 📲 8. Akıllı Ekran Görüntüsü ve Özet Paylaşımı
* Tek tuşla skor tablosunun yüksek çözünürlüklü bir ekran görüntüsünü alır.
* Paylaşım esnasında WhatsApp'ta otomatik olarak şu şekilde **Markdown formatlı, emojili şık bir özet metin** oluşturur:
  > 🎴 **OKEY 101 SKOR TABLOSU** 🎴
  > 
  > 🟢 **BİZ**: 250
  > 🔴 **ONLAR**: 450
  > 
  > ⚖️ **Puan Farkı**: 200
  > 🏆 **Durum**: BİZ ÖNDE! 😎

### 🎨 9. Kod Tabanlı Animasyonlu Açılış Ekranı (AnimatedSplashScreen)
* **Logosuz Giriş Şovu**: Cold-start (soğuk çalıştırma) aşamasında dahi Android sistem logolarını kapatan transparan tema altyapısı.
* **Dinamik Aura**: `rememberInfiniteTransition` ile yayılan, nefes alıp veren neon kırmızı ve indigo Mesh Gradient arka plan.
* **Neon Kırpışma Efekti**: "101 COCKPIT" başlığının ilk 600ms boyunca sanki elektrik alan bir neon tabela gibi titreşerek ve arkasında neon gölgesiyle (`Shadow`) parlayarak açılması.
* **Süzülen Alt Metin**: "Eğlence Modu Başlatılıyor..." ifadesinin pürüzsüzce aşağıdan yukarıya doğru süzülüp yerleşmesi.

---

## 🛠️ Kullanılan Teknolojiler

### Android Uygulaması (Compile SDK 36 / Target SDK 36)
* **Dil**: Kotlin
* **Arayüz**: Jetpack Compose (BOM) & Material 3
* **Mimari**: MVVM (AndroidViewModel ile yerel veri yönetimi)
* **Veri Katmanı**: Preferences DataStore & Kotlinx Serialization Json
* **Karekod Üretimi**: ZXing Core (3.5.3) ile tamamen yerel üretim
* **Giriş Ekranı**: Android Core Splashscreen (1.2.0)
* **Navigasyon**: Navigation Compose (2.8.5)
* **Asenkron Motor**: Kotlin Coroutines & Flow (StateFlow, SharedFlow)
* **Sensörler**: Android SensorManager (İvmeölçer ile Shake tespiti)

### Seyirci Web Uygulaması
* **Arayüz**: Vanilla HTML5, CSS3 (Glassmorphism & Neon FX)
* **Yazı Tipi**: Google Fonts (Outfit & JetBrains Mono)
* **Haberleşme**: Firebase JS SDK (Realtime Database Listener)
* **Hosting**: GitHub Pages

### Sunucu & Veritabanı
* **Altyapı**: Firebase Realtime Database (Google Cloud)
* **Haberleşme**: WebSockets tabanlı canlı dinleme ve yazma

---

## 📁 Proje Dosya Yapısı

```
okey101Scorer/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/okey101scorer/
│           │   ├── components/
│           │   │   ├── EventDialogs.kt      # Mystery Box, Yancı İhtilali, Kumar, Swap dialogları
│           │   │   ├── RoundList.kt         # Skor satırlarının listelendiği Swipe-To-Delete listesi
│           │   │   ├── ScoreComponents.kt   # Temel UI elemanları
│           │   │   ├── SumBar.kt            # Alt toplam çubuğu
│           │   │   └── TeamHeader.kt        # Üst takım başlıkları ve durum göstergeleri
│           │   ├── engine/
│           │   │   ├── EventEngine.kt       # Olay havuzu ve ağırlıklı rastgele seçim motoru
│           │   │   └── TableEvent.kt        # Olay listesi ve gaddarlık seviyesi enumları
│           │   ├── ui/theme/                # Renk, tipografi ve tema tanımlamaları
│           │   ├── AnimatedSplashScreen.kt  # Animasyonlu neon açılış ekranı ve geçiş kontrolü
│           │   ├── CustomNumpad.kt          # Donanımsal ivmeli özel klavye paneli
│           │   ├── MainActivity.kt          # Uygulama başlangıç noktası
│           │   ├── MainScreen.kt            # Ana skor tahtası, aura ve reaksiyon katmanları
│           │   └── ScoreViewModel.kt        # Firebase, DataStore ve oyun lojiği ViewModel'i
│           └── AndroidManifest.xml
├── spectator_web/                           # Geliştirme aşamasındaki web arayüzü
├── index.html                               # GitHub Pages üzerinde canlıya alınan ana web arayüzü
└── README.md                                # Proje belgelendirmesi (Şu an okuduğunuz dosya)
```

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
