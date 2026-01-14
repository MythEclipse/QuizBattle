package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.model.ChatMessage
import com.mytheclipse.quizbattle.data.model.ChatRoom
import com.mytheclipse.quizbattle.data.remote.api.ChatMessageResponse
import com.mytheclipse.quizbattle.data.remote.api.ChatRoomResponse
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
 * UI State for Chat screen
 */
data class ChatState(
    val rooms: List<ChatRoom> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val typingUsers: List<String> = emptyList(),
    val currentUserId: String? = null
) {
    /** Check if any room exists */
    val hasRooms: Boolean get() = rooms.isNotEmpty()
    
    /** Check if any message exists */
    val hasMessages: Boolean get() = messages.isNotEmpty()
    
    /** Get formatted typing indicator text */
    val typingIndicatorText: String?
        get() = when {
            typingUsers.isEmpty() -> null
            typingUsers.size == 1 -> "${typingUsers.first()} is typing..."
            typingUsers.size == 2 -> "${typingUsers[0]} and ${typingUsers[1]} are typing..."
            else -> "${typingUsers.take(2).joinToString(", ")} and ${typingUsers.size - 2} others are typing..."
        }
}

/**
 * One-time events for Chat screen
 */
sealed class ChatEvent {
    data class MessageSent(val roomId: String) : ChatEvent()
    data class RoomCreated(val roomId: String) : ChatEvent()
    data class ShowError(val message: String) : ChatEvent()
    data object RoomsLoaded : ChatEvent()
}

/**
 * ViewModel for Chat functionality
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val repository = ChatRepository()
    private val tokenRepository = TokenRepository(application)
    // endregion
    
    // region State
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        initializeChat()
    }
    
    // region Public Actions
    
    fun loadChatRooms() {
        launchSafely {
            setLoading(true)
            
            repository.getChatRooms()
                .onSuccess { roomResponses ->
                    val rooms = roomResponses.map { it.toChatRoom() }
                    updateState { copy(rooms = rooms, isLoading = false) }
                    emitEvent(ChatEvent.RoomsLoaded)
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_LOAD_ROOMS)
                }
        }
    }
    
    fun loadRoomMessages(roomId: String) {
        launchSafely {
            setLoading(true)
            
            repository.getRoomMessages(roomId)
                .onSuccess { messageResponses ->
                    val messages = messageResponses.map { it.toChatMessage() }
                    updateState { copy(messages = messages, isLoading = false) }
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_LOAD_MESSAGES)
                }
        }
    }
    
    fun sendMessage(roomId: String, content: String) {
        if (content.isBlank()) return
        
        launchSafely {
            repository.sendMessage(roomId, content)
                .onSuccess {
                    loadRoomMessages(roomId)
                    emitEvent(ChatEvent.MessageSent(roomId))
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_SEND_MESSAGE)
                }
        }
    }
    
    fun sendGlobalMessage(message: String) {
        if (message.isBlank()) return
        
        launchSafely {
            val userId = tokenRepository.getUserId() ?: return@launchSafely
            repository.sendGlobalChatMessage(userId, message)
        }
    }
    
    fun connectToRoom(roomId: String) {
        launchSafely {
            repository.joinRoom(roomId)
            loadRoomMessages(roomId)
        }
    }
    
    fun sendTypingIndicator(roomId: String) {
        launchSafely {
            val userId = tokenRepository.getUserId() ?: return@launchSafely
            repository.sendTypingIndicator(userId, roomId, true)
        }
    }
    
    fun createChatRoom(name: String, description: String? = null, isPrivate: Boolean = false) {
        launchSafely {
            repository.createChatRoom(name, description, isPrivate)
                .onSuccess { response ->
                    loadChatRooms()
                    emitEvent(ChatEvent.RoomCreated(response.id))
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_CREATE_ROOM)
                }
        }
    }
    
    fun loadPrivateChat(friendId: String) {
        launchSafely {
            setLoading(true)
            
            repository.observePrivateChatMessages(friendId).collect { event ->
                handlePrivateChatEvent(event)
            }
        }
    }
    
    fun sendPrivateMessage(friendId: String, message: String) {
        if (message.isBlank()) return
        
        launchSafely {
            val userId = tokenRepository.getUserId() ?: return@launchSafely
            repository.sendPrivateChatMessage(userId, friendId, message)
            
            // Optimistically add message to UI
            val chatMessage = createOptimisticMessage(userId, message)
            updateState { copy(messages = messages + chatMessage) }
        }
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun initializeChat() {
        loadChatRooms()
        observeChatMessages()
        loadCurrentUserId()
    }
    
    private fun loadCurrentUserId() {
        launchSafely {
            val userId = tokenRepository.getUserId()
            updateState { copy(currentUserId = userId) }
        }
    }
    
    private fun observeChatMessages() {
        launchSafely {
            repository.observeChatMessages().collect { event ->
                handleChatMessageEvent(event)
            }
        }
    }
    
    private fun handleChatMessageEvent(event: ChatMessageEvent) {
        when (event) {
            is ChatMessageEvent.GlobalMessage -> {
                // Add new message to current room if it matches
                logDebug("Global message received: ${event.message}")
            }
            is ChatMessageEvent.PrivateMessage -> {
                // Handle private message
                logDebug("Private message received from: ${event.senderName}")
            }
            is ChatMessageEvent.TypingIndicator -> {
                handleTypingIndicator(event)
            }
            else -> { /* Ignore other events */ }
        }
    }
    
    private fun handleTypingIndicator(event: ChatMessageEvent.TypingIndicator) {
        updateState {
            val updatedTypingUsers = if (event.isTyping) {
                typingUsers + event.username
            } else {
                typingUsers - event.username
            }
            copy(typingUsers = updatedTypingUsers.distinct())
        }
    }
    
    private fun handlePrivateChatEvent(event: ChatMessageEvent) {
        when (event) {
            is ChatMessageEvent.PrivateMessage -> {
                val message = ChatMessage(
                    id = event.messageId,
                    senderId = event.senderId,
                    senderName = event.senderName,
                    content = event.message,
                    timestamp = event.timestamp
                )
                updateState {
                    copy(messages = messages + message, isLoading = false)
                }
            }
            else -> { /* Ignore other events */ }
        }
    }
    
    private fun createOptimisticMessage(userId: String, message: String) = ChatMessage(
        id = "temp_${System.currentTimeMillis()}",
        senderId = userId,
        senderName = SELF_USERNAME,
        content = message,
        timestamp = System.currentTimeMillis(),
        isOwn = true
    )
    
    // endregion
    
    // region Extension Functions
    
    private fun ChatRoomResponse.toChatRoom() = ChatRoom(
        id = id,
        name = name,
        lastMessage = messages.lastOrNull()?.content,
        lastMessageTime = System.currentTimeMillis(),
        unreadCount = 0,
        isPrivate = isPrivate == 1,
        description = description
    )
    
    private fun ChatMessageResponse.toChatMessage() = ChatMessage(
        id = id,
        senderId = userId,
        senderName = user.name ?: UNKNOWN_USER,
        content = content,
        timestamp = System.currentTimeMillis()
    )
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: ChatState.() -> ChatState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun handleError(message: String) {
        updateState { copy(isLoading = false, error = message) }
        emitEvent(ChatEvent.ShowError(message))
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: ChatEvent) {
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
        private const val TAG = "ChatViewModel"
        
        // Default values
        private const val ROOM_TYPE_GROUP = "group"
        private const val UNKNOWN_USER = "Unknown"
        private const val SELF_USERNAME = "You"
        
        // Error messages
        private const val ERROR_LOAD_ROOMS = "Failed to load chat rooms"
        private const val ERROR_LOAD_MESSAGES = "Failed to load messages"
        private const val ERROR_SEND_MESSAGE = "Failed to send message"
        private const val ERROR_CREATE_ROOM = "Failed to create chat room"
    }
}
