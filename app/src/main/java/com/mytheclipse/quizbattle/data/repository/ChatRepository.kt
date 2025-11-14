package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.remote.api.*
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class ChatRepository {
    
    private val apiService = ApiConfig.createService(ChatApiService::class.java)
    private val webSocketManager = WebSocketManager.getInstance()
    
    suspend fun getChatRooms(): Result<List<ChatRoomResponse>> {
        return try {
            val response = apiService.getChatRooms()
            if (response.success) {
                Result.success(response.rooms)
            } else {
                Result.failure(Exception("Failed to fetch chat rooms"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createChatRoom(name: String, description: String? = null, isPrivate: Boolean = false): Result<ChatRoomResponse> {
        return try {
            val response = apiService.createChatRoom(CreateRoomRequest(name, description, isPrivate))
            if (response.success) {
                Result.success(response.room)
            } else {
                Result.failure(Exception("Failed to create chat room"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoomMessages(roomId: String, limit: Int = 50, before: String? = null): Result<List<ChatMessageResponse>> {
        return try {
            val response = apiService.getRoomMessages(roomId, limit, before)
            if (response.success) {
                Result.success(response.messages)
            } else {
                Result.failure(Exception("Failed to fetch messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(roomId: String, content: String): Result<ChatMessageResponse> {
        return try {
            val response = apiService.sendMessage(roomId, SendMessageRequest(content))
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun joinRoom(roomId: String): Result<Boolean> {
        return try {
            val response = apiService.joinRoom(roomId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun leaveRoom(roomId: String): Result<Boolean> {
        return try {
            val response = apiService.leaveRoom(roomId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMessage(messageId: String): Result<Boolean> {
        return try {
            val response = apiService.deleteMessage(messageId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // WebSocket functions
    fun sendGlobalChatMessage(userId: String, message: String) {
        val wsMessage = mapOf(
            "type" to "chat:global:send",
            "payload" to mapOf(
                "userId" to userId,
                "message" to message
            )
        )
        webSocketManager.sendMessage(wsMessage)
    }
    
    fun sendPrivateChatMessage(senderId: String, receiverId: String, message: String) {
        val wsMessage = mapOf(
            "type" to "chat:private:send",
            "payload" to mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message,
                "timestamp" to System.currentTimeMillis()
            )
        )
        webSocketManager.sendMessage(wsMessage)
    }
    
    fun observeChatMessages(): Flow<ChatMessageEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("chat:") == true
            }
            .map { message ->
                parseChatMessage(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseChatMessage(message: Map<String, Any>): ChatMessageEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "chat:global:message" -> {
                val sender = payload["sender"] as? Map<String, Any> ?: emptyMap()
                ChatMessageEvent.GlobalMessage(
                    messageId = payload["messageId"] as? String ?: "",
                    senderId = sender["userId"] as? String ?: "",
                    senderName = sender["username"] as? String ?: "",
                    message = payload["message"] as? String ?: "",
                    timestamp = (payload["timestamp"] as? Double)?.toLong() ?: 0L
                )
            }
            "chat:private:message" -> {
                val sender = payload["sender"] as? Map<String, Any> ?: emptyMap()
                ChatMessageEvent.PrivateMessage(
                    messageId = payload["messageId"] as? String ?: "",
                    conversationId = payload["conversationId"] as? String ?: "",
                    senderId = sender["userId"] as? String ?: "",
                    senderName = sender["username"] as? String ?: "",
                    message = payload["message"] as? String ?: "",
                    timestamp = (payload["timestamp"] as? Double)?.toLong() ?: 0L,
                    isRead = payload["isRead"] as? Boolean ?: false
                )
            }
            "chat:typing:indicator" -> {
                ChatMessageEvent.TypingIndicator(
                    userId = payload["userId"] as? String ?: "",
                    username = payload["username"] as? String ?: "",
                    isTyping = payload["isTyping"] as? Boolean ?: false
                )
            }
            else -> ChatMessageEvent.Unknown
        }
    }
    
    fun sendTypingIndicator(userId: String, targetUserId: String? = null, isTyping: Boolean) {
        val wsMessage = buildMap {
            put("type", "chat:typing")
            put("payload", buildMap {
                put("userId", userId)
                targetUserId?.let { put("targetUserId", it) }
                put("isTyping", isTyping)
            })
        }
        webSocketManager.sendMessage(wsMessage)
    }
    
    fun markAsRead(userId: String, targetUserId: String) {
        val wsMessage = mapOf(
            "type" to "chat:mark:read",
            "payload" to mapOf(
                "userId" to userId,
                "targetUserId" to targetUserId
            )
        )
        webSocketManager.sendMessage(wsMessage)
    }
}

sealed class ChatMessageEvent {
    data class GlobalMessage(
        val messageId: String,
        val senderId: String,
        val senderName: String,
        val message: String,
        val timestamp: Long
    ) : ChatMessageEvent()
    
    data class PrivateMessage(
        val messageId: String,
        val conversationId: String,
        val senderId: String,
        val senderName: String,
        val message: String,
        val timestamp: Long,
        val isRead: Boolean
    ) : ChatMessageEvent()
    
    data class TypingIndicator(
        val userId: String,
        val username: String,
        val isTyping: Boolean
    ) : ChatMessageEvent()
    
    object Unknown : ChatMessageEvent()
}

// Data classes for UI
data class ChatRoom(
    val roomId: String,
    val roomName: String,
    val roomType: String,
    val lastMessage: String?,
    val unreadCount: Int,
    val createdAt: Long
)

data class ChatMessage(
    val messageId: String,
    val userId: String,
    val userName: String,
    val message: String,
    val createdAt: Long
)
