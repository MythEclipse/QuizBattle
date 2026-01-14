package com.mytheclipse.quizbattle.data.repository

object DataModels {
    // Social Media Data Classes
    data class Post(
        val postId: String,
        val userId: String,
        val userName: String,
        val content: String,
        val imageUrl: String? = null,
        val likesCount: Int,
        val commentsCount: Int,
        val isLikedByUser: Boolean,
        val createdAt: Long,
        val updatedAt: Long
    )

    // Leaderboard Data Classes
    data class LeaderboardEntry(
        val rank: Int = 0,
        val userId: String,
        val userName: String,
        val score: Int,
        val wins: Int,
        val losses: Int,
        val mmr: Int,
        val isCurrentUser: Boolean = false
    )

    // Lobby Data Classes
    data class LobbyInfo(
        val lobbyId: String,
        val lobbyName: String,
        val hostId: String,
        val hostName: String,
        val currentPlayers: Int,
        val maxPlayers: Int,
        val isPrivate: Boolean,
        val status: String
    )

    data class PlayerInfo(
        val userId: String,
        val userName: String,
        val isHost: Boolean,
        val isReady: Boolean
    )

    // Online Game Data Classes
    data class Question(
        val questionId: String,
        val questionText: String,
        val options: List<String>,
        val correctAnswer: String? = null,
        val category: String? = null
    )

    // Notification Data Classes  
    data class NotificationInfo(
        val notificationId: String,
        val type: String,
        val title: String,
        val message: String,
        val isRead: Boolean,
        val createdAt: Long
    )
}
