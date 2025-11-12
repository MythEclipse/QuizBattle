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
    entities = [User::class, Question::class, GameHistory::class, Friend::class],
    version = 1,
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
            // Insert sample questions for offline mode
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
                )
            )
            
            questionDao.insertQuestions(sampleQuestions)
        }
    }
}
