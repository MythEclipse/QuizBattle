package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Lobby screen
 */
data class LobbyState(
    val lobbyId: String? = null,
    val lobbyCode: String? = null,
    val players: List<DataModels.PlayerInfo> = emptyList(),
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val gameStarting: Boolean = false,
    val matchId: String? = null,
    val lobbies: List<DataModels.LobbyInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Check if user is in a lobby */
    val isInLobby: Boolean get() = lobbyId != null
    
    /** Get player count */
    val playerCount: Int get() = players.size
    
    /** Check if all players are ready */
    val allPlayersReady: Boolean get() = players.isNotEmpty() && players.all { it.isReady }
    
    /** Check if host can start the game */
    val canStartGame: Boolean get() = isHost && allPlayersReady && playerCount >= MIN_PLAYERS_TO_START
    
    companion object {
        const val MIN_PLAYERS_TO_START = 2
    }
}

/**
 * One-time events for Lobby screen
 */
sealed class LobbyUiEvent {
    data class LobbyCreated(val lobbyCode: String) : LobbyUiEvent()
    data class JoinedLobby(val lobbyId: String) : LobbyUiEvent()
    data class PlayerJoined(val playerName: String) : LobbyUiEvent()
    data class PlayerLeft(val playerName: String) : LobbyUiEvent()
    data class GameStarting(val matchId: String) : LobbyUiEvent()
    data class ShowError(val message: String) : LobbyUiEvent()
    data object LeftLobby : LobbyUiEvent()
}

/**
 * ViewModel for Lobby management
 */
class LobbyViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val lobbyRepository = LobbyRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<LobbyUiEvent>()
    val events: SharedFlow<LobbyUiEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        observeLobbyEvents()
    }
    
    // region Public Actions
    
    fun createLobby(name: String, maxPlayers: Int, isPrivate: Boolean) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            
            lobbyRepository.createLobby(
                hostId = userId,
                maxPlayers = maxPlayers,
                isPrivate = isPrivate,
                difficulty = DEFAULT_DIFFICULTY,
                category = DEFAULT_CATEGORY,
                totalQuestions = DEFAULT_QUESTIONS,
                timePerQuestion = DEFAULT_TIME_PER_QUESTION
            )
        }
    }
    
    fun joinLobby(lobbyCode: String) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            lobbyRepository.joinLobby(userId, lobbyCode)
        }
    }
    
    fun joinLobbyByCode(code: String) {
        joinLobby(code)
    }
    
    fun setReady(isReady: Boolean) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val lobbyId = getLobbyIdOrReturn() ?: return@launchSafely
            lobbyRepository.setReady(userId, lobbyId, isReady)
        }
    }
    
    fun startGame() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val lobbyId = getLobbyIdOrReturn() ?: return@launchSafely
            lobbyRepository.startGame(userId, lobbyId)
        }
    }
    
    fun leaveLobby() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val lobbyId = getLobbyIdOrReturn() ?: return@launchSafely
            
            lobbyRepository.leaveLobby(userId, lobbyId)
            resetState()
            emitEvent(LobbyUiEvent.LeftLobby)
        }
    }
    
    fun kickPlayer(targetUserId: String) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val lobbyId = getLobbyIdOrReturn() ?: return@launchSafely
            lobbyRepository.kickPlayer(userId, lobbyId, targetUserId)
        }
    }
    
    fun listLobbies() {
        lobbyRepository.requestLobbyList()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun observeLobbyEvents() {
        launchSafely {
            lobbyRepository.observeLobbyEvents().collect { event ->
                handleLobbyEvent(event)
            }
        }
    }
    
    private fun handleLobbyEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.LobbyCreated -> handleLobbyCreated(event)
            is LobbyEvent.PlayerJoined -> handlePlayerJoined(event)
            is LobbyEvent.PlayerReady -> handlePlayerReady(event)
            is LobbyEvent.GameStarting -> handleGameStarting()
            is LobbyEvent.LobbyListData -> handleLobbyList(event)
            is LobbyEvent.GameStarted -> handleGameStarted(event)
            else -> logDebug("Unhandled lobby event: $event")
        }
    }
    
    private fun handleLobbyCreated(event: LobbyEvent.LobbyCreated) {
        updateState {
            copy(lobbyId = event.lobbyId, lobbyCode = event.lobbyCode, isHost = true)
        }
        event.lobbyCode?.let { emitEvent(LobbyUiEvent.LobbyCreated(it)) }
    }
    
    private fun handlePlayerJoined(event: LobbyEvent.PlayerJoined) {
        val playerInfos = event.players.map { it.toPlayerInfo() }
        updateState { copy(players = playerInfos) }
    }
    
    private fun handlePlayerReady(event: LobbyEvent.PlayerReady) {
        updateState { copy(isReady = event.isReady) }
    }
    
    private fun handleGameStarting() {
        updateState { copy(gameStarting = true) }
    }
    
    private fun handleLobbyList(event: LobbyEvent.LobbyListData) {
        updateState { copy(lobbies = event.lobbies) }
    }
    
    private fun handleGameStarted(event: LobbyEvent.GameStarted) {
        updateState { copy(gameStarting = true, matchId = event.matchId) }
        emitEvent(LobbyUiEvent.GameStarting(event.matchId))
    }
    
    private suspend fun getUserIdOrReturn(): String? {
        return tokenRepository.getUserId().also {
            if (it == null) logError("User ID not available")
        }
    }
    
    private fun getLobbyIdOrReturn(): String? {
        return _state.value.lobbyId.also {
            if (it == null) logError("Lobby ID not available")
        }
    }
    
    private fun resetState() {
        _state.value = LobbyState()
    }
    
    // endregion
    
    // region Extension Functions
    
    private fun LobbyPlayer.toPlayerInfo() = DataModels.PlayerInfo(
        userId = userId,
        userName = username,
        isHost = isHost,
        isReady = isReady
    )
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: LobbyState.() -> LobbyState) {
        _state.update { it.update() }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: LobbyUiEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "LobbyViewModel"
        
        // Default lobby settings
        private const val DEFAULT_DIFFICULTY = "medium"
        private const val DEFAULT_CATEGORY = "general"
        private const val DEFAULT_QUESTIONS = 10
        private const val DEFAULT_TIME_PER_QUESTION = 30
    }
}
