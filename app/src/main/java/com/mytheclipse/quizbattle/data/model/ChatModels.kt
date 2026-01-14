package com.mytheclipse.quizbattle.data.model

/**
 * Chat Room model for UI display
 */
data class ChatRoom(
    val id: String,
    val name: String,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val isPrivate: Boolean = false,
    val description: String? = null,
    val memberCount: Int = 0,
    val avatarUrl: String? = null
) {
    val roomId: String get() = id
    val roomName: String get() = name
}

/**
 * Chat Message model for UI display
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isOwn: Boolean = false,
    val avatarUrl: String? = null
)
