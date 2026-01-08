# Verifikasi Fitur Aplikasi (Requirements B1-B10)

Dokumen ini menjelaskan bagaimana aplikasi **QuizBattle** memenuhi semua persyaratan teknis yang ditetapkan (B1 sampai B10).

## B1: Aplikasi dibuat dengan Kotlin

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Seluruh kode sumber (Source Code) aplikasi ditulis menggunakan bahasa pemrograman **Kotlin** (`.kt`).
- **Bukti**:
  - `MainActivity.kt`
  - `BattleActivity.kt`
  - `QuestionManagementActivity.kt`
  - Semua ViewModel, Repository, dan DAO.

## B2: Minimal 2 Activity digunakan

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi menggunakan lebih dari 2 Activity untuk navigasi antar fitur.
- **Daftar Activity**:
  1. `LoginActivity` (Login/Register)
  2. `MainActivity` (Menu Utama)
  3. `BattleActivity` (Gameplay Offline)
  4. `QuestionManagementActivity` (CRUD Soal)
  5. `LeaderboardActivity` (Papan Skor)
  6. `ProfileActivity` (Profil User)

## B3: Intent Explicit berjalan

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Navigasi antar halaman menggunakan **Explicit Intent**.
- **Contoh Kode** (`MainActivity.kt`):
  ```kotlin
  val intent = Intent(this, BattleActivity::class.java)
  startActivity(intent)
  ```

## B4: RecyclerView tampil dengan benar

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: RecyclerView digunakan untuk menampilkan daftar yang panjang secara efisien.
- **Lokasi**:
  - `QuestionManagementActivity`: Menampilkan daftar soal.
  - `LeaderboardActivity`: Menampilkan daftar skor.

## B5: RecyclerView terhubung dengan Adapter

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Setiap RecyclerView memiliki Adapter khusus (turunan `RecyclerView.Adapter` atau `ListAdapter`) untuk mengikat data ke tampilan (ViewHolder).
- **Bukti**:
  - `QuestionAdapter.kt`: Menghubungkan data `Question` ke `QuestionManagementActivity`.
  - `LeaderboardAdapter.kt`: Menghubungkan data `User` ke `LeaderboardActivity`.

## B6: Create data (Insert SQLite)

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi dapat menambahkan data baru ke dalam database SQLite (via Room).
- **Implementasi**:
  - Fitur **Tambah Soal** di `QuestionManagementActivity` memanggil `viewModel.addQuestion()` yang melakukan `INSERT` ke tabel `questions`.
  - Registrasi user melakukan INSERT ke tabel `users`.

## B7: Read data (Tampil di RecyclerView)

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi membaca data dari database dan menampilkannya dalam list.
- **Implementasi**:
  - `QuestionManagementActivity` mengamati `viewModel.state.questions` (via Flow/LiveData) yang diambil dari database (`SELECT * FROM questions`) dan menampilkannya di RecyclerView.

## B8: Update data

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi dapat mengubah data yang sudah ada di database.
- **Implementasi**:
  - Fitur **Edit Soal** di `QuestionManagementActivity` memungkinkan pengguna mengubah teks soal atau jawaban. Tombol "Simpan" memanggil `viewModel.updateQuestion()` yang melakukan `UPDATE` SQL.

## B9: Delete data

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi dapat menghapus data dari database.
- **Implementasi**:
  - Fitur **Hapus Soal** (ikon sampah) di `QuestionManagementActivity` memanggil `viewModel.deleteQuestion()` yang melakukan `DELETE` SQL.

## B10: Tidak ada crash saat dijalankan

- **Status**: ✅ **Terpenuhi**
- **Penjelasan**: Aplikasi telah melalui proses debugging dan perbaikan (termasuk perbaikan crash "Asset Loading" dan duplikasi soal).
- **Verifikasi**: Build terakhir (`./gradlew assembleDebug`) sukses dan alur utama (Login -> Battle -> Result) berjalan lancar.
