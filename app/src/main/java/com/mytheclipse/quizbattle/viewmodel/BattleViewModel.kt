package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.data.repository.GameHistoryRepository
import com.mytheclipse.quizbattle.data.repository.QuestionRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class BattleQuestion(
    val id: Long,
    val text: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

data class BattleState(
    val questions: List<BattleQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val isAnswered: Boolean = false,
    val selectedAnswerIndex: Int = -1,
    val timeProgress: Float = 1f,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BattleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = QuizBattleDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    private val userRepository = UserRepository(database.userDao())
    private val gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    
    private val _state = MutableStateFlow(BattleState())
    val state: StateFlow<BattleState> = _state.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val questions = questionRepository.getRandomQuestions(5)
                val battleQuestions = questions.map { question ->
                    BattleQuestion(
                        id = question.id,
                        text = question.questionText,
                        answers = listOf(
                            question.answer1,
                            question.answer2,
                            question.answer3,
                            question.answer4
                        ),
                        correctAnswerIndex = question.correctAnswerIndex
                    )
                }
                
                _state.value = _state.value.copy(
                    questions = battleQuestions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat pertanyaan"
                )
            }
        }
    }
    
    fun answerQuestion(answerIndex: Int) {
        val currentState = _state.value
        if (currentState.isAnswered || currentState.questions.isEmpty()) return
        
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = answerIndex == currentQuestion.correctAnswerIndex
        
        // Calculate opponent's random answer
        val opponentCorrect = Random.nextBoolean()
        
        _state.value = currentState.copy(
            isAnswered = true,
            selectedAnswerIndex = answerIndex,
            playerScore = if (isCorrect) currentState.playerScore + 1 else currentState.playerScore,
            opponentScore = if (opponentCorrect) currentState.opponentScore + 1 else currentState.opponentScore
        )
    }
    
    fun nextQuestion() {
        val currentState = _state.value
        if (!currentState.isAnswered) return
        
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _state.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                isAnswered = false,
                selectedAnswerIndex = -1,
                timeProgress = 1f
            )
        } else {
            // Game over
            _state.value = currentState.copy(isGameOver = true)
            saveGameResult()
        }
    }
    
    fun timeUp() {
        val currentState = _state.value
        if (currentState.isAnswered) return
        
        // Opponent has a chance to answer
        val opponentCorrect = Random.nextBoolean()
        
        _state.value = currentState.copy(
            isAnswered = true,
            opponentScore = if (opponentCorrect) currentState.opponentScore + 1 else currentState.opponentScore
        )
    }
    
    fun updateTimeProgress(progress: Float) {
        _state.value = _state.value.copy(timeProgress = progress)
    }
    
    private fun saveGameResult() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getLoggedInUser()
                if (currentUser != null) {
                    val currentState = _state.value
                    val isVictory = currentState.playerScore > currentState.opponentScore
                    
                    // Save game history
                    val gameHistory = GameHistory(
                        userId = currentUser.id,
                        opponentName = "AI Bot",
                        userScore = currentState.playerScore,
                        opponentScore = currentState.opponentScore,
                        isVictory = isVictory,
                        totalQuestions = currentState.questions.size,
                        gameMode = "offline"
                    )
                    gameHistoryRepository.insertGame(gameHistory)
                    
                    // Update user stats
                    val points = currentState.playerScore * 10
                    val wins = if (isVictory) 1 else 0
                    val losses = if (!isVictory) 1 else 0
                    userRepository.updateUserStats(currentUser.id, points, wins, losses)
                }
            } catch (e: Exception) {
                // Error saving game result
            }
        }
    }
    
    fun resetGame() {
        _state.value = BattleState()
        loadQuestions()
    }
}
