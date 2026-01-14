package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.FriendDao
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.local.entity.FriendStatus
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

/**
 * Repository for managing friend data and WebSocket communication
 */
class FriendRepository(
    private val friendDao: FriendDao,
    private val webSocketManager: WebSocketManager
) {
    
    private val _friendRequestEvent = MutableStateFlow<FriendEvent?>(null)
    val friendRequestEvent: StateFlow<FriendEvent?> = _friendRequestEvent
    
    private val _matchInviteEvent = MutableStateFlow<MatchInviteEvent?>(null)
    val matchInviteEvent: StateFlow<MatchInviteEvent?> = _matchInviteEvent
    
    // Local database operations
    fun getAllFriends(): Flow<List<Friend>> = friendDao.getAllFriends()
    
    fun getPendingReceivedRequests(): Flow<List<Friend>> = friendDao.getPendingReceivedRequests()
    
    fun getPendingSentRequests(): Flow<List<Friend>> = friendDao.getPendingSentRequests()
    
    fun getPendingRequestCount(): Flow<Int> = friendDao.getPendingRequestCount()
    
    fun getOnlineFriends(): Flow<List<Friend>> = friendDao.getOnlineFriends()
    
    fun getOnlineFriendsCount(): Flow<Int> = friendDao.getOnlineFriendsCount()
    
    fun searchFriends(query: String): Flow<List<Friend>> = friendDao.searchFriends(query)
    
    suspend fun getFriendByFriendId(friendId: String): Friend? = friendDao.getFriendByFriendId(friendId)
    
    // WebSocket operations
    
    /**
     * Send friend request to another user
     */
    fun sendFriendRequest(userId: String, targetUserId: String, message: String? = null) {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("targetUserId", targetUserId)
            if (message != null) put("message", message)
        }
        webSocketManager.send("friend.request.send", payload)
    }
    
    /**
     * Respond to a friend request (accept or reject)
     */
    fun respondToFriendRequest(userId: String, requestId: String, accept: Boolean) {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("requestId", requestId)
            put("accept", accept)
        }
        webSocketManager.send("friend.request.respond", payload)
    }
    
    /**
     * Remove a friend
     */
    fun removeFriend(userId: String, friendId: String) {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("friendId", friendId)
        }
        webSocketManager.send("friend.remove", payload)
    }
    
    /**
     * Request friend list from server
     */
    fun requestFriendList(userId: String) {
        val payload = JSONObject().apply {
            put("userId", userId)
        }
        webSocketManager.send("friend.list.request", payload)
    }
    
    /**
     * Request pending friend requests
     */
    fun requestPendingRequests(userId: String, type: String = "incoming") {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("type", type)
        }
        webSocketManager.send("friend.request.list", payload)
    }
    
    /**
     * Send match invite to a friend
     */
    fun sendMatchInvite(
        senderId: String,
        receiverId: String,
        difficulty: String = "medium",
        category: String = "General",
        totalQuestions: Int = 5,
        timePerQuestion: Int = 10,
        message: String? = null
    ) {
        val gameSettings = JSONObject().apply {
            put("difficulty", difficulty)
            put("category", category)
            put("totalQuestions", totalQuestions)
            put("timePerQuestion", timePerQuestion)
        }
        
        val payload = JSONObject().apply {
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("gameSettings", gameSettings)
            if (message != null) put("message", message)
        }
        webSocketManager.send("match.invite.send", payload)
    }
    
    /**
     * Respond to match invite
     */
    fun respondToMatchInvite(userId: String, inviteId: String, accept: Boolean) {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("inviteId", inviteId)
            put("accept", accept)
        }
        webSocketManager.send("match.invite.respond", payload)
    }
    
    // Database update operations
    
    suspend fun saveFriend(friend: Friend) {
        friendDao.insert(friend)
    }
    
    suspend fun saveFriends(friends: List<Friend>) {
        friendDao.insertAll(friends)
    }
    
    suspend fun updateFriendStatus(id: String, status: FriendStatus) {
        friendDao.updateStatus(id, status)
    }
    
    suspend fun updateFriendOnlineStatus(friendId: String, isOnline: Boolean) {
        friendDao.updateOnlineStatus(friendId, isOnline)
    }
    
    suspend fun deleteFriend(friend: Friend) {
        friendDao.delete(friend)
    }
    
    suspend fun deleteFriendById(id: String) {
        friendDao.deleteById(id)
    }
    
    // Event handling
    
    fun handleWebSocketMessage(type: String, payload: JSONObject) {
        when (type) {
            "friend.request.received" -> {
                val sender = payload.getJSONObject("sender")
                _friendRequestEvent.value = FriendEvent.RequestReceived(
                    requestId = payload.getString("requestId"),
                    senderId = sender.getString("userId"),
                    senderName = sender.getString("username"),
                    senderPoints = sender.optInt("points", 0),
                    senderAvatarUrl = sender.optString("avatarUrl", null),
                    message = payload.optString("message", null)
                )
            }
            "friend.request.accepted" -> {
                val friend = payload.optJSONObject("friend")
                _friendRequestEvent.value = FriendEvent.RequestAccepted(
                    requestId = payload.getString("requestId"),
                    friendId = friend?.optString("userId"),
                    friendName = friend?.optString("username")
                )
            }
            "friend.request.response" -> {
                val status = payload.getString("status")
                _friendRequestEvent.value = if (status == "accepted") {
                    FriendEvent.RequestAccepted(
                        requestId = payload.getString("requestId"),
                        friendId = payload.optJSONObject("friend")?.optString("userId"),
                        friendName = payload.optJSONObject("friend")?.optString("username")
                    )
                } else {
                    FriendEvent.RequestRejected(payload.getString("requestId"))
                }
            }
            "friend.removed" -> {
                _friendRequestEvent.value = FriendEvent.FriendRemoved(
                    friendId = payload.getString("removedFriendId"),
                    removedBy = payload.getString("removedBy")
                )
            }
            "match.invite.received" -> {
                val sender = payload.getJSONObject("sender")
                val settings = payload.getJSONObject("gameSettings")
                _matchInviteEvent.value = MatchInviteEvent.InviteReceived(
                    inviteId = payload.getString("inviteId"),
                    senderId = sender.getString("userId"),
                    senderName = sender.getString("username"),
                    senderPoints = sender.optInt("points", 0),
                    senderWins = sender.optInt("wins", 0),
                    senderAvatarUrl = sender.optString("avatarUrl", null),
                    difficulty = settings.getString("difficulty"),
                    category = settings.getString("category"),
                    totalQuestions = settings.getInt("totalQuestions"),
                    timePerQuestion = settings.getInt("timePerQuestion"),
                    message = payload.optString("message", null),
                    expiresIn = payload.optLong("expiresIn", 60000)
                )
            }
            "match.invite.accepted" -> {
                val opponent = payload.getJSONObject("opponent")
                val settings = payload.getJSONObject("gameSettings")
                _matchInviteEvent.value = MatchInviteEvent.InviteAccepted(
                    inviteId = payload.getString("inviteId"),
                    matchId = payload.getString("matchId"),
                    opponentId = opponent.getString("userId"),
                    opponentName = opponent.getString("username"),
                    startIn = payload.optInt("startIn", 5)
                )
            }
            "match.invite.rejected" -> {
                _matchInviteEvent.value = MatchInviteEvent.InviteRejected(
                    inviteId = payload.getString("inviteId"),
                    rejectedBy = payload.optString("rejectedBy")
                )
            }
            "match.invite.expired" -> {
                _matchInviteEvent.value = MatchInviteEvent.InviteExpired(
                    inviteId = payload.getString("inviteId")
                )
            }
        }
    }
    
    fun clearFriendEvent() {
        _friendRequestEvent.value = null
    }
    
    fun clearMatchInviteEvent() {
        _matchInviteEvent.value = null
    }
}

sealed class FriendEvent {
    data class RequestReceived(
        val requestId: String,
        val senderId: String,
        val senderName: String,
        val senderPoints: Int,
        val senderAvatarUrl: String?,
        val message: String?
    ) : FriendEvent()
    
    data class RequestAccepted(
        val requestId: String,
        val friendId: String?,
        val friendName: String?
    ) : FriendEvent()
    
    data class RequestRejected(val requestId: String) : FriendEvent()
    
    data class FriendRemoved(
        val friendId: String,
        val removedBy: String
    ) : FriendEvent()
}

sealed class MatchInviteEvent {
    data class InviteReceived(
        val inviteId: String,
        val senderId: String,
        val senderName: String,
        val senderPoints: Int,
        val senderWins: Int,
        val senderAvatarUrl: String?,
        val difficulty: String,
        val category: String,
        val totalQuestions: Int,
        val timePerQuestion: Int,
        val message: String?,
        val expiresIn: Long
    ) : MatchInviteEvent()
    
    data class InviteAccepted(
        val inviteId: String,
        val matchId: String,
        val opponentId: String,
        val opponentName: String,
        val startIn: Int
    ) : MatchInviteEvent()
    
    data class InviteRejected(
        val inviteId: String,
        val rejectedBy: String?
    ) : MatchInviteEvent()
    
    data class InviteExpired(val inviteId: String) : MatchInviteEvent()
}
