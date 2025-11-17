CATATAN: Permintaan asli file menggunakan `untitled:plan-composeToXml.prompt.md`. Nama file tersebut mengandung karakter ':' (colon) yang tidak diperbolehkan pada Windows. Saya membuat file ini dengan nama yang aman: `untitled-plan-composeToXml.prompt.md`.

# TODO: Migrasi Compose → Classic XML Layout & Activity (Intent-based) ✅

TL;DR: Migrasi bertahap semua layar Compose menjadi layout XML (`app/src/main/res/layout/*`) dan Activity/Activity hosts yang terpisah dengan Intent navigation. Setelah semua layar dimigrasi, hapus plugin & dependency Compose dari Gradle. Pastikan semua fitur, state, animasi & ViewModel tetap bekerja sama seperti fitur awal.

---

## 🔧 Persiapan (Sebelum mulai)
- [ ] Buat branch backup: `git checkout -b feature/migrate-compose-to-xml`
- [ ] Tag/backup komponen Compose saat ini untuk rollback.
- [ ] Setup `viewBinding` di `app/build.gradle.kts` (sementara `compose` tetap aktif sampai semua migrasi selesai).
- [ ] Tambahkan `com.google.android.material:material` dependency jika belum:
  - `implementation("com.google.android.material:material:...")`
- [ ] Tentukan strategi migrasi: Full (big-bang) / Phased (direkomendasikan) / Hybrid.
- [ ] Pilih screen POC: (rekomendasi) `Login` atau `Main`.

---

## 📋 Checklist Umum Per-Screen
Untuk setiap screen Compose, lakukan langkah-langkah berikut:

- [ ] Buat XML layout: `app/src/main/res/layout/activity_<screen>.xml`
- [ ] Buat Activity: `app/src/main/java/.../<Screen>Activity.kt` (atau gunakan Activity yang sudah ada)
- [ ] Enable & gunakan `viewBinding`:
  - `private lateinit var binding: ActivityXBinding` dan `binding = ActivityXBinding.inflate(layoutInflater)`
  - `setContentView(binding.root)`
- [ ] Pindahkan UI & styling dari Composable → XML Views (TextView, EditText, RecyclerView, ConstraintLayout, MotionLayout, Material Buttons).
- [ ] Migrasikan state interaction → ViewModel:
  - Ubah `viewModel()` ke `private val vm: XViewModel by viewModels()` atau DI provider.
- [ ] Migrasikan event callbacks:
  - `onNavigateToX` → `startActivity(Intent(this, XActivity::class.java))`
- [ ] Migrasikan composable-only utils (e.g., `remember`, `LaunchedEffect`, `LocalContext`) ke lifecycle-safe code di Activity/ViewModel.
- [ ] Recreate list adapters: RecyclerView + Adapter/Binding for list-like Compose lists.
- [ ] Migrasikan animations: Lottie / MotionLayout / custom View animations for transitions & animated elements.
- [ ] Update manifest: tambahkan Activity entry di `AndroidManifest.xml` jika baru.
- [ ] Transfer strings → `strings.xml`, colors → `colors.xml`, dimens → `dimens.xml`, icons/ assets ke `res/`.
- [ ] Update tests & add UI tests (Espresso) if present.
- [ ] QA Checklist: navigation, back-stack behavior, orientation changes, state restore, performance.

---

## 🔁 Navigation & Flow
- [ ] Stop using `QuizBattleNavigation()` / `NavHost` (unless migrating to single-activity). Replace Compose navigation with Intent-based navigation:
  - Replace `navController.navigate` & `composable` usages with `Intent`.
- [ ] For deep/back-stack, maintain `finish()` and `flags` behavior as needed.
- [ ] Add `overridePendingTransition` or shared element transitions if needed to mimic Compose transitions.

---

## 🧩 List Per-Screen (Recommendation order + specific names)
Gunakan tabel ini untuk memprioritaskan migrasi (POC 1-2 screens):

| No | Screen (Compose) | New Activity | Layout file |
|----|------------------|--------------|-------------|
| 1 | SplashScreen | `SplashActivity` | `activity_splash.xml` |
| 2 | LoginScreen | `LoginActivity` | `activity_login.xml` |
| 3 | RegisterScreen | `RegisterActivity` | `activity_register.xml` |
| 4 | ResetPasswordScreen | `ResetPasswordActivity` | `activity_reset_password.xml` |
| 5 | MainScreen (Home) | `MainActivity` | `activity_main.xml` |
| 6 | BattleScreen (Offline) | `BattleActivity` | `activity_battle.xml` |
| 7 | BattleResultScreen | `BattleResultActivity` | `activity_battle_result.xml` |
| 8 | OnlineMenu/OnlineBattle/Matchmaking | `OnlineMenuActivity`, `OnlineBattleActivity`, `MatchmakingActivity` | `activity_online_menu.xml`, `activity_online_battle.xml`, `activity_matchmaking.xml` |
| 9 | LobbyList/LobbyRoom | `LobbyListActivity`, `LobbyRoomActivity` | `activity_lobby_list.xml`, `activity_lobby_room.xml` |
| 10 | Feed/CreatePost | `FeedActivity`, `CreatePostActivity` | `activity_feed.xml`, `activity_create_post.xml` |
| 11 | Profile/EditProfile | `ProfileActivity`, `EditProfileActivity` | `activity_profile.xml`, `activity_edit_profile.xml` |
| 12 | Leaderboard/Ranked/Missions | `LeaderboardActivity`, `RankedActivity`, `MissionsActivity` | `activity_leaderboard.xml`, ... |
| 13 | ChatList/ChatRoom | `ChatListActivity`, `ChatRoomActivity` | `activity_chat_list.xml`, `activity_chat_room.xml` |
| 14 | Notifications | `NotificationActivity` | `activity_notification.xml` |
| 15 | Settings | `SettingsActivity` | `activity_settings.xml` |

> Catatan: Bisa menyesuaikan nama Activity sesuai preferensi tim. Utamakan re-using Activity jika konsepnya “tab” dan lebih cocok jadi Fragment.

---

## 🧭 Komponen UI & Utilities yang harus dibuat ulang
- [ ] Buttons: `QuizBattleButton`, `OutlinedButton` → custom Material Buttons in XML or `styles`.
- [ ] TextFields: convert Compose `TextField` to `TextInputLayout + EditText`.
- [ ] List Items: `Card`, `Row`, `Column` → XML item layouts + `RecyclerView`.
- [ ] Dialogs: Compose dialogs → `AlertDialog` or `DialogFragment`.
- [ ] Snackbar: `Snackbar` from Material.
- [ ] Toasts & Notifications: map Compose usage to Android equivalents.
- [ ] Loading skeletons: Placeholder views or Shimmer library.
- [ ] Reusable components: move to `res/layout/include_*` for reuse.
- [ ] Lottie or AnimatedVectorDrawable for animations.

---

## 🧠 ViewModel & State Migration
- [ ] Migrate `@Composable` stateful logic to ViewModel (LiveData / StateFlow).
- [ ] Where Compose used `viewModel()` or `collectAsState()`, change to `collect/observe` in Activity:
  - `lifecycleScope.launchWhenStarted { vm.state.collect { /* update UI */ } }`
- [ ] Use `SavedStateHandle` for saved state.

---

## 🗂 Resources & Theme
- [ ] Implement theme in XML:
  - `res/values/themes.xml`, `styles.xml`, `colors.xml`, `dimens.xml`.
  - Recreate `QuizBattleTheme` styles (Material Components).
- [ ] Import fonts, images, svg assets into `res/`.
- [ ] Ensure color and typographic parity (Material 3 -> Material Components mapping).

---

## ✅ Animations & Transitions
- [ ] Convert animated Compose elements to `MotionLayout` / `Lottie`.
- [ ] For custom animations, replicate via `ObjectAnimator` / `AnimatorSet` or `Drawable` animations.

---

## 🔎 Camera/Media/Other platform features
- [ ] Any Compose-based camera/permissions usage → refactor to platform APIs, `Activity` lifecycle, or `CameraX`.
- [ ] Audio haptic utils: move to non-Compose singleton/service usage.

---

## 🧪 Testing & QA
- [ ] Write Espresso tests for each converted screen flows (login → main, battle flow, create post).
- [ ] Unit test ViewModel transformations.
- [ ] Regression test for animations/transition flows.
- [ ] E2E tests for major flows: Login to Battle Result, Online Lobby -> Matchmaking -> Online Battle.

---

## 🧹 Cleanup Gradle & Repo (After all screens migrated)
- [ ] Remove:
  - `alias(libs.plugins.kotlin.compose)` plugin
  - `buildFeatures { compose = true }`
  - Compose dependencies: `androidx.compose.*`, `navigation-compose`, `coil.compose`, `compose.ui.tooling` (and other Compose libraries)
- [ ] Add or verify:
  - `viewBinding` enabled in `android.buildFeatures`:
    ```kotlin
    buildFeatures {
      viewBinding = true
    }
    ```
- [ ] Update `libs.versions.toml` and `gradle.properties` accordingly
- [ ] Remove or archive `@Composable` and Compose-based screens.

---

## 🔁 Rollout & Monitoring
- [ ] Migrate POC screens → run nightly builds → test.
- [ ] Release internal QA build to testers (feature/alpha track).
- [ ] Rollout monitoring: track crash reports and performance issues post-migration.
- [ ] Keep fallback plan: revert to Compose branch if critical blocks.

---

## ⚠️ Edge cases & migration risks
- Animations & UX parity: may not be identically reproducible; need to define acceptable approximations.
- Time cost: converting all Compose screens is manual and heavy.
- ViewModel coupling: some utilities rely on Compose-specific lifecycle features and need full refactor.
- Navigation Compose graph currently unused — if reusing Graph for Fragment navigation, conversion approach changes.

---

## 📌 Post-Migration Tasks
- [ ] Run full static code analysis / Lint and fix warnings
- [ ] Remove unused resources (images, strings)
- [ ] Update README & developer docs explaining navigation & build config changes
- [ ] Create migration summary & test checklist in repo

---

## 💡 Rekomendasi Implementasi POC
- POC screen: `Login` atau `Main` (priority).
- Build POC dengan:
  1. `activity_login.xml` layout
  2. `LoginActivity.kt` with `viewBinding` & `LoginViewModel`
  3. `strings.xml`, `colors.xml` declarations
  4. `test` untuk login success path

Setelah POC stabil, gunakan checklist di atas untuk iterasi pada tiap screen.

---

Jika kamu setuju, saya siap membuat POC `LoginScreen` → `LoginActivity` + layout `activity_login.xml` serta contoh porting `LoginViewModel` dan `RecyclerView adapter` contoh untuk list. Mau saya mulai dari `Login` atau `Main`?
