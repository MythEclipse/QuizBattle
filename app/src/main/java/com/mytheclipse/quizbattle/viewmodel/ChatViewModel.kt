package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val rooms: List<ChatRoom> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val typingUsers: List<String> = emptyList(),
    val currentUserId: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ChatRepository()
    private val tokenRepository = TokenRepository(application)
    
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    init {
        loadChatRooms()
        observeChatMessages()
        // Load current user id for message ownership checks
        viewModelScope.launch {
            _state.value = _state.value.copy(currentUserId = tokenRepository.getUserId())
        }
    }
    
    private fun observeChatMessages() {
        viewModelScope.launch {
            repository.observeChatMessages().collect { event ->
                when (event) {
                    is ChatMessageEvent.GlobalMessage -> {
                        // Add new message to current room if it matches
                    }
                    is ChatMessageEvent.PrivateMessage -> {
                        // Handle private message
                    }
                    is ChatMessageEvent.TypingIndicator -> {
                        if (event.isTyping) {
                            _state.value = _state.value.copy(
                                typingUsers = _state.value.typingUsers + event.username
                            )
                        } else {
                            _state.value = _state.value.copy(
                                typingUsers = _state.value.typingUsers - event.username
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun loadChatRooms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getChatRooms().onSuccess { roomResponses ->
                val rooms = roomResponses.map { r ->
                    ChatRoom(
                        roomId = r.id,
                        roomName = r.name,
                        roomType = "group",
                        lastMessage = r.messages.lastOrNull()?.content,
                        unreadCount = 0,
                        createdAt = System.currentTimeMillis()
                    )
                }
                _state.value = _state.value.copy(
                    rooms = rooms,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load chat rooms"
                )
            }
        }
    }
    
    fun loadRoomMessages(roomId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getRoomMessages(roomId).onSuccess { messageResponses ->
                val messages = messageResponses.map { m ->
                    ChatMessage(
                        messageId = m.id,
                        userId = m.userId,
                        userName = m.user.name ?: "Unknown",
                        message = m.content,
                        createdAt = System.currentTimeMillis()
                    )
                }
                _state.value = _state.value.copy(
                    messages = messages,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load messages"
                )
            }
        }
    }
    
    fun sendMessage(roomId: String, content: String) {
        viewModelScope.launch {
            repository.sendMessage(roomId, content).onSuccess {
                loadRoomMessages(roomId)
            }
        }
    }
    
    fun sendGlobalMessage(message: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            repository.sendGlobalChatMessage(userId, message)
        }
    }
    
    fun connectToRoom(roomId: String) {
        viewModelScope.launch {
            repository.joinRoom(roomId)
            // Load actual room messages from API
            loadRoomMessages(roomId)
        }
    }
    
    fun sendTypingIndicator(roomId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            repository.sendTypingIndicator(userId, roomId, true)
        }
    }
    
    fun createChatRoom(name: String, description: String? = null, isPrivate: Boolean = false) {
        viewModelScope.launch {
            repository.createChatRoom(name, description, isPrivate).onSuccess {
                loadChatRooms()
            }
        }
    }
}
