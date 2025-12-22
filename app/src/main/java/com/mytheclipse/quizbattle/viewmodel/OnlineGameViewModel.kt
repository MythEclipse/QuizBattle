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

data class OnlineGameState(
    val matchId: String = "",
    val allQuestions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
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
    val error: String? = null
)

class OnlineGameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val gameRepository = OnlineGameRepository()
    
    private val _state = MutableStateFlow(OnlineGameState())
    val state: StateFlow<OnlineGameState> = _state.asStateFlow()
    
    init {
        observeGameEvents()
    }
    
    fun connectToMatch(matchId: String) {
        _state.value = _state.value.copy(matchId = matchId)
        viewModelScope.launch {
            if (BuildConfig.DEBUG) Log.d("API", "OnlineGameViewModel.connectToMatch - matchId=$matchId")
            gameRepository.connectToMatch(matchId)
        }
    }
    
    private fun observeGameEvents() {
        viewModelScope.launch {
            gameRepository.observeGameEvents().collect { event ->
                when (event) {
                    is GameEvent.GameStarted -> {
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
                        val currentState = _state.value
                        _state.value = currentState.copy(
                            lastAnswerCorrect = event.isCorrect,
                            isAnswered = true,
                            correctAnswerIndex = event.correctAnswer.toIntOrNull() ?: -1,
                            playerScore = currentState.playerScore + event.points + event.timeBonus
                        )
                        
                        // Auto-advance to next question after short delay (spam mode)
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(500) // Brief delay to show feedback
                            val nextIndex = currentState.currentQuestionIndex + 1
                            if (nextIndex < currentState.allQuestions.size) {
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
                        _state.value = _state.value.copy(
                            gameFinished = true,
                            isVictory = event.winner == tokenRepository.getUserId(),
                            playerScore = event.playerScore,
                            opponentScore = event.opponentScore
                        )
                    }
                    is GameEvent.OpponentAnswered -> {
                        _state.value = _state.value.copy(
                            opponentAnswered = true
                            // Could use event.isCorrect and event.animation for opponent animations
                        )
                    }
                    is GameEvent.BattleUpdate -> {
                        // Real-time update of scores and health during gameplay
                        // Swap scores if user is player2 (goblin)
                        val (myScore, theirScore) = if (_state.value.isPlayer1) {
                            event.playerScore to event.opponentScore
                        } else {
                            event.opponentScore to event.playerScore
                        }
                        _state.value = _state.value.copy(
                            playerScore = myScore,
                            opponentScore = theirScore
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
    
    fun nextQuestion() {
        _state.value = _state.value.copy(
            currentQuestionIndex = _state.value.currentQuestionIndex + 1,
            lastAnswerCorrect = null,
            opponentAnswered = false,
            timeRemaining = 30,
            timeLeft = 30,
            isAnswered = false
        )
    }
}
