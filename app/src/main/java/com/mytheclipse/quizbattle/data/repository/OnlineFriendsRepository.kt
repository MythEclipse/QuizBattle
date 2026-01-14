package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.model.FriendEvent
import com.mytheclipse.quizbattle.data.model.FriendInfo
import com.mytheclipse.quizbattle.data.model.FriendRequestInfo
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OnlineFriendsRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun sendFriendRequest(senderId: String, targetUsername: String) {
        AppLogger.Friend.requestSent(targetUsername)
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
        AppLogger.Friend.requestAccepted(requestId)
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
        AppLogger.Friend.requestRejected(requestId)
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
        AppLogger.Friend.inviteSent(targetFriendId)
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
                    requestId = friendship["friendshipId"] as? String ?: ""
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
                        level = calculateLevelFromPoints((item["points"] as? Double)?.toInt() ?: 0),
                        avatar = item["avatarUrl"] as? String,
                        status = item["status"] as? String ?: "offline",
                        wins = (item["wins"] as? Double)?.toInt() ?: 0,
                        losses = 0 // Not provided by backend
                    )
                }
                
                val pendingCount = (payload["pendingRequests"] as? Double)?.toInt() ?: 0
                
                FriendEvent.FriendListData(
                    friends = friends,
                    pendingRequests = emptyList(), // Pending requests now sent separately
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
    
    /**
     * Calculate level from points (100 points per level)
     */
    private fun calculateLevelFromPoints(points: Int): Int {
        return (points / 100) + 1
    }
}
