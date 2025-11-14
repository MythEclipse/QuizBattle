package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationState(
    val notifications: List<DataModels.NotificationInfo> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val notificationRepository = NotificationRepository()
    
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()
    
    init {
        loadNotifications()
        observeNotifications()
    }
    
    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.observeNotifications().collect { event ->
                when (event) {
                    is NotificationEvent.NotificationListData -> {
                        _state.value = _state.value.copy(
                            notifications = event.notifications,
                            unreadCount = event.unreadCount,
                            isLoading = false
                        )
                    }
                    is NotificationEvent.MarkedAsRead -> {
                        _state.value = _state.value.copy(
                            notifications = _state.value.notifications.map { notif ->
                                if (notif.notificationId == event.notificationId) {
                                    notif.copy(isRead = true)
                                } else notif
                            },
                            unreadCount = _state.value.unreadCount - 1
                        )
                    }
                    is NotificationEvent.AllMarkedAsRead -> {
                        _state.value = _state.value.copy(
                            notifications = _state.value.notifications.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                    is NotificationEvent.NotificationDeleted -> {
                        _state.value = _state.value.copy(
                            notifications = _state.value.notifications.filter { 
                                it.notificationId != event.notificationId 
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun loadNotifications(unreadOnly: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            notificationRepository.requestNotificationList(userId, unreadOnly)
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            notificationRepository.markAsRead(userId, notificationId)
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            notificationRepository.markAllAsRead(userId)
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            notificationRepository.deleteNotification(userId, notificationId)
        }
    }
}
