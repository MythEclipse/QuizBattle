package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OnlineFriendsRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun sendFriendRequest(senderId: String, targetUsername: String) {
        val message = mapOf(
            "type" to "friend.request.send",
            "payload" to mapOf(
                "senderId" to senderId,
                "targetUsername" to targetUsername
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun acceptFriendRequest(userId: String, requestId: String) {
        val message = mapOf(
            "type" to "friend.request.accept",
            "payload" to mapOf(
                "userId" to userId,
                "requestId" to requestId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun rejectFriendRequest(userId: String, requestId: String) {
        val message = mapOf(
            "type" to "friend.request.reject",
            "payload" to mapOf(
                "userId" to userId,
                "requestId" to requestId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun removeFriend(userId: String, friendId: String) {
        val message = mapOf(
            "type" to "friend.remove",
            "payload" to mapOf(
                "userId" to userId,
                "friendId" to friendId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun requestFriendList(userId: String) {
        val message = mapOf(
            "type" to "friend.list.request",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
    
    fun challengeFriend(challengerId: String, targetFriendId: String, gameSettings: Map<String, Any>) {
        val message = mapOf(
            "type" to "friend.challenge",
            "payload" to mapOf(
                "challengerId" to challengerId,
                "targetFriendId" to targetFriendId,
                "gameSettings" to gameSettings
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun observeFriendEvents(): Flow<FriendEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("friend.") == true
            }
            .map { message ->
                parseFriendEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseFriendEvent(message: Map<String, Any>): FriendEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "friend.request.sent" -> {
                val targetUser = payload["targetUser"] as? Map<String, Any> ?: emptyMap()
                FriendEvent.RequestSent(
                    requestId = payload["requestId"] as? String ?: "",
                    targetUserId = targetUser["id"] as? String ?: "",
                    targetUsername = targetUser["name"] as? String ?: ""
                )
            }
            "friend.request.accepted" -> {
                val friendship = payload["friendship"] as? Map<String, Any> ?: emptyMap()
                FriendEvent.RequestAccepted(
                    friendshipId = friendship["friendshipId"] as? String ?: ""
                )
            }
            "friend.request.rejected" -> {
                FriendEvent.RequestRejected(
                    requestId = payload["requestId"] as? String ?: ""
                )
            }
            "friend.removed" -> {
                FriendEvent.FriendRemoved(
                    friendId = payload["friendId"] as? String ?: ""
                )
            }
            "friend.list.data" -> {
                val friendsList = payload["friends"] as? List<Map<String, Any>> ?: emptyList()
                val friends = friendsList.map { item ->
                    FriendInfo(
                        userId = item["userId"] as? String ?: "",
                        username = item["username"] as? String ?: "",
                        level = (item["level"] as? Double)?.toInt() ?: 1,
                        avatar = null, // Avatar disabled for user management
                        status = item["status"] as? String ?: "offline",
                        wins = (item["wins"] as? Double)?.toInt() ?: 0,
                        losses = (item["losses"] as? Double)?.toInt() ?: 0
                    )
                }
                
                val pendingList = payload["pendingRequests"] as? List<Map<String, Any>> ?: emptyList()
                val pendingRequests = pendingList.map { item ->
                    FriendRequestInfo(
                        requestId = item["requestId"] as? String ?: "",
                        senderId = item["senderId"] as? String ?: "",
                        senderName = item["senderName"] as? String ?: ""
                    )
                }
                
                FriendEvent.FriendListData(
                    friends = friends,
                    pendingRequests = pendingRequests,
                    totalFriends = (payload["totalFriends"] as? Double)?.toInt() ?: 0
                )
            }
            "friend.challenge.sent" -> {
                FriendEvent.ChallengeSent(
                    challengeId = payload["challengeId"] as? String ?: ""
                )
            }
            else -> FriendEvent.Unknown
        }
    }
}

data class FriendInfo(
    val userId: String,
    val username: String,
    val level: Int,
    val avatar: String?,
    val status: String,
    val wins: Int,
    val losses: Int
)

data class FriendRequestInfo(
    val requestId: String,
    val senderId: String,
    val senderName: String
)

sealed class FriendEvent {
    data class RequestSent(
        val requestId: String,
        val targetUserId: String,
        val targetUsername: String
    ) : FriendEvent()
    
    data class RequestAccepted(val friendshipId: String) : FriendEvent()
    data class RequestRejected(val requestId: String) : FriendEvent()
    data class FriendRemoved(val friendId: String) : FriendEvent()
    
    data class FriendListData(
        val friends: List<FriendInfo>,
        val pendingRequests: List<FriendRequestInfo>,
        val totalFriends: Int
    ) : FriendEvent()
    
    data class ChallengeSent(val challengeId: String) : FriendEvent()
    object Unknown : FriendEvent()
}
