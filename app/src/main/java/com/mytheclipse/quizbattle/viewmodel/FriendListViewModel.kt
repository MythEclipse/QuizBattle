package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.local.entity.FriendStatus
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.data.repository.FriendEvent
import com.mytheclipse.quizbattle.data.repository.FriendRepository
import com.mytheclipse.quizbattle.data.repository.MatchInviteEvent
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
)

enum class FriendTab {
    ALL, ONLINE, PENDING
}

sealed class FriendAction {
    data class ShowToast(val message: String) : FriendAction()
    data class ShowFriendRequestDialog(val event: FriendEvent.RequestReceived) : FriendAction()
    data class ShowMatchInviteDialog(val event: MatchInviteEvent.InviteReceived) : FriendAction()
    data class NavigateToMatch(val matchId: String) : FriendAction()
    data class NavigateToChat(val friendId: String, val friendName: String) : FriendAction()
}

class FriendListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = QuizBattleDatabase.getDatabase(application)
    private val webSocketManager = WebSocketManager.getInstance()
    private val friendRepository = FriendRepository(database.friendDao(), webSocketManager)
    private val tokenRepository = TokenRepository(application)
    
    private val _state = MutableStateFlow(FriendListState())
    val state: StateFlow<FriendListState> = _state.asStateFlow()
    
    private val _action = MutableSharedFlow<FriendAction>()
    val action: SharedFlow<FriendAction> = _action.asSharedFlow()
    
    private var currentUserId: String = ""
    
    init {
        observeFriends()
        observeEvents()
        initializeUser()
    }
    
    private fun initializeUser() {
        viewModelScope.launch {
            currentUserId = tokenRepository.getUserId() ?: ""
            if (currentUserId.isNotEmpty()) {
                refreshFriendList()
            }
        }
    }
    
    fun setCurrentUser(userId: String) {
        currentUserId = userId
        refreshFriendList()
    }
    
    private fun observeFriends() {
        viewModelScope.launch {
            friendRepository.getAllFriends().collect { friends ->
                _state.update { it.copy(friends = friends) }
            }
        }
        
        viewModelScope.launch {
            friendRepository.getPendingReceivedRequests().collect { pending ->
                _state.update { it.copy(pendingReceived = pending) }
            }
        }
        
        viewModelScope.launch {
            friendRepository.getPendingSentRequests().collect { pending ->
                _state.update { it.copy(pendingSent = pending) }
            }
        }
        
        viewModelScope.launch {
            friendRepository.getOnlineFriends().collect { online ->
                _state.update { it.copy(onlineFriends = online) }
            }
        }
        
        viewModelScope.launch {
            friendRepository.getPendingRequestCount().collect { count ->
                _state.update { it.copy(pendingCount = count) }
            }
        }
        
        viewModelScope.launch {
            friendRepository.getOnlineFriendsCount().collect { count ->
                _state.update { it.copy(onlineCount = count) }
            }
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
            is FriendEvent.RequestReceived -> {
                // Save to local database
                val friend = Friend(
                    id = event.requestId,
                    friendId = event.senderId,
                    friendName = event.senderName,
                    friendAvatarUrl = event.senderAvatarUrl,
                    points = event.senderPoints,
                    status = FriendStatus.PENDING_RECEIVED
                )
                friendRepository.saveFriend(friend)
                
                // Show dialog
                _action.emit(FriendAction.ShowFriendRequestDialog(event))
            }
            is FriendEvent.RequestAccepted -> {
                // Update local database
                friendRepository.updateFriendStatus(event.requestId, FriendStatus.ACCEPTED)
                _action.emit(FriendAction.ShowToast("${event.friendName ?: "User"} menerima permintaan pertemanan"))
            }
            is FriendEvent.RequestRejected -> {
                friendRepository.deleteFriendById(event.requestId)
                _action.emit(FriendAction.ShowToast("Permintaan pertemanan ditolak"))
            }
            is FriendEvent.FriendRemoved -> {
                friendRepository.deleteFriendById(event.friendId)
                _action.emit(FriendAction.ShowToast("Teman telah dihapus"))
            }
        }
        friendRepository.clearFriendEvent()
    }
    
    private suspend fun handleMatchInviteEvent(event: MatchInviteEvent) {
        when (event) {
            is MatchInviteEvent.InviteReceived -> {
                _action.emit(FriendAction.ShowMatchInviteDialog(event))
            }
            is MatchInviteEvent.InviteAccepted -> {
                _action.emit(FriendAction.ShowToast("Undangan diterima! Pertandingan dimulai..."))
                _action.emit(FriendAction.NavigateToMatch(event.matchId))
            }
            is MatchInviteEvent.InviteRejected -> {
                _action.emit(FriendAction.ShowToast("Undangan ditolak"))
            }
            is MatchInviteEvent.InviteExpired -> {
                _action.emit(FriendAction.ShowToast("Undangan telah kadaluarsa"))
            }
        }
        friendRepository.clearMatchInviteEvent()
    }
    
    fun refreshFriendList() {
        if (currentUserId.isEmpty()) return
        
        _state.update { it.copy(isLoading = true) }
        friendRepository.requestFriendList(currentUserId)
        friendRepository.requestPendingRequests(currentUserId, "incoming")
    }
    
    fun sendFriendRequest(targetUserId: String, message: String? = null) {
        if (currentUserId.isEmpty()) return
        friendRepository.sendFriendRequest(currentUserId, targetUserId, message)
    }
    
    fun acceptFriendRequest(requestId: String) {
        if (currentUserId.isEmpty()) return
        friendRepository.respondToFriendRequest(currentUserId, requestId, true)
    }
    
    fun rejectFriendRequest(requestId: String) {
        if (currentUserId.isEmpty()) return
        friendRepository.respondToFriendRequest(currentUserId, requestId, false)
    }
    
    fun removeFriend(friendId: String) {
        if (currentUserId.isEmpty()) return
        friendRepository.removeFriend(currentUserId, friendId)
    }
    
    fun sendMatchInvite(friendId: String, message: String? = null) {
        if (currentUserId.isEmpty()) return
        friendRepository.sendMatchInvite(
            senderId = currentUserId,
            receiverId = friendId,
            message = message
        )
    }
    
    fun acceptMatchInvite(inviteId: String) {
        if (currentUserId.isEmpty()) return
        friendRepository.respondToMatchInvite(currentUserId, inviteId, true)
    }
    
    fun rejectMatchInvite(inviteId: String) {
        if (currentUserId.isEmpty()) return
        friendRepository.respondToMatchInvite(currentUserId, inviteId, false)
    }
    
    fun openChat(friend: Friend) {
        viewModelScope.launch {
            _action.emit(FriendAction.NavigateToChat(friend.friendId, friend.friendName))
        }
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
}
