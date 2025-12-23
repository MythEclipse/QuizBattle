package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels.Question
import com.mytheclipse.quizbattle.data.repository.*
import android.util.Log
import com.mytheclipse.quizbattle.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

data class OnlineGameState(
    val matchId: String = "",
    val allQuestions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val timeRemaining: Int = 30,
    val timeLeft: Int = 30,
    val isAnswered: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val selectedAnswerIndex: Int = -1,
    val correctAnswerIndex: Int = -1,
    val opponentAnswered: Boolean = false,
    val gameFinished: Boolean = false,
    val isVictory: Boolean = false,
    val isPlayer1: Boolean = true, // true = left/knight, false = right/goblin
    val error: String? = null,
    val playerHealth: Int = 100,
    val opponentHealth: Int = 100
)

class OnlineGameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val gameRepository = OnlineGameRepository()
    
    private val _state = MutableStateFlow(OnlineGameState())
    val state: StateFlow<OnlineGameState> = _state.asStateFlow()
    
    private var observingJob: Job? = null  // Track collector job for proper cancellation
    
    init {
        // observeGameEvents() moved to connectToMatch
    }
    
    fun connectToMatch(matchId: String) {
        // Cancel old event collector to prevent replay
        observingJob?.cancel()
        
        // FULL state reset for new match to prevent replay
        _state.value = OnlineGameState(
            matchId = matchId
            // All other fields use default values from data class
        )
        
        viewModelScope.launch {
            try {
                if (BuildConfig.DEBUG) Log.d("API", "OnlineGameViewModel.connectToMatch - matchId=$matchId")
                
                // Always create fresh collector for new match
                observingJob = observeGameEvents(matchId)
                
                gameRepository.connectToMatch(matchId)
            } catch (e: Exception) {
                Log.e("OnlineGameVM", "Failed to connect to match", e)
                _state.value = _state.value.copy(
                    error = "Failed to connect to match: ${e.message}",
                    gameFinished = true
                )
            }
        }
    }
    
    private fun observeGameEvents(targetMatchId: String): Job {
        return viewModelScope.launch {
            gameRepository.observeGameEvents().collect { event ->
                if (BuildConfig.DEBUG) {
                    val eventType = event.javaClass.simpleName
                    val eventMatchId = when (event) {
                        is GameEvent.GameStarted -> event.matchId
                        is GameEvent.AllQuestions -> event.matchId
                        is GameEvent.QuestionNew -> event.matchId
                        is GameEvent.GameFinished -> event.matchId
                        is GameEvent.BattleUpdate -> event.matchId
                        else -> "N/A"
                    }
                    Log.d("OnlineGameVM", "Received Event: $eventType for match: $eventMatchId (Target: $targetMatchId)")
                }

                // Filter events that don't belong to this match
                // Note: GameStarting doesn't have matchId, allow it if we are connecting
                
                when (event) {
                    is GameEvent.GameStarted -> {
                         if (event.matchId != targetMatchId) return@collect
                         
                        // Determine if user is player1 (left/knight) based on position
                        val userId = tokenRepository.getUserId()
                        val currentUserPlayer = event.players.find { it.userId == userId }
                        val isUserPlayer1 = currentUserPlayer?.position == "left"
                        
                        _state.value = _state.value.copy(
                            matchId = event.matchId,
                            totalQuestions = event.totalQuestions,
                            timeRemaining = event.timePerQuestion,
                            timeLeft = event.timePerQuestion,
                            isPlayer1 = isUserPlayer1
                        )
                    }
                    is GameEvent.AllQuestions -> {
                        if (event.matchId != targetMatchId) return@collect
                        
                        // Store all questions and display first one
                        _state.value = _state.value.copy(
                            allQuestions = event.questions,
                            currentQuestion = event.questions.firstOrNull(),
                            currentQuestionIndex = 0,
                            totalQuestions = event.questions.size,
                            isAnswered = false
                        )
                    }
                    is GameEvent.QuestionNew -> {
                        if (event.matchId != targetMatchId) return@collect
                        
                        _state.value = _state.value.copy(
                            currentQuestion = event.question,
                            currentQuestionIndex = event.questionIndex,
                            timeRemaining = event.timeLimit,
                            timeLeft = event.timeLimit,
                            isAnswered = false,
                            lastAnswerCorrect = null,
                            selectedAnswerIndex = -1,
                            correctAnswerIndex = -1,
                            opponentAnswered = false
                        )
                    }
                    is GameEvent.AnswerResult -> {
                        // Answers don't have matchId in the event wrapper usually, but if they did we'd check it
                        // Assuming flow context is sufficient or we handle based on state
                        
                        val currentState = _state.value
                        _state.value = currentState.copy(
                            lastAnswerCorrect = event.isCorrect,
                            isAnswered = true,
                            correctAnswerIndex = event.correctAnswer.toIntOrNull() ?: -1,
                            playerHealth = event.playerHealth,  // Update both healths immediately
                            opponentHealth = event.opponentHealth
                        )
                        
                        // Auto-advance to next question after short delay (spam mode)
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(500) // Brief delay to show feedback
                            val nextIndex = currentState.currentQuestionIndex + 1
                            // Check if game is not finished and still have questions
                            if (nextIndex < currentState.allQuestions.size && !_state.value.gameFinished) {
                                _state.value = _state.value.copy(
                                    currentQuestion = currentState.allQuestions[nextIndex],
                                    currentQuestionIndex = nextIndex,
                                    isAnswered = false,
                                    lastAnswerCorrect = null,
                                    selectedAnswerIndex = -1,
                                    correctAnswerIndex = -1
                                )
                            }
                        }
                    }
                    is GameEvent.GameStarting -> {
                        // Game starting countdown
                    }
                    is GameEvent.GameFinished -> {
                        if (event.matchId != targetMatchId) return@collect
                        
                        _state.value = _state.value.copy(
                            gameFinished = true,
                            isVictory = event.winner == tokenRepository.getUserId()
                        )
                    }
                    is GameEvent.OpponentAnswered -> {
                        _state.value = _state.value.copy(
                            opponentAnswered = true
                            // Could use event.isCorrect and event.animation for opponent animations
                        )
                    }
                    is GameEvent.BattleUpdate -> {
                        if (event.matchId != targetMatchId) return@collect

                        // Real-time update of health during gameplay
                        // Swap health if user is player2 (goblin)
                        val (myHealth, theirHealth) = if (_state.value.isPlayer1) {
                            event.playerHealth to event.opponentHealth
                        } else {
                            event.opponentHealth to event.playerHealth
                        }

                        _state.value = _state.value.copy(
                            playerHealth = myHealth,
                            opponentHealth = theirHealth
                        )
                    }
                    is GameEvent.OpponentDisconnected -> {
                        _state.value = _state.value.copy(
                            gameFinished = true,
                            isVictory = true,
                            error = "Opponent disconnected"
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun setMatchId(matchId: String) {
        _state.value = _state.value.copy(matchId = matchId)
    }
    
    fun submitAnswer(answerIndex: Int) {
        val question = _state.value.currentQuestion ?: return
        val timeSpentSeconds = 30 - _state.value.timeRemaining
        submitAnswer(question.questionId, _state.value.currentQuestionIndex, answerIndex, timeSpentSeconds * 1000)
    }
    
    fun submitAnswer(questionId: String, questionIndex: Int, answerIndex: Int, answerTimeMs: Int) {
        // Store the selected answer index for UI feedback
        _state.value = _state.value.copy(selectedAnswerIndex = answerIndex)
        
        viewModelScope.launch {
            if (BuildConfig.DEBUG) Log.d("API", "OnlineGameViewModel.submitAnswer - matchId=${_state.value.matchId} questionId=$questionId questionIndex=$questionIndex answerIndex=$answerIndex answerTimeMs=$answerTimeMs")
            val userId = tokenRepository.getUserId() ?: return@launch
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
    
}
