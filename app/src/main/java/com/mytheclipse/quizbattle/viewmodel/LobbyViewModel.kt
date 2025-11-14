package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
)

class LobbyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val lobbyRepository = LobbyRepository()
    
    private val _state = MutableStateFlow(LobbyState())
    val state: StateFlow<LobbyState> = _state.asStateFlow()
    
    init {
        observeLobbyEvents()
    }
    
    fun observeLobbyEvents() {
        viewModelScope.launch {
            lobbyRepository.observeLobbyEvents().collect { event ->
                when (event) {
                    is LobbyEvent.LobbyCreated -> {
                        _state.value = _state.value.copy(
                            lobbyId = event.lobbyId,
                            lobbyCode = event.lobbyCode,
                            isHost = true
                        )
                    }
                    is LobbyEvent.PlayerJoined -> {
                        val playerInfos = event.players.map { p ->
                            DataModels.PlayerInfo(
                                userId = p.userId,
                                userName = p.username,
                                isHost = p.isHost,
                                isReady = p.isReady
                            )
                        }
                        _state.value = _state.value.copy(
                            players = playerInfos
                        )
                    }
                    is LobbyEvent.PlayerReady -> {
                        _state.value = _state.value.copy(
                            isReady = event.isReady
                        )
                    }
                    is LobbyEvent.GameStarting -> {
                        _state.value = _state.value.copy(
                            gameStarting = true
                        )
                    }
                    is LobbyEvent.LobbyListData -> {
                        _state.value = _state.value.copy(
                            lobbies = event.lobbies
                        )
                    }
                    is LobbyEvent.GameStarted -> {
                        _state.value = _state.value.copy(
                            gameStarting = true,
                            matchId = event.matchId
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun createLobby(name: String, maxPlayers: Int, isPrivate: Boolean) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            lobbyRepository.createLobby(
                hostId = userId,
                maxPlayers = maxPlayers,
                isPrivate = isPrivate,
                difficulty = "medium",
                category = "general",
                totalQuestions = 10,
                timePerQuestion = 30
            )
        }
    }
    
    fun joinLobby(lobbyCode: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            lobbyRepository.joinLobby(userId, lobbyCode)
        }
    }
    
    fun setReady(isReady: Boolean) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val lobbyId = _state.value.lobbyId ?: return@launch
            lobbyRepository.setReady(userId, lobbyId, isReady)
        }
    }
    
    fun startGame() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val lobbyId = _state.value.lobbyId ?: return@launch
            lobbyRepository.startGame(userId, lobbyId)
        }
    }
    
    fun leaveLobby() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val lobbyId = _state.value.lobbyId ?: return@launch
            lobbyRepository.leaveLobby(userId, lobbyId)
            _state.value = LobbyState()
        }
    }
    
    fun kickPlayer(targetUserId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val lobbyId = _state.value.lobbyId ?: return@launch
            lobbyRepository.kickPlayer(userId, lobbyId, targetUserId)
        }
    }
    
    fun listLobbies() {
        lobbyRepository.requestLobbyList()
    }
    
    fun joinLobbyByCode(code: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            lobbyRepository.joinLobby(userId, code)
        }
    }
}
