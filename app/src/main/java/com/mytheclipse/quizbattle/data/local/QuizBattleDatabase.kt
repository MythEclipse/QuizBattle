package com.mytheclipse.quizbattle.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mytheclipse.quizbattle.data.local.dao.*
import com.mytheclipse.quizbattle.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class, 
        Question::class, 
        GameHistory::class, 
        Friend::class,
        com.mytheclipse.quizbattle.data.local.entity.UserQuestionHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuizBattleDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun questionDao(): QuestionDao
    abstract fun gameHistoryDao(): GameHistoryDao
    abstract fun friendDao(): FriendDao
    
    companion object {
        @Volatile
        private var INSTANCE: QuizBattleDatabase? = null
        
        fun getDatabase(context: Context): QuizBattleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizBattleDatabase::class.java,
                    "quiz_battle_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.questionDao())
                    }
                }
            }
        }
        
        private suspend fun populateDatabase(questionDao: QuestionDao) {
            val sampleQuestions = listOf(
                Question(
                    questionText = "Benda langit yang dikenal sebagai \"Bintang Kejora\" atau \"Bintang Fajar\" adalah:",
                    answer1 = "Mars",
                    answer2 = "Venus",
                    answer3 = "Jupiter",
                    answer4 = "Saturnus",
                    correctAnswerIndex = 1,
                    category = "Science",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Unsur kimia dengan lambang Fe dan nomor atom 26 dikenal sebagai",
                    answer1 = "Emas (Gold)",
                    answer2 = "Perak (Silver)",
                    answer3 = "Besi (Iron)",
                    answer4 = "Tembaga (Copper)",
                    correctAnswerIndex = 2,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Siapakah pelukis terkenal asal Belanda yang dikenal dengan karyanya The Starry Night?",
                    answer1 = "Pablo Picasso",
                    answer2 = "Claude Monet",
                    answer3 = "Vincent van Gogh",
                    answer4 = "Leonardo da Vinci",
                    correctAnswerIndex = 2,
                    category = "Art",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Ibu kota Indonesia sebelum Jakarta adalah:",
                    answer1 = "Bandung",
                    answer2 = "Surabaya",
                    answer3 = "Yogyakarta",
                    answer4 = "Semarang",
                    correctAnswerIndex = 2,
                    category = "History",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa penemu bola lampu?",
                    answer1 = "Thomas Edison",
                    answer2 = "Nikola Tesla",
                    answer3 = "Alexander Graham Bell",
                    answer4 = "Benjamin Franklin",
                    correctAnswerIndex = 0,
                    category = "History",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Planet terbesar di tata surya adalah:",
                    answer1 = "Saturnus",
                    answer2 = "Uranus",
                    answer3 = "Jupiter",
                    answer4 = "Neptunus",
                    correctAnswerIndex = 2,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Bahasa pemrograman yang dikembangkan oleh Google untuk Android adalah:",
                    answer1 = "Java",
                    answer2 = "Swift",
                    answer3 = "Kotlin",
                    answer4 = "C++",
                    correctAnswerIndex = 2,
                    category = "Technology",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Tahun berapa Indonesia merdeka?",
                    answer1 = "1942",
                    answer2 = "1945",
                    answer3 = "1949",
                    answer4 = "1950",
                    correctAnswerIndex = 1,
                    category = "History",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Siapa presiden pertama Indonesia?",
                    answer1 = "Soekarno",
                    answer2 = "Soeharto",
                    answer3 = "Habibie",
                    answer4 = "Megawati",
                    correctAnswerIndex = 0,
                    category = "History",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Berapa jumlah pulau di Indonesia menurut data terbaru?",
                    answer1 = "13.466",
                    answer2 = "15.000",
                    answer3 = "17.504",
                    answer4 = "20.000",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Hard"
                ),
                Question(
                    questionText = "Apa ibu kota Australia?",
                    answer1 = "Sydney",
                    answer2 = "Melbourne",
                    answer3 = "Canberra",
                    answer4 = "Brisbane",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa penulis novel \"Laskar Pelangi\"?",
                    answer1 = "Andrea Hirata",
                    answer2 = "Dee Lestari",
                    answer3 = "Tere Liye",
                    answer4 = "Pramoedya Ananta Toer",
                    correctAnswerIndex = 0,
                    category = "Literature",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa satuan pengukuran kekuatan gempa?",
                    answer1 = "Celsius",
                    answer2 = "Richter",
                    answer3 = "Pascal",
                    answer4 = "Newton",
                    correctAnswerIndex = 1,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Berapa lama waktu yang dibutuhkan cahaya matahari untuk sampai ke Bumi?",
                    answer1 = "8 menit",
                    answer2 = "8 detik",
                    answer3 = "8 jam",
                    answer4 = "8 hari",
                    correctAnswerIndex = 0,
                    category = "Science",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa tokoh yang dijuluki \"Bapak Teknologi Indonesia\"?",
                    answer1 = "B.J. Habibie",
                    answer2 = "Ki Hajar Dewantara",
                    answer3 = "Mohammad Hatta",
                    answer4 = "Sutan Sjahrir",
                    correctAnswerIndex = 0,
                    category = "History",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Hewan apa yang menjadi simbol negara Australia?",
                    answer1 = "Koala",
                    answer2 = "Kangguru",
                    answer3 = "Platipus",
                    answer4 = "Emu",
                    correctAnswerIndex = 1,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa nama mata uang Jepang?",
                    answer1 = "Won",
                    answer2 = "Yuan",
                    answer3 = "Yen",
                    answer4 = "Baht",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Candi Buddha terbesar di dunia adalah:",
                    answer1 = "Prambanan",
                    answer2 = "Angkor Wat",
                    answer3 = "Borobudur",
                    answer4 = "Mendut",
                    correctAnswerIndex = 2,
                    category = "History",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapakah pendiri Microsoft?",
                    answer1 = "Steve Jobs",
                    answer2 = "Bill Gates",
                    answer3 = "Mark Zuckerberg",
                    answer4 = "Elon Musk",
                    correctAnswerIndex = 1,
                    category = "Technology",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa rumus kimia air?",
                    answer1 = "CO2",
                    answer2 = "H2O",
                    answer3 = "NaCl",
                    answer4 = "O2",
                    correctAnswerIndex = 1,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Negara mana yang memenangkan Piala Dunia FIFA 2022?",
                    answer1 = "Prancis",
                    answer2 = "Brasil",
                    answer3 = "Argentina",
                    answer4 = "Jerman",
                    correctAnswerIndex = 2,
                    category = "Sports",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Apa nama samudra terluas di dunia?",
                    answer1 = "Atlantik",
                    answer2 = "Hindia",
                    answer3 = "Pasifik",
                    answer4 = "Arktik",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Berapa jumlah pemain dalam satu tim sepak bola?",
                    answer1 = "9",
                    answer2 = "10",
                    answer3 = "11",
                    answer4 = "12",
                    correctAnswerIndex = 2,
                    category = "Sports",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Siapa penemu telepon?",
                    answer1 = "Alexander Graham Bell",
                    answer2 = "Thomas Edison",
                    answer3 = "Nikola Tesla",
                    answer4 = "Guglielmo Marconi",
                    correctAnswerIndex = 0,
                    category = "History",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Apa ibu kota provinsi Jawa Barat?",
                    answer1 = "Bogor",
                    answer2 = "Bandung",
                    answer3 = "Bekasi",
                    answer4 = "Cirebon",
                    correctAnswerIndex = 1,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Gas apa yang paling banyak terkandung di atmosfer Bumi?",
                    answer1 = "Oksigen",
                    answer2 = "Karbon Dioksida",
                    answer3 = "Nitrogen",
                    answer4 = "Hidrogen",
                    correctAnswerIndex = 2,
                    category = "Science",
                    difficulty = "Hard"
                ),
                Question(
                    questionText = "Alat musik tradisional Angklung berasal dari daerah mana?",
                    answer1 = "Jawa Tengah",
                    answer2 = "Bali",
                    answer3 = "Jawa Barat",
                    answer4 = "Sumatera Utara",
                    correctAnswerIndex = 2,
                    category = "Art",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa pelukis lukisan Monalisa?",
                    answer1 = "Michelangelo",
                    answer2 = "Raphael",
                    answer3 = "Leonardo da Vinci",
                    answer4 = "Donatello",
                    correctAnswerIndex = 2,
                    category = "Art",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Apa nama gunung tertinggi di dunia?",
                    answer1 = "K2",
                    answer2 = "Kilimanjaro",
                    answer3 = "Everest",
                    answer4 = "Fuji",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Makanan khas Sumatera Barat yang dinobatkan sebagai makanan terenak di dunia adalah:",
                    answer1 = "Sate Padang",
                    answer2 = "Rendang",
                    answer3 = "Dendeng Balado",
                    answer4 = "Ayam Pop",
                    correctAnswerIndex = 1,
                    category = "General Knowledge",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Berapa hasil dari 12 dikali 12?",
                    answer1 = "124",
                    answer2 = "144",
                    answer3 = "142",
                    answer4 = "122",
                    correctAnswerIndex = 1,
                    category = "Mathematics",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa nama planet merah?",
                    answer1 = "Mars",
                    answer2 = "Venus",
                    answer3 = "Jupiter",
                    answer4 = "Merkurius",
                    correctAnswerIndex = 0,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Siapa pendiri Facebook?",
                    answer1 = "Jeff Bezos",
                    answer2 = "Mark Zuckerberg",
                    answer3 = "Jack Dorsey",
                    answer4 = "Tim Cook",
                    correctAnswerIndex = 1,
                    category = "Technology",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Hewan apa yang bisa mengubah warna kulitnya?",
                    answer1 = "Tokek",
                    answer2 = "Bunglon",
                    answer3 = "Kadal",
                    answer4 = "Iguana",
                    correctAnswerIndex = 1,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Danau terbesar di Indonesia adalah:",
                    answer1 = "Danau Toba",
                    answer2 = "Danau Singkarak",
                    answer3 = "Danau Maninjau",
                    answer4 = "Danau Batur",
                    correctAnswerIndex = 0,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa nama ibu kota Amerika Serikat?",
                    answer1 = "New York",
                    answer2 = "Los Angeles",
                    answer3 = "Washington D.C.",
                    answer4 = "Chicago",
                    correctAnswerIndex = 2,
                    category = "Geography",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Mamalia terbesar di dunia adalah:",
                    answer1 = "Gajah Afrika",
                    answer2 = "Paus Biru",
                    answer3 = "Hiu Paus",
                    answer4 = "Jerapah",
                    correctAnswerIndex = 1,
                    category = "Science",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Hari Batik Nasional diperingati setiap tanggal:",
                    answer1 = "17 Agustus",
                    answer2 = "2 Mei",
                    answer3 = "2 Oktober",
                    answer4 = "10 November",
                    correctAnswerIndex = 2,
                    category = "General Knowledge",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa pencipta lagu Indonesia Raya?",
                    answer1 = "Ismail Marzuki",
                    answer2 = "W.R. Supratman",
                    answer3 = "C. Simanjuntak",
                    answer4 = "Ibu Sud",
                    correctAnswerIndex = 1,
                    category = "History",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Berapa jumlah sisi pada bangun segienam?",
                    answer1 = "4",
                    answer2 = "5",
                    answer3 = "6",
                    answer4 = "8",
                    correctAnswerIndex = 2,
                    category = "Mathematics",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Apa kepanjangan dari CPU?",
                    answer1 = "Central Process Unit",
                    answer2 = "Central Processing Unit",
                    answer3 = "Computer Personal Unit",
                    answer4 = "Central Personal Unit",
                    correctAnswerIndex = 1,
                    category = "Technology",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Sungai terpanjang di dunia adalah:",
                    answer1 = "Sungai Amazon",
                    answer2 = "Sungai Nil",
                    answer3 = "Sungai Yangtze",
                    answer4 = "Sungai Mississippi",
                    correctAnswerIndex = 1,
                    category = "Geography",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa atlet bulu tangkis Indonesia peraih medali emas Olimpiade Barcelona 1992?",
                    answer1 = "Taufik Hidayat",
                    answer2 = "Susi Susanti",
                    answer3 = "Liliyana Natsir",
                    answer4 = "Greysia Polii",
                    correctAnswerIndex = 1,
                    category = "Sports",
                    difficulty = "Hard"
                ),
                Question(
                    questionText = "Apa nama menara terkenal di Paris?",
                    answer1 = "Pisa",
                    answer2 = "Eiffel",
                    answer3 = "Big Ben",
                    answer4 = "Tokyo Tower",
                    correctAnswerIndex = 1,
                    category = "Geography",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Berapa jumlah detik dalam satu jam?",
                    answer1 = "60",
                    answer2 = "360",
                    answer3 = "3600",
                    answer4 = "6000",
                    correctAnswerIndex = 2,
                    category = "Mathematics",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Siapa karakter fiksi detektif ciptaan Sir Arthur Conan Doyle?",
                    answer1 = "Hercule Poirot",
                    answer2 = "Sherlock Holmes",
                    answer3 = "James Bond",
                    answer4 = "Arsene Lupin",
                    correctAnswerIndex = 1,
                    category = "Literature",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Apa warna primer dalam seni rupa?",
                    answer1 = "Merah, Kuning, Hijau",
                    answer2 = "Merah, Biru, Kuning",
                    answer3 = "Hitam, Putih, Abu-abu",
                    answer4 = "Merah, Hijau, Biru",
                    correctAnswerIndex = 1,
                    category = "Art",
                    difficulty = "Medium"
                ),
                Question(
                    questionText = "Lagu kebangsaan Amerika Serikat berjudul:",
                    answer1 = "God Save the Queen",
                    answer2 = "The Star-Spangled Banner",
                    answer3 = "La Marseillaise",
                    answer4 = "Waltzing Matilda",
                    correctAnswerIndex = 1,
                    category = "General Knowledge",
                    difficulty = "Hard"
                ),
                Question(
                    questionText = "Berapa kaki yang dimiliki laba-laba?",
                    answer1 = "4",
                    answer2 = "6",
                    answer3 = "8",
                    answer4 = "10",
                    correctAnswerIndex = 2,
                    category = "Science",
                    difficulty = "Easy"
                ),
                Question(
                    questionText = "Rumah adat dari Sumatera Barat disebut:",
                    answer1 = "Rumah Joglo",
                    answer2 = "Rumah Gadang",
                    answer3 = "Rumah Honai",
                    answer4 = "Tongkonan",
                    correctAnswerIndex = 1,
                    category = "Culture",
                    difficulty = "Medium"
                )
            )
            
            questionDao.insertQuestions(sampleQuestions)
        }
    }
}