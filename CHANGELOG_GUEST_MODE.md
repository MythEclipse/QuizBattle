# Changelog - Guest Mode & Flow Update

## Perubahan Flow Aplikasi

### ✅ Update: Langsung Masuk Menu Utama (Mode Guest)

**Sebelumnya:**
```
Splash Screen → Login Screen → Main Menu
```

**Sekarang:**
```
Splash Screen → Main Menu (Guest Mode)
```

## File Yang Diubah

### 1. `SplashScreen.kt`
- **Perubahan**: `onNavigateToLogin` → `onNavigateToMain`
- **Alasan**: Langsung ke main menu tanpa login

### 2. `Navigation.kt`
- **Perubahan**: Splash screen sekarang navigate ke `Screen.Main` bukan `Screen.Login`
- **Route**: `splash` → `main` (direct)

### 3. `MainScreen.kt`
**Penambahan:**
- Card info user/guest di bagian atas buttons
- Menampilkan "Mode: Guest" jika belum login
- Menampilkan username & points jika sudah login

**Update Buttons:**
- ✅ "Main Quiz (Offline)" - Fully functional
- 🔜 "Login / Register" - Coming Soon badge
- 🔜 "Main Online" - Coming Soon (outlined button)
- 🔜 "Tantangan Teman" - Coming Soon (outlined button)
- ❌ Removed: "Daftar Teman" button

**Leaderboard:**
- Menampilkan placeholder "Player 1/2/3" jika database kosong
- Tetap functional untuk menampilkan real users dari database

### 4. `MainViewModel.kt`
- **Tidak ada perubahan**: Sudah support guest mode (currentUser bisa null)

### 5. `BattleViewModel.kt`
- **Tidak ada perubahan**: Sudah check `if (currentUser != null)` sebelum save
- Guest bisa main tapi progress tidak disimpan

## Fitur Mode Guest

### ✅ Yang Bisa Dilakukan (Guest):
1. Main quiz offline
2. Lihat leaderboard
3. Main berulang kali
4. Lihat result screen

### ❌ Yang Tidak Bisa (Guest):
1. Save progress/statistics
2. Login/Register (Coming Soon)
3. Main online (Coming Soon)
4. Challenge teman (Coming Soon)

## User Experience

### Pertama Kali Buka App:
1. **Splash Screen** (2 detik)
   - Logo Quiz Battle dengan gradient

2. **Main Menu - Guest Mode**
   - Info: "Mode: Guest"
   - Subtext: "Login untuk menyimpan progress"
   - Leaderboard placeholder
   - Button: "Main Quiz (Offline)" ← Langsung bisa main!
   - Button: "Login / Register" + badge "Coming Soon"

3. **Main Quiz**
   - 5 pertanyaan random
   - Timer per soal
   - VS AI Bot
   - Real-time feedback

4. **Result Screen**
   - Victory/Defeat animation
   - Buttons: Main Menu | Rematch

### Flow Lengkap:
```
┌─────────────┐
│   Splash    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│    Main Menu        │
│   (Guest Mode)      │
│                     │
│  - Leaderboard      │
│  - Guest Info       │
│  - Main Quiz ✓      │
│  - Login 🔜         │
│  - Online 🔜        │
│  - Challenge 🔜     │
└──────┬──────────────┘
       │ (click Main Quiz)
       ▼
┌─────────────────────┐
│   Battle Screen     │
│  (5 questions)      │
│  Player vs AI Bot   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│   Result Screen     │
│  Victory / Defeat   │
│  [Main Menu][Rematch]│
└─────────────────────┘
```

## Testing Guide

### Test Scenario 1: First Time User (Guest)
1. ✅ Open app
2. ✅ See splash screen
3. ✅ Auto-navigate to main menu
4. ✅ See "Mode: Guest" card
5. ✅ See placeholder leaderboard
6. ✅ Click "Main Quiz (Offline)"
7. ✅ Play quiz (5 questions)
8. ✅ See result screen
9. ✅ Click "Main Menu" to return

### Test Scenario 2: Coming Soon Features
1. ✅ Click "Login / Register" button
2. ✅ Nothing happens (Coming Soon)
3. ✅ Click "Main Online" button
4. ✅ Nothing happens (Coming Soon)
5. ✅ See "Coming Soon" text below Login button

### Test Scenario 3: Leaderboard
1. ✅ Fresh install: Shows "Player 1, 2, 3" with 0 pts
2. ✅ After some users login: Shows real usernames & points

## Future Development

Untuk mengaktifkan Login/Register (ketika siap):
1. Uncomment/enable onClick di "Login / Register" button
2. Navigate ke `Screen.Login`
3. Remove "Coming Soon" badge
4. User flow akan jadi: Login → Save progress → Leaderboard update

## Notes

- Database tetap functional (create saat install)
- 15 sample questions sudah ada
- AuthViewModel, UserRepository, dll tetap ada (ready untuk future)
- Game statistics tracking ready (hanya tidak save untuk guest)
