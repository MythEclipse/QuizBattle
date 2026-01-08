package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.QuestionDao
import com.mytheclipse.quizbattle.data.local.entity.Question
import kotlinx.coroutines.flow.Flow

class QuestionRepository(private val questionDao: QuestionDao) {
    
    fun getAllActiveQuestions(): Flow<List<Question>> {
        return questionDao.getAllActiveQuestions()
    }
    
    suspend fun getQuestionById(questionId: Long): Question? {
        return questionDao.getQuestionById(questionId)
    }
    
    suspend fun getQuestionsByCategory(category: String): List<Question> {
        return questionDao.getQuestionsByCategory(category)
    }
    
    suspend fun getQuestionsByDifficulty(difficulty: String): List<Question> {
        return questionDao.getQuestionsByDifficulty(difficulty)
    }
    
    suspend fun getRandomQuestions(limit: Int = 5): List<Question> {
        return questionDao.getRandomQuestions(limit)
    }
    
    suspend fun getRandomQuestionsByCategory(category: String, limit: Int = 5): List<Question> {
        return questionDao.getRandomQuestionsByCategory(category, limit)
    }
    
    suspend fun insertQuestion(question: Question): Long {
        return questionDao.insertQuestion(question)
    }
    
    suspend fun insertQuestions(questions: List<Question>) {
        questionDao.insertQuestions(questions)
    }
    
    suspend fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }
    
    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteQuestion(question)
    }
    
    suspend fun deleteAllQuestions() {
        questionDao.deleteAllQuestions()
    }
    

    suspend fun getActiveQuestionCount(): Int {
        return questionDao.getActiveQuestionCount()
    }

    suspend fun getUnseenRandomQuestions(userId: Long, limit: Int = 5): List<Question> {
        return questionDao.getUnseenRandomQuestions(userId, limit)
    }

    suspend fun getUnseenRandomQuestionsByCategory(userId: Long, category: String, limit: Int = 5): List<Question> {
        return questionDao.getUnseenRandomQuestionsByCategory(userId, category, limit)
    }

    suspend fun insertUserQuestionHistory(history: List<com.mytheclipse.quizbattle.data.local.entity.UserQuestionHistory>) {
        questionDao.insertUserQuestionHistory(history)
    }

    suspend fun clearUserQuestionHistory(userId: Long) {
        questionDao.clearUserQuestionHistory(userId)
    }
    
    suspend fun ensureMinimumQuestions() {
        // 1. Clean up potential duplicates first (same text, different IDs)
        questionDao.removeDuplicateQuestions()
        
        // 2. Check strict count
        val count = questionDao.getActiveQuestionCount()
        
        // 3. Always try to add sample questions if they are missing (Target: 50+)
        // We compare against the set of existing text to avoid re-inserting duplicates
        val existingTexts = questionDao.getAllQuestionTexts().toHashSet()
        val sampleQuestions = com.mytheclipse.quizbattle.data.QuestionDataProvider.getSampleQuestions()
        
        val newQuestions = sampleQuestions.filter { !existingTexts.contains(it.questionText) }
        
        if (newQuestions.isNotEmpty()) {
            questionDao.insertQuestions(newQuestions)
        }
    }
}
