# QuizBattle - Aplikasi Quiz Berbasis RPG Battle System

## 📚 Informasi Mata Kuliah
- **Mata Kuliah**: Bahasa Pemrograman 3
- **Program Studi**: Teknik Informatika
- **Universitas**: Universitas Kuningan
- **Tahun Akademik**: 2024/2025

## 📝 Deskripsi Proyek

QuizBattle adalah aplikasi mobile berbasis Android yang menggabungkan konsep pembelajaran melalui kuis dengan sistem pertarungan RPG (Role-Playing Game). Aplikasi ini menghadirkan pengalaman belajar yang interaktif dan menyenangkan dengan memvisualisasikan setiap pertanyaan sebagai pertarungan antara Knight (pemain) melawan Goblin (lawan).

### 🎮 Konsep Utama
- **Battle System**: Setiap pertanyaan yang dijawab benar akan menghasilkan serangan terhadap musuh
- **Animasi Karakter**: Knight dan Goblin memiliki animasi lengkap (Idle, Attack, Hurt, Dead)
- **Real-time Combat**: Sistem pertarungan dinamis dengan efek visual dan feedback haptik
- **Multiplayer**: Mode online untuk bertanding dengan pemain lain

## ✨ Fitur Utama

### 1. **Battle Mode (Offline)**
- Pertarungan PvE melawan AI
- Sistem health bar untuk Knight dan Goblin
- Animasi karakter frame-by-frame dengan 40+ frame per karakter
- Timer 10 detik per pertanyaan
- Efek visual (flash damage, character movement)
- Vibration feedback saat terkena damage

### 2. **Online Multiplayer**
- Real-time battle dengan pemain lain
- Matchmaking system
- Lobby room dengan chat
- Ranked mode dengan sistem rating

### 3. **Social Features**
- Friend list dan friend request
- Chat system (private dan group)
- News feed dengan post & comment
- Profile customization

### 4. **Gamification**
- Leaderboard global dan friends
- Missions dan achievements
- Score tracking
- Battle statistics

### 5. **Account Management**
- Login & Register dengan validasi
- Email verification
- Password reset
- Profile editing
- Settings (notification, sound, language)

## 🛠 Teknologi yang Digunakan

### **Android Development**
- **Bahasa**: Kotlin
- **Minimum SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 36
- **Architecture**: MVVM (Model-View-ViewModel)

### **UI Framework**
- **Classic Android XML Layouts** dengan Material Design 3
- ViewBinding untuk binding views
- ConstraintLayout untuk layout kompleks
- MaterialCardView, MaterialButton untuk UI components

### **Libraries & Dependencies**
```kotlin
// Core Android
- AndroidX Core KTX
- AndroidX AppCompat
- AndroidX Activity
- Material Components

// Architecture Components
- ViewModel & LiveData
- Lifecycle Runtime KTX
- Coroutines (Flow & StateFlow)

// Networking
- Retrofit 2 untuk REST API
- OkHttp3 untuk HTTP client
- Gson untuk JSON parsing

// Image Loading
- Coil untuk image loading & caching

// Dependency Injection
- Hilt (Dagger) untuk DI

// Data Persistence
- Room Database untuk local storage
- DataStore untuk preferences

// Animation
- AnimationDrawable untuk frame-by-frame animation
- Property Animation untuk movement effects
```

### **Backend Integration**
- RESTful API
- JSON data format
- Token-based authentication
- Real-time updates

## 📁 Struktur Proyek

```
QuizBattle/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mytheclipse/quizbattle/
│   │   │   │   ├── data/              # Data layer (models, repositories)
│   │   │   │   ├── viewmodel/         # ViewModels untuk business logic
│   │   │   │   ├── utils/             # Utility classes & helpers
│   │   │   │   ├── BattleActivity.kt  # Main battle screen
│   │   │   │   ├── LoginActivity.kt   # Authentication
│   │   │   │   ├── MainActivity.kt    # Home screen
│   │   │   │   └── [24+ Activity files]
│   │   │   ├── res/
│   │   │   │   ├── drawable/          # 300+ sprite frames & assets
│   │   │   │   ├── layout/            # 27+ XML layout files
│   │   │   │   ├── values/            # Colors, strings, dimensions
│   │   │   │   └── raw/               # Raw resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🎨 Fitur Animasi Battle System

### **Knight Character**
- **Idle**: 4 frames looping animation
- **Attack**: 5 frames oneshot animation (500ms)
- **Hurt**: 2 frames damage animation (200ms)
- **Dead**: 6 frames death animation (900ms)

### **Goblin Character**
- **Idle**: 40 frames looping animation
- **Attack**: 26 frames attack animation (900ms)
- **Hurt**: 11 frames damage animation (300ms)
- **Dead**: 24 frames death animation (800ms)

### **Visual Effects**
- Character movement saat menyerang (forward 80px)
- Recoil effect saat terkena damage (backward 20px)
- Flash damage effect (alpha animation)
- Smooth transitions dengan 300ms duration
- Screen vibration pada damage

## 🔄 Migrasi dari Jetpack Compose ke XML

Proyek ini awalnya dikembangkan menggunakan Jetpack Compose, namun telah sepenuhnya dimigrasikan ke Classic Android XML dengan alasan:
- Kompatibilitas lebih luas (minimum SDK 26 vs 21)
- Performa lebih stabil untuk animasi frame-by-frame
- Kontrol lebih detail untuk custom views
- Maintenance lebih mudah untuk tim

## 🚀 Cara Menjalankan Proyek

### **Prerequisites**
- Android Studio Ladybug atau lebih baru
- JDK 11 atau lebih tinggi
- Android SDK API 26+
- Gradle 8.x

### **Langkah Instalasi**

1. **Clone Repository**
   ```bash
   git clone https://github.com/MythEclipse/QuizBattle.git
   cd QuizBattle
   ```

2. **Buka di Android Studio**
   - Open Android Studio
   - File → Open → Pilih folder QuizBattle
   - Tunggu Gradle sync selesai

3. **Build Project**
   ```bash
   # Windows
   .\gradlew.bat assembleDebug
   
   # Linux/Mac
   ./gradlew assembleDebug
   ```

4. **Run on Emulator/Device**
   - Pilih device atau emulator
   - Klik Run (Shift+F10)
   - Atau gunakan command line:
   ```bash
   .\gradlew.bat installDebug
   ```

## 📱 Screenshots

### Battle Screen
- Knight vs Goblin dengan animasi lengkap
- Health bars untuk kedua karakter
- Timer countdown
- Multiple choice questions dengan 4 pilihan

### Victory/Defeat Screen
- Animasi karakter sesuai hasil
- Score summary
- Rematch button
- Back to menu button

## 🎯 Pembelajaran yang Dicapai

### **Konsep Bahasa Pemrograman 3**
1. **Object-Oriented Programming (OOP)**
   - Class, Interface, Abstract class
   - Inheritance & Polymorphism
   - Encapsulation & Data hiding

2. **Kotlin Advanced Features**
   - Extension functions
   - Higher-order functions & Lambdas
   - Coroutines & Flow
   - Sealed classes & Data classes

3. **Design Patterns**
   - MVVM Architecture pattern
   - Repository pattern
   - Observer pattern (LiveData/Flow)
   - Singleton pattern
   - Factory pattern

4. **Android Framework**
   - Activity lifecycle
   - ViewBinding
   - Animation framework
   - Handler & Runnable
   - Intent & Navigation

5. **Asynchronous Programming**
   - Kotlin Coroutines
   - Flow & StateFlow
   - Suspend functions
   - Dispatchers

6. **Networking & Data**
   - REST API integration
   - JSON parsing
   - HTTP requests
   - Error handling

## 🐛 Bug Fixes & Optimizations

### **Perbaikan Major**
- ✅ Fixed animation ghosting (double rendering)
- ✅ Fixed stuck animation after answering
- ✅ Fixed invisible answer buttons
- ✅ Fixed death animation not showing before result
- ✅ Fixed duplicate navigation to result screen
- ✅ Optimized animation state management
- ✅ Proper cleanup on activity destroy

## 📊 Statistik Proyek

- **Total Activities**: 24+
- **Total XML Layouts**: 27+
- **Total Sprite Frames**: 300+ (Knight & Goblin animations)
- **Lines of Code**: ~5000+ (Kotlin)
- **Build Time**: ~12-50 detik
- **APK Size**: ~15-20 MB (debug)

## 👥 Tim Pengembang

- **Developer**: Asep Haryana Saputra
- **NIM**: 20230810043
- **Kelas**: TINFC-2023-04

## 📄 Lisensi

Proyek ini dibuat untuk keperluan tugas akademik Mata Kuliah Bahasa Pemrograman 3.

## 🙏 Acknowledgments

- Sprite assets: Knight & Goblin character sprites
- Material Design 3 guidelines
- Android Developer Documentation
- Kotlin Documentation

---

## 🛠 Debugging & API Logs

Untuk memudahkan debugging interaksi API (REST dan WebSocket), proyek ini sudah dilengkapi dengan beberapa log dan interceptor:

- OkHttp HttpLoggingInterceptor: menampilkan header dan body HTTP di logcat (aktif di debug build).
- ApiLoggingInterceptor: interceptor kustom yang mencatat request/response dengan format terstruktur, dan menyamarkan header Authorization (hanya aktif di debug build).
- WebSocket logging: menampilkan koneksi WebSocket, pesan masuk/keluar, retry attempts untuk mempermudah tracing pada mode online.
- safeApiCall logging: mencatat start/finish/exception pada panggilan API untuk melihat caller dan waktu eksekusi (hanya di debug build).

Untuk melihat log di Android Studio, jalankan aplikasi pada emulator atau perangkat dan buka Logcat, lalu filter dengan tag `API` atau `WebSocket`.

Contoh filter:
```bash
tag:API
tag:WebSocket
```

Catatan: Semua log verbose hanya aktif pada debug builds; release builds tidak akan mencatat data sensitif.

