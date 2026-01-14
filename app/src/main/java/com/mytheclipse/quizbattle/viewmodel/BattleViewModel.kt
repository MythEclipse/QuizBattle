package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.data.local.entity.UserQuestionHistory
import com.mytheclipse.quizbattle.data.repository.GameHistoryRepository
import com.mytheclipse.quizbattle.data.repository.QuestionRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Represents a single question in battle mode
 */
data class BattleQuestion(
    val id: Long,
    val text: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

/**
 * UI State for Battle screen - immutable data class
 */
data class BattleState(
    val questions: List<BattleQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val playerHealth: Int = DEFAULT_HEALTH,
    val opponentHealth: Int = DEFAULT_HEALTH,
    val isAnswered: Boolean = false,
    val selectedAnswerIndex: Int = NO_ANSWER_SELECTED,
    val timeProgress: Float = FULL_TIME,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val damageAmount: Int = DEFAULT_DAMAGE,
    val playerTookDamage: Boolean = false,
    val playerAttacking: Boolean = false,
    val opponentTookDamage: Boolean = false,
    val lastOpponentAttackTime: Long = 0,
    val earnedPoints: Int = 0,
    val earnedCoins: Int = 0,
    val earnedExp: Int = 0
) {
    /** Current question or null if no questions */
    val currentQuestion: BattleQuestion?
        get() = questions.getOrNull(currentQuestionIndex)
    
    /** Check if player won */
    val isVictory: Boolean
        get() = opponentHealth <= 0 && playerHealth > 0
    
    /** Check if there are more questions */
    val hasMoreQuestions: Boolean
        get() = currentQuestionIndex < questions.size - 1
    
    companion object {
        const val DEFAULT_HEALTH = 100
        const val DEFAULT_DAMAGE = 10
        const val NO_ANSWER_SELECTED = -1
        const val FULL_TIME = 1f
    }
}

/**
 * One-time events for Battle screen
 */
sealed class BattleEvent {
    data class ShowDamageAnimation(val isPlayer: Boolean) : BattleEvent()
    data class ShowAttackAnimation(val isPlayerAttacking: Boolean) : BattleEvent()
    data object GameEnded : BattleEvent()
    data class ShowError(val message: String) : BattleEvent()
}

/**
 * ViewModel for offline Battle mode with RTS-style gameplay
 */
class BattleViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val database = QuizBattleDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    private val userRepository = UserRepository(database.userDao())
    private val gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    // endregion
    
    // region State
    private val _state = MutableStateFlow(BattleState())
    val state: StateFlow<BattleState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<BattleEvent>()
    val events: SharedFlow<BattleEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        initializeGame()
    }
    
    // region Public Actions
    
    fun answerQuestion(answerIndex: Int) {
        val currentState = _state.value
        if (currentState.isAnswered || currentState.currentQuestion == null) return
        
        val isCorrect = answerIndex == currentState.currentQuestion!!.correctAnswerIndex
        
        if (isCorrect) {
            handleCorrectAnswer(currentState, answerIndex)
        } else {
            handleWrongAnswer(currentState, answerIndex)
        }
    }
    
    fun nextQuestion() {
        val currentState = _state.value
        if (!currentState.isAnswered || currentState.isGameOver) return
        
        // Game only ends when health reaches 0, not when questions run out
        if (!currentState.hasMoreQuestions) {
            // Reload more questions instead of ending the game
            loadMoreQuestions()
            return
        }
        
        updateState {
            copy(
                currentQuestionIndex = currentQuestionIndex + 1,
                isAnswered = false,
                selectedAnswerIndex = BattleState.NO_ANSWER_SELECTED,
                timeProgress = BattleState.FULL_TIME,
                playerTookDamage = false,
                playerAttacking = false,
                opponentTookDamage = false
            )
        }
    }
    
    fun timeUp() {
        val currentState = _state.value
        if (currentState.isAnswered) return
        
        val newPlayerHealth = calculateNewHealth(currentState.playerHealth, currentState.damageAmount)
        val isGameOver = newPlayerHealth <= 0
        
        updateState {
            copy(
                isAnswered = true,
                playerHealth = newPlayerHealth,
                isGameOver = isGameOver,
                playerTookDamage = true,
                playerAttacking = false,
                opponentTookDamage = false
            )
        }
        
        if (isGameOver) saveGameResult()
    }
    
    fun updateTimeProgress(progress: Float) {
        updateState { copy(timeProgress = progress) }
    }
    
    fun resetGame() {
        _state.value = BattleState()
        loadQuestions()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun initializeGame() {
        launchSafely {
            questionRepository.ensureMinimumQuestions()
            loadQuestions()
        }
        startOpponentAttackLoop()
    }
    
    /**
     * Load more questions when current batch is exhausted.
     * Preserves current health and game state, only refreshes questions.
     */
    private fun loadMoreQuestions() {
        launchSafely {
            val currentUser = userRepository.getLoggedInUser() 
                ?: userRepository.getOrCreateGuestUser()
            
            val questions = fetchQuestions(currentUser.id)
            
            if (questions.isNotEmpty()) {
                markQuestionsAsSeen(currentUser.id, questions)
            }
            
            val battleQuestions = questions.map { it.toBattleQuestion() }.shuffled()
            
            updateState {
                copy(
                    questions = battleQuestions,
                    currentQuestionIndex = 0,
                    isAnswered = false,
                    selectedAnswerIndex = BattleState.NO_ANSWER_SELECTED,
                    timeProgress = BattleState.FULL_TIME,
                    playerTookDamage = false,
                    playerAttacking = false,
                    opponentTookDamage = false
                )
            }
        }
    }
    
    private fun loadQuestions() {
        launchSafely {
            updateState { copy(isLoading = true) }
            
            val currentUser = userRepository.getLoggedInUser() 
                ?: userRepository.getOrCreateGuestUser()
            
            val questions = fetchQuestions(currentUser.id)
            
            if (questions.isNotEmpty()) {
                markQuestionsAsSeen(currentUser.id, questions)
            }
            
            val battleQuestions = questions.map { it.toBattleQuestion() }.shuffled()
            
            updateState {
                copy(
                    questions = battleQuestions,
                    currentQuestionIndex = 0,
                    isLoading = false
                )
            }
        }
    }
    
    private suspend fun fetchQuestions(userId: Long): List<Question> {
        var questions = questionRepository.getUnseenRandomQuestions(userId, QUESTIONS_PER_GAME)
        
        if (questions.size < QUESTIONS_PER_GAME) {
            questionRepository.clearUserQuestionHistory(userId)
            questions = questionRepository.getUnseenRandomQuestions(userId, QUESTIONS_PER_GAME)
            
            if (questions.isEmpty()) {
                questions = questionRepository.getRandomQuestions(QUESTIONS_PER_GAME)
            }
        }
        
        return questions
    }
    
    private suspend fun markQuestionsAsSeen(userId: Long, questions: List<Question>) {
        val historyList = questions.map { question ->
            UserQuestionHistory(
                userId = userId,
                questionId = question.id,
                seenAt = System.currentTimeMillis()
            )
        }
        questionRepository.insertUserQuestionHistory(historyList)
    }
    
    private fun startOpponentAttackLoop() {
        launchSafely {
            while (true) {
                delay(CHECK_INTERVAL)
                
                val currentState = _state.value
                if (currentState.isGameOver || currentState.playerHealth <= 0) break
                
                val shouldAttack = shouldOpponentAttack(currentState)
                if (shouldAttack) {
                    executeOpponentAttack()
                }
            }
        }
    }
    
    private fun shouldOpponentAttack(state: BattleState): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAttack = currentTime - state.lastOpponentAttackTime
        val nextAttackInterval = Random.nextLong(OPPONENT_ATTACK_MIN, OPPONENT_ATTACK_MAX)
        return timeSinceLastAttack >= nextAttackInterval || state.lastOpponentAttackTime == 0L
    }
    
    private fun executeOpponentAttack() {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.playerHealth <= 0) return
        
        val opponentHits = Random.nextBoolean()
        
        if (opponentHits) {
            val newPlayerHealth = calculateNewHealth(currentState.playerHealth, currentState.damageAmount)
            val isGameOver = newPlayerHealth <= 0
            
            updateState {
                copy(
                    playerHealth = newPlayerHealth,
                    playerTookDamage = true,
                    isGameOver = isGameOver,
                    lastOpponentAttackTime = System.currentTimeMillis()
                )
            }
            
            if (isGameOver) saveGameResult()
            
            resetDamageIndicatorWithDelay()
        } else {
            updateState { copy(lastOpponentAttackTime = System.currentTimeMillis()) }
        }
    }
    
    private fun handleCorrectAnswer(currentState: BattleState, answerIndex: Int) {
        val newOpponentHealth = calculateNewHealth(currentState.opponentHealth, currentState.damageAmount)
        val isGameOver = newOpponentHealth <= 0
        
        updateState {
            copy(
                isAnswered = true,
                selectedAnswerIndex = answerIndex,
                opponentHealth = newOpponentHealth,
                isGameOver = isGameOver,
                playerTookDamage = false,
                playerAttacking = true,
                opponentTookDamage = true
            )
        }
        
        if (isGameOver) saveGameResult()
        resetAttackAnimationsWithDelay()
    }
    
    private fun handleWrongAnswer(currentState: BattleState, answerIndex: Int) {
        val newPlayerHealth = calculateNewHealth(currentState.playerHealth, currentState.damageAmount)
        val isGameOver = newPlayerHealth <= 0
        
        updateState {
            copy(
                isAnswered = true,
                selectedAnswerIndex = answerIndex,
                playerHealth = newPlayerHealth,
                isGameOver = isGameOver,
                playerTookDamage = true,
                playerAttacking = false,
                opponentTookDamage = false
            )
        }
        
        if (isGameOver) saveGameResult()
        resetDamageIndicatorWithDelay()
    }
    
    private fun endGame() {
        updateState { copy(isGameOver = true) }
        saveGameResult()
    }
    
    private fun calculateNewHealth(currentHealth: Int, damage: Int): Int =
        (currentHealth - damage).coerceAtLeast(0)
    
    private fun resetDamageIndicatorWithDelay() {
        launchSafely {
            delay(ANIMATION_DELAY)
            updateState { copy(playerTookDamage = false) }
        }
    }
    
    private fun resetAttackAnimationsWithDelay() {
        launchSafely {
            delay(ATTACK_ANIMATION_DELAY)
            updateState { copy(playerAttacking = false, opponentTookDamage = false) }
        }
    }
    
    private fun saveGameResult() {
        launchSafely {
            val currentUser = userRepository.getLoggedInUser() 
                ?: userRepository.getOrCreateGuestUser()
            
            val currentState = _state.value
            val isVictory = determineVictory(currentState)
            
            val gameHistory = createGameHistory(currentUser.id, currentState, isVictory)
            gameHistoryRepository.insertGame(gameHistory)
            
            val rewards = calculateRewards(isVictory)
            updateUserStats(currentUser.id, rewards, isVictory)
            
            updateState {
                copy(
                    earnedPoints = rewards.points,
                    earnedCoins = rewards.coins,
                    earnedExp = rewards.exp
                )
            }
            
            emitEvent(BattleEvent.GameEnded)
        }
    }
    
    private fun determineVictory(state: BattleState): Boolean = when {
        state.opponentHealth <= 0 && state.playerHealth > 0 -> true
        state.playerHealth <= 0 && state.opponentHealth > 0 -> false
        else -> state.playerHealth > state.opponentHealth
    }
    
    private fun createGameHistory(
        userId: Long,
        state: BattleState,
        isVictory: Boolean
    ) = GameHistory(
        userId = userId,
        opponentName = OPPONENT_NAME,
        userScore = state.playerHealth,
        opponentScore = state.opponentHealth,
        isVictory = isVictory,
        totalQuestions = state.questions.size,
        gameMode = GAME_MODE_OFFLINE
    )
    
    private fun calculateRewards(isVictory: Boolean) = BattleRewards(
        points = if (isVictory) VICTORY_POINTS else DEFEAT_POINTS,
        coins = if (isVictory) VICTORY_COINS else DEFEAT_COINS,
        exp = if (isVictory) VICTORY_EXP else DEFEAT_EXP
    )
    
    private suspend fun updateUserStats(userId: Long, rewards: BattleRewards, isVictory: Boolean) {
        val wins = if (isVictory) 1 else 0
        val losses = if (!isVictory) 1 else 0
        userRepository.updateUserStats(userId, rewards.points, wins, losses)
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: BattleState.() -> BattleState) {
        _state.update { it.update() }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: BattleEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    private fun Question.toBattleQuestion() = BattleQuestion(
        id = id,
        text = questionText,
        answers = listOf(answer1, answer2, answer3, answer4),
        correctAnswerIndex = correctAnswerIndex
    )
    
    // endregion
    
    companion object {
        private const val TAG = "BattleViewModel"
        
        // Game Settings
        private const val QUESTIONS_PER_GAME = 5
        private const val OPPONENT_NAME = "AI Bot"
        private const val GAME_MODE_OFFLINE = "offline"
        
        // Timing Constants (ms)
        private const val OPPONENT_ATTACK_MIN = 3000L
        private const val OPPONENT_ATTACK_MAX = 7000L
        private const val CHECK_INTERVAL = 1000L
        private const val ANIMATION_DELAY = 500L
        private const val ATTACK_ANIMATION_DELAY = 800L
        
        // Rewards
        private const val VICTORY_POINTS = 100
        private const val DEFEAT_POINTS = 20
        private const val VICTORY_COINS = 50
        private const val DEFEAT_COINS = 10
        private const val VICTORY_EXP = 100
        private const val DEFEAT_EXP = 25
    }
    
    /** Data class for battle rewards */
    private data class BattleRewards(
        val points: Int,
        val coins: Int,
        val exp: Int
    )
}