package com.mytheclipse.quizbattle.data.model

/**
 * Unified Friend Events for WebSocket communication
 */
sealed class FriendEvent {
    // Request related events
    data class RequestReceived(
        val requestId: String,
        val senderId: String,
        val senderName: String,
        val senderPoints: Int = 0,
        val senderAvatarUrl: String? = null,
        val message: String? = null
    ) : FriendEvent()
    
    data class RequestSent(
        val requestId: String,
        val targetUserId: String,
        val targetUsername: String
    ) : FriendEvent()
    
    data class RequestAccepted(
        val requestId: String,
        val friendId: String? = null,
        val friendName: String? = null
    ) : FriendEvent()
    
    data class RequestRejected(val requestId: String) : FriendEvent()
    
    data class FriendRemoved(
        val friendId: String,
        val removedBy: String? = null
    ) : FriendEvent()
    
    // List related events
    data class FriendListData(
        val friends: List<FriendInfo>,
        val pendingRequests: List<FriendRequestInfo>,
        val totalFriends: Int
    ) : FriendEvent()
    
    // Challenge related events
    data class ChallengeSent(val challengeId: String) : FriendEvent()
    
    // Unknown event fallback
    data object Unknown : FriendEvent()
}

/**
 * Friend information model
 */
data class FriendInfo(
    val userId: String,
    val username: String,
    val level: Int = 1,
    val avatar: String? = null,
    val status: String = "offline",
    val wins: Int = 0,
    val losses: Int = 0
)

/**
 * Friend request information model
 */
data class FriendRequestInfo(
    val requestId: String,
    val senderId: String,
    val senderName: String
)

/**
 * Match invite events
 */
sealed class MatchInviteEvent {
    data class InviteReceived(
        val inviteId: String,
        val senderId: String,
        val senderName: String,
        val senderPoints: Int = 0,
        val senderWins: Int = 0,
        val senderAvatarUrl: String? = null,
        val difficulty: String = "medium",
        val category: String = "General",
        val totalQuestions: Int = 5,
        val timePerQuestion: Int = 10,
        val message: String? = null,
        val expiresIn: Long = 60000
    ) : MatchInviteEvent()
    
    data class InviteAccepted(
        val inviteId: String,
        val matchId: String,
        val opponentId: String,
        val opponentName: String,
        val startIn: Int = 5
    ) : MatchInviteEvent()
    
    data class InviteRejected(
        val inviteId: String,
        val rejectedBy: String? = null
    ) : MatchInviteEvent()
    
    data class InviteExpired(val inviteId: String) : MatchInviteEvent()
}
