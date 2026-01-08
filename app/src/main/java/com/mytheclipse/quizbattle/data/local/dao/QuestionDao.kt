package com.mytheclipse.quizbattle.data.local.dao

import androidx.room.*
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.data.local.entity.UserQuestionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): Question?
    
    @Query("SELECT * FROM questions WHERE isActive = 1")
    fun getAllActiveQuestions(): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE category = :category AND isActive = 1")
    suspend fun getQuestionsByCategory(category: String): List<Question>
    
    @Query("SELECT * FROM questions WHERE difficulty = :difficulty AND isActive = 1")
    suspend fun getQuestionsByDifficulty(difficulty: String): List<Question>
    
    @Query("SELECT * FROM questions WHERE isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int = 5): List<Question>
    
    @Query("""
        SELECT * FROM questions 
        WHERE isActive = 1 
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
        ORDER BY RANDOM() 
        LIMIT :limit
    """)
    suspend fun getUnseenRandomQuestions(userId: Long, limit: Int = 5): List<Question>
    
    @Query("""
        SELECT * FROM questions 
        WHERE category = :category 
        AND isActive = 1 
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
        ORDER BY RANDOM() 
        LIMIT :limit
    """)
    suspend fun getUnseenRandomQuestionsByCategory(userId: Long, category: String, limit: Int = 5): List<Question>
    
    @Query("SELECT * FROM questions WHERE category = :category AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByCategory(category: String, limit: Int = 5): List<Question>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserQuestionHistory(history: List<com.mytheclipse.quizbattle.data.local.entity.UserQuestionHistory>)
    
    @Query("DELETE FROM user_question_history WHERE userId = :userId")
    suspend fun clearUserQuestionHistory(userId: Long)
    
    @Update
    suspend fun updateQuestion(question: Question)
    
    @Delete
    suspend fun deleteQuestion(question: Question)
    
    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
    
    @Query("SELECT COUNT(*) FROM questions WHERE isActive = 1")
    suspend fun getActiveQuestionCount(): Int
}
