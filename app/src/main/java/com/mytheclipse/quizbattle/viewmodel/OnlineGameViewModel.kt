package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels.Question
import com.mytheclipse.quizbattle.data.repository.*
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
            gameRepository.connectToMatch(matchId)
        }
    }
    
    private fun observeGameEvents() {
        viewModelScope.launch {
            gameRepository.observeGameEvents().collect { event ->
                when (event) {
                    is GameEvent.AnswerResult -> {
                        _state.value = _state.value.copy(
                            lastAnswerCorrect = event.isCorrect,
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
    
    fun submitAnswer(answer: String) {
        val question = _state.value.currentQuestion ?: return
        submitAnswer(question.questionId, answer, _state.value.timeRemaining)
    }
    
    fun submitAnswer(questionId: String, answer: String, timeSpent: Int) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            gameRepository.submitAnswer(
                userId = userId,
                matchId = _state.value.matchId,
                questionId = questionId,
                answer = answer,
                timeSpent = timeSpent
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
