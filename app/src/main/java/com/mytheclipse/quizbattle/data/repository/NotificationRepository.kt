package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class NotificationRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun requestNotificationList(userId: String, unreadOnly: Boolean = false) {
        val message = mapOf(
            "type" to "notification.list.sync",
            "payload" to mapOf(
                "userId" to userId,
                "unreadOnly" to unreadOnly
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun markAsRead(userId: String, notificationId: String) {
        val message = mapOf(
            "type" to "notification.mark.read",
            "payload" to mapOf(
                "userId" to userId,
                "notificationId" to notificationId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun markAllAsRead(userId: String) {
        val message = mapOf(
            "type" to "notification.mark.all.read",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
    
    fun deleteNotification(userId: String, notificationId: String) {
        val message = mapOf(
            "type" to "notification.delete",
            "payload" to mapOf(
                "userId" to userId,
                "notificationId" to notificationId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun observeNotifications(): Flow<NotificationEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("notification.") == true
            }
            .map { message ->
                parseNotificationEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseNotificationEvent(message: Map<String, Any>): NotificationEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "notification.list.data" -> {
                val notificationsList = payload["notifications"] as? List<Map<String, Any>> ?: emptyList()
                val notifications = notificationsList.map { n ->
                    DataModels.NotificationInfo(
                        notificationId = n["notificationId"] as? String ?: "",
                        type = n["type"] as? String ?: "system",
                        title = n["title"] as? String ?: "",
                        message = n["message"] as? String ?: "",
                        isRead = n["isRead"] as? Boolean ?: false,
                        createdAt = (n["createdAt"] as? Double)?.toLong() ?: 0L
                    )
                }
                NotificationEvent.NotificationListData(
                    notifications = notifications,
                    unreadCount = (payload["unreadCount"] as? Double)?.toInt() ?: 0
                )
            }
            "notification.marked.read" -> {
                NotificationEvent.MarkedAsRead(
                    notificationId = payload["notificationId"] as? String ?: ""
                )
            }
            "notification.all.marked.read" -> {
                NotificationEvent.AllMarkedAsRead(
                    count = (payload["count"] as? Double)?.toInt() ?: 0
                )
            }
            "notification.deleted" -> {
                NotificationEvent.NotificationDeleted(
                    notificationId = payload["notificationId"] as? String ?: ""
                )
            }
            else -> NotificationEvent.Unknown
        }
    }
}

sealed class NotificationEvent {
    data class NotificationListData(
        val notifications: List<DataModels.NotificationInfo>,
        val unreadCount: Int
    ) : NotificationEvent()
    
    data class MarkedAsRead(val notificationId: String) : NotificationEvent()
    data class AllMarkedAsRead(val count: Int) : NotificationEvent()
    data class NotificationDeleted(val notificationId: String) : NotificationEvent()
    object Unknown : NotificationEvent()
}
