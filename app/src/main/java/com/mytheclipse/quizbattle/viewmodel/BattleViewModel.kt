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
    val playerHealth: Int = 100,
    val opponentHealth: Int = 100,
    val isAnswered: Boolean = false,
    val selectedAnswerIndex: Int = -1,
    val timeProgress: Float = 1f,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val damageAmount: Int = 20, // Damage per wrong answer
    val playerTookDamage: Boolean = false,
    val playerAttacking: Boolean = false,
    val opponentTookDamage: Boolean = false,
    val lastOpponentAttackTime: Long = 0 // Track when opponent last attacked
)

class BattleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = QuizBattleDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    private val userRepository = UserRepository(database.userDao())
    private val gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    
    private val _state = MutableStateFlow(BattleState())
    val state: StateFlow<BattleState> = _state.asStateFlow()
    
    // Opponent attack interval (random between 3-7 seconds)
    private val opponentAttackIntervalMin = 3000L
    private val opponentAttackIntervalMax = 7000L
    
    init {
        loadQuestions()
        startOpponentAttacks()
    }
    
    private fun startOpponentAttacks() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000) // Check every second
                
                val currentState = _state.value
                if (currentState.isGameOver || currentState.playerHealth <= 0) break
                
                val currentTime = System.currentTimeMillis()
                val timeSinceLastAttack = currentTime - currentState.lastOpponentAttackTime
                val nextAttackInterval = Random.nextLong(opponentAttackIntervalMin, opponentAttackIntervalMax)
                
                if (timeSinceLastAttack >= nextAttackInterval || currentState.lastOpponentAttackTime == 0L) {
                    opponentAttack()
                }
            }
        }
    }
    
    private fun opponentAttack() {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.playerHealth <= 0) return
        
        // Random chance: 50% opponent attacks successfully
        val opponentSuccess = Random.nextBoolean()
        
        if (opponentSuccess) {
            // Opponent attacks player
            val newPlayerHealth = (currentState.playerHealth - currentState.damageAmount).coerceAtLeast(0)
            val isGameOver = newPlayerHealth <= 0
            
            _state.value = currentState.copy(
                playerHealth = newPlayerHealth,
                playerTookDamage = true,
                isGameOver = isGameOver,
                lastOpponentAttackTime = System.currentTimeMillis()
            )
            
            if (isGameOver) {
                saveGameResult()
            }
            
            // Reset damage indicator after short delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                _state.value = _state.value.copy(playerTookDamage = false)
            }
        } else {
            // Opponent missed - just update last attack time
            _state.value = currentState.copy(
                lastOpponentAttackTime = System.currentTimeMillis()
            )
        }
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
                    currentQuestionIndex = Random.nextInt(battleQuestions.size),
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
        
        // Realtime RTS style: only player takes action immediately
        if (isCorrect) {
            // Player attacks opponent
            val newOpponentHealth = (currentState.opponentHealth - currentState.damageAmount).coerceAtLeast(0)
            val isGameOver = newOpponentHealth <= 0
            
            _state.value = currentState.copy(
                isAnswered = true,
                selectedAnswerIndex = answerIndex,
                opponentHealth = newOpponentHealth,
                isGameOver = isGameOver,
                playerTookDamage = false,
                playerAttacking = true,
                opponentTookDamage = true
            )
            
            if (isGameOver) {
                saveGameResult()
            }
            
            // Reset attack flags after animation
            viewModelScope.launch {
                kotlinx.coroutines.delay(800)
                _state.value = _state.value.copy(
                    playerAttacking = false,
                    opponentTookDamage = false
                )
            }
        } else {
            // Player takes damage for wrong answer
            val newPlayerHealth = (currentState.playerHealth - currentState.damageAmount).coerceAtLeast(0)
            val isGameOver = newPlayerHealth <= 0
            
            _state.value = currentState.copy(
                isAnswered = true,
                selectedAnswerIndex = answerIndex,
                playerHealth = newPlayerHealth,
                isGameOver = isGameOver,
                playerTookDamage = true,
                playerAttacking = false,
                opponentTookDamage = false
            )
            
            if (isGameOver) {
                saveGameResult()
            }
            
            // Reset damage flag after animation
            viewModelScope.launch {
                kotlinx.coroutines.delay(800)
                _state.value = _state.value.copy(playerTookDamage = false)
            }
        }
    }
    
    fun nextQuestion() {
        val currentState = _state.value
        if (!currentState.isAnswered) return
        
        // Check if game over due to health
        if (currentState.isGameOver) return
        
        // Pick random question index
        val randomIndex = Random.nextInt(currentState.questions.size)
        
        _state.value = currentState.copy(
            currentQuestionIndex = randomIndex,
            isAnswered = false,
            selectedAnswerIndex = -1,
            timeProgress = 1f,
            playerTookDamage = false,
            playerAttacking = false,
            opponentTookDamage = false
        )
    }
    
    fun timeUp() {
        val currentState = _state.value
        if (currentState.isAnswered) return
        
        // Player takes damage for timeout (no opponent involvement)
        val newPlayerHealth = (currentState.playerHealth - currentState.damageAmount).coerceAtLeast(0)
        val isGameOver = newPlayerHealth <= 0
        
        _state.value = currentState.copy(
            isAnswered = true,
            playerHealth = newPlayerHealth,
            isGameOver = isGameOver,
            playerTookDamage = true,
            playerAttacking = false,
            opponentTookDamage = false
        )
        
        if (isGameOver) {
            saveGameResult()
        }
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
                    // Victory if opponent health reaches 0 or player has more health
                    val isVictory = if (currentState.opponentHealth <= 0 && currentState.playerHealth > 0) {
                        true
                    } else if (currentState.playerHealth <= 0 && currentState.opponentHealth > 0) {
                        false
                    } else {
                        currentState.playerHealth > currentState.opponentHealth
                    }
                    
                    // Save game history (use health as score)
                    val gameHistory = GameHistory(
                        userId = currentUser.id,
                        opponentName = "AI Bot",
                        userScore = currentState.playerHealth,
                        opponentScore = currentState.opponentHealth,
                        isVictory = isVictory,
                        totalQuestions = currentState.questions.size,
                        gameMode = "offline"
                    )
                    gameHistoryRepository.insertGame(gameHistory)
                    
                    // Update user stats based on victory
                    val points = if (isVictory) 100 else 0
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
