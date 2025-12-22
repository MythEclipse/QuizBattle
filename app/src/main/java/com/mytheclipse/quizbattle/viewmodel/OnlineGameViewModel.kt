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
                        _state.value = _state.value.copy(
                            matchId = event.matchId,
                            totalQuestions = event.totalQuestions,
                            timeRemaining = event.timePerQuestion,
                            timeLeft = event.timePerQuestion
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
                        _state.value = _state.value.copy(
                            lastAnswerCorrect = event.isCorrect,
                            isAnswered = true,
                            correctAnswerIndex = event.correctAnswer.toIntOrNull() ?: -1,
                            playerScore = _state.value.playerScore + event.points + event.timeBonus
                        )
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
                        _state.value = _state.value.copy(opponentAnswered = true)
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
