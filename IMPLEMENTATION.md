# Quiz Battle - Implementasi Database Offline

## Flow Aplikasi (Updated)

### Mode Guest (Default)
Aplikasi sekarang **langsung masuk ke menu utama** tanpa perlu login:
- ✅ Splash Screen → **Main Menu** (Mode Guest)
- ✅ User bisa langsung main quiz offline
- ✅ Login/Register: **Coming Soon** (fitur untuk masa depan)
- ✅ Progress tidak tersimpan untuk guest (hanya untuk demo)

### Mode Login (Coming Soon)
Fitur login akan ditambahkan di masa depan untuk:
- Menyimpan progress dan statistics
- Leaderboard dengan nama user
- Mode online dan challenge teman

## Ringkasan Implementasi

Aplikasi Quiz Battle sekarang telah diimplementasikan dengan sistem database offline menggunakan SQLite melalui Room Database. Ini memungkinkan aplikasi untuk berfungsi sepenuhnya offline tanpa memerlukan koneksi internet.

## Komponen Yang Diimplementasikan

### 1. Database Layer

#### Entities (Data Models)
- **User**: Menyimpan informasi pengguna (username, email, password, points, wins, losses, dll)
- **Question**: Menyimpan pertanyaan quiz dengan 4 pilihan jawaban
- **GameHistory**: Menyimpan riwayat permainan pengguna
- **Friend**: Menyimpan daftar teman pengguna

#### DAO (Data Access Objects)
- **UserDao**: CRUD operations untuk User
- **QuestionDao**: CRUD operations untuk Question
- **GameHistoryDao**: CRUD operations untuk GameHistory
- **FriendDao**: CRUD operations untuk Friend

#### Database
- **QuizBattleDatabase**: Room Database utama yang menghubungkan semua entity dan DAO
- Database otomatis di-populate dengan 15 pertanyaan sample saat pertama kali dibuat

### 2. Repository Layer

- **UserRepository**: Mengelola operasi user (register, login, update stats)
- **QuestionRepository**: Mengelola operasi pertanyaan quiz
- **GameHistoryRepository**: Mengelola riwayat permainan
- **FriendRepository**: Mengelola daftar teman

### 3. ViewModel Layer

- **AuthViewModel**: Mengelola authentication (login & register) dengan validasi
- **MainViewModel**: Mengelola data di halaman utama (leaderboard, user info)
- **BattleViewModel**: Mengelola game logic (pertanyaan, scoring, timer)

### 4. UI Integration

Semua screen telah diintegrasikan dengan ViewModels:
- **LoginScreen**: Terintegrasi dengan AuthViewModel untuk login
- **RegisterScreen**: Terintegrasi dengan AuthViewModel untuk register
- **MainScreen**: Terintegrasi dengan MainViewModel untuk menampilkan leaderboard
- **BattleScreen**: Terintegrasi dengan BattleViewModel untuk gameplay

## Fitur Yang Berfungsi

### Authentication
- ✅ Register user baru dengan validasi
- ✅ Login dengan email & password
- ✅ Validasi input (password minimal 6 karakter, dll)
- ✅ Error handling untuk email/username yang sudah ada

### Gameplay (Mode Offline)
- ✅ Random 5 pertanyaan dari database
- ✅ Timer untuk setiap pertanyaan
- ✅ Scoring system (player vs AI bot)
- ✅ Feedback visual untuk jawaban benar/salah
- ✅ Auto-save game result ke database

### Leaderboard
- ✅ Tampilan top 3 users berdasarkan points
- ✅ Data diambil dari database secara real-time
- ✅ Auto-update setelah game selesai

### Game History
- ✅ Otomatis menyimpan setiap game yang dimainkan
- ✅ Track wins, losses, dan points

## Database Structure

### Sample Questions
Database sudah berisi 15 pertanyaan sample dengan kategori:
- Science (6 pertanyaan)
- History (5 pertanyaan)
- Geography (2 pertanyaan)
- Literature (1 pertanyaan)
- Technology (1 pertanyaan)

## Cara Kerja

### Flow Login/Register:
1. User membuka aplikasi → Splash Screen
2. User memilih Login atau Register
3. Input divalidasi di ViewModel
4. Data disimpan/diverifikasi di database
5. Redirect ke Main Screen jika berhasil

### Flow Gameplay:
1. User klik "Main(offline)" di Main Screen
2. BattleViewModel load 5 pertanyaan random dari database
3. Timer dimulai untuk setiap pertanyaan
4. User memilih jawaban → ViewModel update score
5. Setelah 5 pertanyaan, game over
6. Result disimpan ke GameHistory
7. User stats diupdate (points, wins/losses)
8. Navigate ke Result Screen

### Flow Leaderboard:
1. MainViewModel mengambil top users dari database
2. Data di-sort berdasarkan points
3. UI menampilkan top 3 users
4. Data update otomatis menggunakan Flow

## Dependencies

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
```

## Next Steps (Future Enhancements)

Untuk pengembangan lebih lanjut, bisa ditambahkan:
1. Mode Online dengan API backend
2. Real-time multiplayer menggunakan WebSocket
3. Lebih banyak kategori dan tingkat kesulitan
4. Achievement system
5. Profile customization
6. Chat dengan teman
7. Tournament mode
8. Statistics & analytics

## Testing

Untuk test aplikasi:
1. Build project: `./gradlew build`
2. Run di emulator/device Android
3. Register user baru
4. Login dengan user tersebut
5. Main quiz offline
6. Check leaderboard

## Troubleshooting

Jika ada error build:
1. Sync Gradle: File → Sync Project with Gradle Files
2. Clean build: `./gradlew clean`
3. Rebuild: `./gradlew build`

## Catatan Keamanan

⚠️ **PENTING**: Implementasi ini untuk development/demo purposes. Untuk production:
- Password harus di-hash (gunakan BCrypt atau Argon2)
- Tambahkan input sanitization
- Implementasi proper session management
- Tambahkan rate limiting
- Gunakan encrypted database (SQLCipher)
