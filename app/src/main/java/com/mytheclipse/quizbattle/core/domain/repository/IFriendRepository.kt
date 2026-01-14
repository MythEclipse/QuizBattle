package com.mytheclipse.quizbattle.core.domain.repository

import com.mytheclipse.quizbattle.data.local.entity.Friend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining friend-related data operations
 * Follows Repository Pattern for clean architecture
 */
interface IFriendRepository {
    
    // ===== Events =====
    val friendRequestEvent: StateFlow<FriendEvent?>
    val matchInviteEvent: StateFlow<MatchInviteEvent?>
    
    // ===== Local Database Operations =====
    
    fun getAllFriends(): Flow<List<Friend>>
    
    fun getPendingReceivedRequests(): Flow<List<Friend>>
    
    fun getPendingSentRequests(): Flow<List<Friend>>
    
    fun getPendingRequestCount(): Flow<Int>
    
    fun getOnlineFriends(): Flow<List<Friend>>
    
    fun getOnlineFriendsCount(): Flow<Int>
    
    fun searchFriends(query: String): Flow<List<Friend>>
    
    suspend fun getFriendByFriendId(friendId: String): Friend?
    
    // ===== WebSocket Operations =====
    
    fun sendFriendRequest(userId: String, targetUserId: String, message: String? = null)
    
    fun respondToFriendRequest(userId: String, requestId: String, accept: Boolean)
    
    fun removeFriend(userId: String, friendId: String)
    
    fun requestFriendList(userId: String)
    
    fun requestPendingRequests(userId: String, type: String = "incoming")
    
    fun sendMatchInvite(
        senderId: String,
        receiverId: String,
        difficulty: String = "medium",
        category: String = "General",
        totalQuestions: Int = 5,
        timePerQuestion: Int = 10,
        message: String? = null
    )
    
    fun respondToMatchInvite(userId: String, inviteId: String, accept: Boolean)
    
    // ===== Database Update Operations =====
    
    suspend fun saveFriend(friend: Friend)
    
    suspend fun saveFriends(friends: List<Friend>)
    
    suspend fun updateFriend(friend: Friend)
    
    suspend fun deleteFriend(friend: Friend)
    
    suspend fun deleteFriendByFriendId(friendId: String)
    
    suspend fun deleteAllFriends()
    
    suspend fun updateFriendOnlineStatus(friendId: String, isOnline: Boolean)
}

/**
 * Events related to friend requests
 */
sealed class FriendEvent {
    data class RequestReceived(
        val requestId: String,
        val fromUserId: String,
        val fromUsername: String,
        val fromAvatar: String?,
        val message: String?
    ) : FriendEvent()
    
    data class RequestAccepted(
        val friendId: String,
        val friendUsername: String
    ) : FriendEvent()
    
    data class RequestRejected(val requestId: String) : FriendEvent()
    
    data class FriendRemoved(val friendId: String) : FriendEvent()
    
    data class FriendOnline(val friendId: String) : FriendEvent()
    
    data class FriendOffline(val friendId: String) : FriendEvent()
}

/**
 * Events related to match invites
 */
sealed class MatchInviteEvent {
    data class InviteReceived(
        val inviteId: String,
        val fromUserId: String,
        val fromUsername: String,
        val fromAvatar: String?,
        val difficulty: String,
        val category: String,
        val totalQuestions: Int,
        val timePerQuestion: Int,
        val message: String?
    ) : MatchInviteEvent()
    
    data class InviteAccepted(
        val inviteId: String,
        val matchId: String,
        val opponentId: String
    ) : MatchInviteEvent()
    
    data class InviteRejected(val inviteId: String) : MatchInviteEvent()
    
    data class InviteExpired(val inviteId: String) : MatchInviteEvent()
}
