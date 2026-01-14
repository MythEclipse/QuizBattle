package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.BuildConfig
import com.mytheclipse.quizbattle.data.repository.DataModels.Question
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Online Game screen
 */
data class OnlineGameState(
    val matchId: String = "",
    val allQuestions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = DEFAULT_TOTAL_QUESTIONS,
    val timeRemaining: Int = DEFAULT_TIME_PER_QUESTION,
    val timeLeft: Int = DEFAULT_TIME_PER_QUESTION,
    val isAnswered: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val selectedAnswerIndex: Int = NO_ANSWER_SELECTED,
    val correctAnswerIndex: Int = NO_ANSWER_SELECTED,
    val opponentAnswered: Boolean = false,
    val gameFinished: Boolean = false,
    val isVictory: Boolean = false,
    val isPlayer1: Boolean = true,
    val error: String? = null,
    val playerHealth: Int = DEFAULT_HEALTH,
    val opponentHealth: Int = DEFAULT_HEALTH,
    val earnedPoints: Int = 0,
    val earnedCoins: Int = 0,
    val earnedExp: Int = 0
) {
    /** Check if more questions available */
    val hasMoreQuestions: Boolean
        get() = currentQuestionIndex < allQuestions.size - 1
    
    /** Get progress percentage */
    val progressPercentage: Float
        get() = if (totalQuestions > 0) 
            (currentQuestionIndex.toFloat() / totalQuestions) * 100 
        else 0f
    
    /** Calculate time spent in seconds */
    val timeSpentSeconds: Int
        get() = DEFAULT_TIME_PER_QUESTION - timeRemaining
    
    companion object {
        const val DEFAULT_TOTAL_QUESTIONS = 10
        const val DEFAULT_TIME_PER_QUESTION = 30
        const val DEFAULT_HEALTH = 100
        const val NO_ANSWER_SELECTED = -1
    }
}

/**
 * One-time events for Online Game screen
 */
sealed class OnlineGameEvent {
    data class GameStarted(val matchId: String) : OnlineGameEvent()
    data class AnswerResult(val isCorrect: Boolean) : OnlineGameEvent()
    data class GameFinished(val isVictory: Boolean) : OnlineGameEvent()
    data class OpponentDisconnected(val message: String) : OnlineGameEvent()
    data class ShowError(val message: String) : OnlineGameEvent()
}

/**
 * ViewModel for Online Game functionality
 */
class OnlineGameViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val gameRepository = OnlineGameRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(OnlineGameState())
    val state: StateFlow<OnlineGameState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<OnlineGameEvent>()
    val events: SharedFlow<OnlineGameEvent> = _events.asSharedFlow()
    
    /** Track collector job for proper cancellation */
    private var observingJob: Job? = null
    
    /** Track current match to filter old events */
    private var currentMatchId: String = ""
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(error = throwable.message, gameFinished = true) }
    }
    // endregion
    
    // region Public Actions
    
    fun connectToMatch(matchId: String) {
        cancelObservingJob()
        currentMatchId = matchId
        resetStateForNewMatch(matchId)
        
        launchSafely {
            logDebug("Connecting to match: $matchId")
            observingJob = observeGameEvents(matchId)
            gameRepository.connectToMatch(matchId)
        }
    }
    
    fun setMatchId(matchId: String) {
        updateState { copy(matchId = matchId) }
    }
    
    fun submitAnswer(answerIndex: Int) {
        val question = _state.value.currentQuestion ?: return
        val timeSpentMs = _state.value.timeSpentSeconds * 1000
        
        submitAnswerInternal(
            questionId = question.questionId,
            questionIndex = _state.value.currentQuestionIndex,
            answerIndex = answerIndex,
            answerTimeMs = timeSpentMs
        )
    }
    
    fun submitAnswer(questionId: String, questionIndex: Int, answerIndex: Int, answerTimeMs: Int) {
        submitAnswerInternal(questionId, questionIndex, answerIndex, answerTimeMs)
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun cancelObservingJob() {
        observingJob?.cancel()
        observingJob = null
    }
    
    private fun resetStateForNewMatch(matchId: String) {
        _state.value = OnlineGameState(matchId = matchId)
    }
    
    private fun observeGameEvents(targetMatchId: String): Job {
        return viewModelScope.launch(exceptionHandler) {
            gameRepository.observeGameEvents().collect { event ->
                if (shouldIgnoreEvent(event, targetMatchId)) return@collect
                
                logDebug("Processing event: ${event.javaClass.simpleName}")
                handleGameEvent(event, targetMatchId)
            }
        }
    }
    
    private fun shouldIgnoreEvent(event: GameEvent, targetMatchId: String): Boolean {
        val eventMatchId = extractMatchId(event)
        
        if (eventMatchId != NO_MATCH_ID && eventMatchId != currentMatchId) {
            logDebug("Ignored old event: ${event.javaClass.simpleName} from match: $eventMatchId")
            return true
        }
        return false
    }
    
    private fun extractMatchId(event: GameEvent): String = when (event) {
        is GameEvent.GameStarted -> event.matchId
        is GameEvent.AllQuestions -> event.matchId
        is GameEvent.QuestionNew -> event.matchId
        is GameEvent.GameFinished -> event.matchId
        is GameEvent.BattleUpdate -> event.matchId
        else -> NO_MATCH_ID
    }
    
    private suspend fun handleGameEvent(event: GameEvent, targetMatchId: String) {
        when (event) {
            is GameEvent.GameStarted -> handleGameStarted(event, targetMatchId)
            is GameEvent.AllQuestions -> handleAllQuestions(event, targetMatchId)
            is GameEvent.QuestionNew -> handleNewQuestion(event, targetMatchId)
            is GameEvent.AnswerResult -> handleAnswerResult(event)
            is GameEvent.GameStarting -> { /* Game starting countdown */ }
            is GameEvent.GameFinished -> handleGameFinished(event, targetMatchId)
            is GameEvent.OpponentAnswered -> handleOpponentAnswered(event)
            is GameEvent.BattleUpdate -> handleBattleUpdate(event, targetMatchId)
            is GameEvent.OpponentDisconnected -> handleOpponentDisconnected()
            else -> logDebug("Unhandled game event: ${event.javaClass.simpleName}")
        }
    }
    
    private suspend fun handleGameStarted(event: GameEvent.GameStarted, targetMatchId: String) {
        if (event.matchId != targetMatchId) return
        
        val userId = tokenRepository.getUserId()
        val currentUserPlayer = event.players.find { it.userId == userId }
        val isUserPlayer1 = currentUserPlayer?.position == POSITION_LEFT
        
        updateState {
            copy(
                matchId = event.matchId,
                totalQuestions = event.totalQuestions,
                timeRemaining = event.timePerQuestion,
                timeLeft = event.timePerQuestion,
                isPlayer1 = isUserPlayer1
            )
        }
        
        emitEvent(OnlineGameEvent.GameStarted(event.matchId))
    }
    
    private fun handleAllQuestions(event: GameEvent.AllQuestions, targetMatchId: String) {
        if (event.matchId != targetMatchId) return
        
        updateState {
            copy(
                allQuestions = event.questions,
                currentQuestion = event.questions.firstOrNull(),
                currentQuestionIndex = 0,
                totalQuestions = event.questions.size,
                isAnswered = false
            )
        }
    }
    
    private fun handleNewQuestion(event: GameEvent.QuestionNew, targetMatchId: String) {
        if (event.matchId != targetMatchId) return
        
        updateState {
            copy(
                currentQuestion = event.question,
                currentQuestionIndex = event.questionIndex,
                timeRemaining = event.timeLimit,
                timeLeft = event.timeLimit,
                isAnswered = false,
                lastAnswerCorrect = null,
                selectedAnswerIndex = OnlineGameState.NO_ANSWER_SELECTED,
                correctAnswerIndex = OnlineGameState.NO_ANSWER_SELECTED,
                opponentAnswered = false
            )
        }
    }
    
    private fun handleAnswerResult(event: GameEvent.AnswerResult) {
        val currentState = _state.value
        
        updateState {
            copy(
                lastAnswerCorrect = event.isCorrect,
                isAnswered = true,
                correctAnswerIndex = event.correctAnswer.toIntOrNull() 
                    ?: OnlineGameState.NO_ANSWER_SELECTED,
                playerHealth = event.playerHealth,
                opponentHealth = event.opponentHealth
            )
        }
        
        emitEvent(OnlineGameEvent.AnswerResult(event.isCorrect))
        
        // Auto-advance to next question after delay
        launchSafely {
            delay(ANSWER_FEEDBACK_DELAY)
            advanceToNextQuestionIfPossible(currentState)
        }
    }
    
    private fun advanceToNextQuestionIfPossible(previousState: OnlineGameState) {
        val nextIndex = previousState.currentQuestionIndex + 1
        val hasNextQuestion = nextIndex < previousState.allQuestions.size
        val gameNotFinished = !_state.value.gameFinished
        
        if (hasNextQuestion && gameNotFinished) {
            updateState {
                copy(
                    currentQuestion = previousState.allQuestions[nextIndex],
                    currentQuestionIndex = nextIndex,
                    isAnswered = false,
                    lastAnswerCorrect = null,
                    selectedAnswerIndex = OnlineGameState.NO_ANSWER_SELECTED,
                    correctAnswerIndex = OnlineGameState.NO_ANSWER_SELECTED
                )
            }
        }
    }
    
    private suspend fun handleGameFinished(event: GameEvent.GameFinished, targetMatchId: String) {
        if (event.matchId != targetMatchId) return
        
        val userId = tokenRepository.getUserId()
        val isWinner = event.winner == userId
        val userRewards = if (isWinner) event.winnerRewards else event.loserRewards
        
        updateState {
            copy(
                gameFinished = true,
                isVictory = isWinner,
                earnedPoints = userRewards[REWARD_POINTS] ?: 0,
                earnedCoins = userRewards[REWARD_COINS] ?: 0,
                earnedExp = userRewards[REWARD_EXPERIENCE] ?: 0
            )
        }
        
        emitEvent(OnlineGameEvent.GameFinished(isWinner))
    }
    
    private fun handleOpponentAnswered(event: GameEvent.OpponentAnswered) {
        updateState { copy(opponentAnswered = true) }
    }
    
    private fun handleBattleUpdate(event: GameEvent.BattleUpdate, targetMatchId: String) {
        if (event.matchId != targetMatchId) return
        
        val (myHealth, theirHealth) = if (_state.value.isPlayer1) {
            event.playerHealth to event.opponentHealth
        } else {
            event.opponentHealth to event.playerHealth
        }
        
        updateState {
            copy(playerHealth = myHealth, opponentHealth = theirHealth)
        }
    }
    
    private fun handleOpponentDisconnected() {
        updateState {
            copy(
                gameFinished = true,
                isVictory = true,
                error = ERROR_OPPONENT_DISCONNECTED
            )
        }
        emitEvent(OnlineGameEvent.OpponentDisconnected(ERROR_OPPONENT_DISCONNECTED))
    }
    
    private fun submitAnswerInternal(
        questionId: String,
        questionIndex: Int,
        answerIndex: Int,
        answerTimeMs: Int
    ) {
        updateState { copy(selectedAnswerIndex = answerIndex) }
        
        launchSafely {
            logDebug("Submitting answer: matchId=${_state.value.matchId}, " +
                    "questionId=$questionId, answerIndex=$answerIndex")
            
            val userId = getUserIdOrReturn() ?: return@launchSafely
            gameRepository.submitAnswer(
                userId = userId,
                matchId = _state.value.matchId,
                questionId = questionId,
                questionIndex = questionIndex,
                answerIndex = answerIndex,
                answerTimeMs = answerTimeMs
            )
        }
    }
    
    private suspend fun getUserIdOrReturn(): String? {
        return tokenRepository.getUserId().also {
            if (it == null) logError("User ID not available")
        }
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: OnlineGameState.() -> OnlineGameState) {
        _state.update { it.update() }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: OnlineGameEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "OnlineGameViewModel"
        
        // Constants
        private const val NO_MATCH_ID = "N/A"
        private const val POSITION_LEFT = "left"
        private const val ANSWER_FEEDBACK_DELAY = 500L
        
        // Reward keys
        private const val REWARD_POINTS = "points"
        private const val REWARD_COINS = "coins"
        private const val REWARD_EXPERIENCE = "experience"
        
        // Error messages
        private const val ERROR_OPPONENT_DISCONNECTED = "Opponent disconnected"
    }
}
