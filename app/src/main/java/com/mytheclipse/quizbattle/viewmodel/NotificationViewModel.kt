package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Notification screen
 */
data class NotificationState(
    val notifications: List<DataModels.NotificationInfo> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Check if there are any notifications */
    val hasNotifications: Boolean get() = notifications.isNotEmpty()
    
    /** Check if there are unread notifications */
    val hasUnread: Boolean get() = unreadCount > 0
    
    /** Get unread notifications only */
    val unreadNotifications: List<DataModels.NotificationInfo>
        get() = notifications.filter { !it.isRead }
    
    /** Get read notifications only */
    val readNotifications: List<DataModels.NotificationInfo>
        get() = notifications.filter { it.isRead }
}

/**
 * One-time events for Notification screen
 */
sealed class NotificationUiEvent {
    data class MarkedAsRead(val notificationId: String) : NotificationUiEvent()
    data object AllMarkedAsRead : NotificationUiEvent()
    data class Deleted(val notificationId: String) : NotificationUiEvent()
    data class ShowError(val message: String) : NotificationUiEvent()
}

/**
 * ViewModel for Notification management
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val notificationRepository = NotificationRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<NotificationUiEvent>()
    val events: SharedFlow<NotificationUiEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        loadNotifications()
        observeNotifications()
    }
    
    // region Public Actions
    
    fun loadNotifications(unreadOnly: Boolean = false) {
        launchSafely {
            setLoading(true)
            val userId = getUserIdOrReturn() ?: return@launchSafely
            notificationRepository.requestNotificationList(userId, unreadOnly)
        }
    }
    
    fun markAsRead(notificationId: String) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            notificationRepository.markAsRead(userId, notificationId)
        }
    }
    
    fun markAllAsRead() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            notificationRepository.markAllAsRead(userId)
        }
    }
    
    fun deleteNotification(notificationId: String) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            notificationRepository.deleteNotification(userId, notificationId)
        }
    }
    
    fun refresh() {
        loadNotifications()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun observeNotifications() {
        launchSafely {
            notificationRepository.observeNotifications().collect { event ->
                handleNotificationEvent(event)
            }
        }
    }
    
    private fun handleNotificationEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.NotificationListData -> handleListData(event)
            is NotificationEvent.MarkedAsRead -> handleMarkedAsRead(event)
            is NotificationEvent.AllMarkedAsRead -> handleAllMarkedAsRead()
            is NotificationEvent.NotificationDeleted -> handleDeleted(event)
            else -> logDebug("Unhandled notification event: $event")
        }
    }
    
    private fun handleListData(event: NotificationEvent.NotificationListData) {
        updateState {
            copy(
                notifications = event.notifications,
                unreadCount = event.unreadCount,
                isLoading = false
            )
        }
    }
    
    private fun handleMarkedAsRead(event: NotificationEvent.MarkedAsRead) {
        updateState {
            val updatedNotifications = notifications.map { notif ->
                if (notif.notificationId == event.notificationId) {
                    notif.copy(isRead = true)
                } else notif
            }
            copy(
                notifications = updatedNotifications,
                unreadCount = (unreadCount - 1).coerceAtLeast(0)
            )
        }
        emitEvent(NotificationUiEvent.MarkedAsRead(event.notificationId))
    }
    
    private fun handleAllMarkedAsRead() {
        updateState {
            copy(
                notifications = notifications.map { it.copy(isRead = true) },
                unreadCount = 0
            )
        }
        emitEvent(NotificationUiEvent.AllMarkedAsRead)
    }
    
    private fun handleDeleted(event: NotificationEvent.NotificationDeleted) {
        updateState {
            copy(
                notifications = notifications.filter { 
                    it.notificationId != event.notificationId 
                }
            )
        }
        emitEvent(NotificationUiEvent.Deleted(event.notificationId))
    }
    
    private suspend fun getUserIdOrReturn(): String? {
        return tokenRepository.getUserId().also {
            if (it == null) {
                logError("User ID not available")
                updateState { copy(isLoading = false) }
            }
        }
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: NotificationState.() -> NotificationState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: NotificationUiEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "NotificationViewModel"
    }
}
