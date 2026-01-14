package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.local.entity.FriendStatus
import com.mytheclipse.quizbattle.data.model.FriendEvent
import com.mytheclipse.quizbattle.data.model.MatchInviteEvent
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.data.repository.FriendRepository
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Friend List Screen
 */
data class FriendListState(
    val friends: List<Friend> = emptyList(),
    val pendingReceived: List<Friend> = emptyList(),
    val pendingSent: List<Friend> = emptyList(),
    val onlineFriends: List<Friend> = emptyList(),
    val pendingCount: Int = 0,
    val onlineCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedTab: FriendTab = FriendTab.ALL
) {
    val filteredFriends: List<Friend> 
        get() = when (selectedTab) {
            FriendTab.ALL -> friends
            FriendTab.ONLINE -> onlineFriends
            FriendTab.PENDING -> pendingReceived
        }.filter { 
            searchQuery.isEmpty() || it.friendName.contains(searchQuery, ignoreCase = true) 
        }
    
    val isEmpty: Boolean 
        get() = filteredFriends.isEmpty() && !isLoading
}

/**
 * Tab options for friend list
 */
enum class FriendTab {
    ALL, ONLINE, PENDING
}

/**
 * One-time actions for Friend List
 */
sealed class FriendAction {
    data class ShowToast(val message: String) : FriendAction()
    data class ShowFriendRequestDialog(val event: FriendEvent.RequestReceived) : FriendAction()
    data class ShowMatchInviteDialog(val event: MatchInviteEvent.InviteReceived) : FriendAction()
    data class NavigateToMatch(val matchId: String) : FriendAction()
    data class NavigateToChat(val friendId: String, val friendName: String) : FriendAction()
}

/**
 * ViewModel for Friend List Screen
 * Handles friend data and WebSocket communication
 */
class FriendListViewModel(application: Application) : AndroidViewModel(application) {
    
    // ===== Dependencies =====
    private val database = QuizBattleDatabase.getDatabase(application)
    private val webSocketManager = WebSocketManager.getInstance()
    private val friendRepository = FriendRepository(database.friendDao(), webSocketManager)
    private val tokenRepository = TokenRepository(application)
    
    // ===== State =====
    private val _state = MutableStateFlow(FriendListState())
    val state: StateFlow<FriendListState> = _state.asStateFlow()
    
    // ===== Events =====
    private val _action = MutableSharedFlow<FriendAction>()
    val action: SharedFlow<FriendAction> = _action.asSharedFlow()
    
    // ===== Current User =====
    private var currentUserId: String = ""
    
    init {
        initializeUser()
        observeFriends()
        observeEvents()
    }
    
    // ===== Public Methods =====
    
    fun setCurrentUser(userId: String) {
        currentUserId = userId
        refreshFriendList()
    }
    
    fun refreshFriendList() {
        if (!hasUser) return
        
        setLoading(true)
        friendRepository.requestFriendList(currentUserId)
        friendRepository.requestPendingRequests(currentUserId, INCOMING_REQUESTS)
    }
    
    fun sendFriendRequest(targetUserId: String, message: String? = null) {
        if (!hasUser) return
        friendRepository.sendFriendRequest(currentUserId, targetUserId, message)
    }
    
    fun acceptFriendRequest(requestId: String) {
        if (!hasUser) return
        friendRepository.respondToFriendRequest(currentUserId, requestId, true)
    }
    
    fun rejectFriendRequest(requestId: String) {
        if (!hasUser) return
        friendRepository.respondToFriendRequest(currentUserId, requestId, false)
    }
    
    fun removeFriend(friendId: String) {
        if (!hasUser) return
        friendRepository.removeFriend(currentUserId, friendId)
    }
    
    fun sendMatchInvite(friendId: String, message: String? = null) {
        if (!hasUser) return
        friendRepository.sendMatchInvite(
            senderId = currentUserId,
            receiverId = friendId,
            message = message
        )
    }
    
    fun acceptMatchInvite(inviteId: String) {
        if (!hasUser) return
        friendRepository.respondToMatchInvite(currentUserId, inviteId, true)
    }
    
    fun rejectMatchInvite(inviteId: String) {
        if (!hasUser) return
        friendRepository.respondToMatchInvite(currentUserId, inviteId, false)
    }
    
    fun openChat(friend: Friend) {
        emitAction(FriendAction.NavigateToChat(friend.friendId, friend.friendName))
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
    
    fun setSelectedTab(tab: FriendTab) {
        _state.update { it.copy(selectedTab = tab) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    // ===== Private Methods =====
    
    private val hasUser: Boolean get() = currentUserId.isNotEmpty()
    
    private fun initializeUser() {
        viewModelScope.launch {
            currentUserId = tokenRepository.getUserId() ?: ""
            if (hasUser) refreshFriendList()
        }
    }
    
    private fun observeFriends() {
        collectFlow(friendRepository.getAllFriends()) { friends ->
            _state.update { it.copy(friends = friends) }
        }
        
        collectFlow(friendRepository.getPendingReceivedRequests()) { pending ->
            _state.update { it.copy(pendingReceived = pending) }
        }
        
        collectFlow(friendRepository.getPendingSentRequests()) { pending ->
            _state.update { it.copy(pendingSent = pending) }
        }
        
        collectFlow(friendRepository.getOnlineFriends()) { online ->
            _state.update { it.copy(onlineFriends = online) }
        }
        
        collectFlow(friendRepository.getPendingRequestCount()) { count ->
            _state.update { it.copy(pendingCount = count) }
        }
        
        collectFlow(friendRepository.getOnlineFriendsCount()) { count ->
            _state.update { it.copy(onlineCount = count) }
        }
    }
    
    private fun observeEvents() {
        viewModelScope.launch {
            friendRepository.friendRequestEvent.filterNotNull().collect { event ->
                handleFriendEvent(event)
            }
        }
        
        viewModelScope.launch {
            friendRepository.matchInviteEvent.filterNotNull().collect { event ->
                handleMatchInviteEvent(event)
            }
        }
    }
    
    private suspend fun handleFriendEvent(event: FriendEvent) {
        when (event) {
            is FriendEvent.RequestReceived -> handleRequestReceived(event)
            is FriendEvent.RequestAccepted -> handleRequestAccepted(event)
            is FriendEvent.RequestRejected -> handleRequestRejected(event)
            is FriendEvent.FriendRemoved -> handleFriendRemoved(event)
            is FriendEvent.RequestSent -> { /* Request was sent successfully */ }
            is FriendEvent.FriendListData -> { /* Friend list data received */ }
            is FriendEvent.ChallengeSent -> { /* Challenge sent */ }
            is FriendEvent.Unknown -> { /* Unknown event */ }
        }
        friendRepository.clearFriendEvent()
    }
    
    private suspend fun handleRequestReceived(event: FriendEvent.RequestReceived) {
        val friend = Friend(
            id = event.requestId,
            friendId = event.senderId,
            friendName = event.senderName,
            friendAvatarUrl = event.senderAvatarUrl,
            points = event.senderPoints,
            status = FriendStatus.PENDING_RECEIVED
        )
        friendRepository.saveFriend(friend)
        _action.emit(FriendAction.ShowFriendRequestDialog(event))
    }
    
    private suspend fun handleRequestAccepted(event: FriendEvent.RequestAccepted) {
        friendRepository.updateFriendStatus(event.requestId, FriendStatus.ACCEPTED)
        showToast("${event.friendName ?: "User"} menerima permintaan pertemanan")
    }
    
    private suspend fun handleRequestRejected(event: FriendEvent.RequestRejected) {
        friendRepository.deleteFriendById(event.requestId)
        showToast("Permintaan pertemanan ditolak")
    }
    
    private suspend fun handleFriendRemoved(event: FriendEvent.FriendRemoved) {
        friendRepository.deleteFriendById(event.friendId)
        showToast("Teman telah dihapus")
    }
    
    private suspend fun handleMatchInviteEvent(event: MatchInviteEvent) {
        when (event) {
            is MatchInviteEvent.InviteReceived -> {
                _action.emit(FriendAction.ShowMatchInviteDialog(event))
            }
            is MatchInviteEvent.InviteAccepted -> {
                showToast("Undangan diterima! Pertandingan dimulai...")
                _action.emit(FriendAction.NavigateToMatch(event.matchId))
            }
            is MatchInviteEvent.InviteRejected -> {
                showToast("Undangan ditolak")
            }
            is MatchInviteEvent.InviteExpired -> {
                showToast("Undangan telah kadaluarsa")
            }
        }
        friendRepository.clearMatchInviteEvent()
    }
    
    // ===== Utility Methods =====
    
    private fun setLoading(loading: Boolean) {
        _state.update { it.copy(isLoading = loading) }
    }
    
    private fun showToast(message: String) {
        emitAction(FriendAction.ShowToast(message))
    }
    
    private fun emitAction(action: FriendAction) {
        viewModelScope.launch { _action.emit(action) }
    }
    
    private fun <T> collectFlow(
        flow: kotlinx.coroutines.flow.Flow<T>,
        collector: suspend (T) -> Unit
    ) {
        viewModelScope.launch { flow.collect(collector) }
    }
    
    companion object {
        private const val INCOMING_REQUESTS = "incoming"
    }
}
