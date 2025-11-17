package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.OnlineFriendsRepository
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.data.repository.FriendEvent
import com.mytheclipse.quizbattle.data.repository.FriendInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendListState(
    val friends: List<FriendInfo> = emptyList(),
    val pendingCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FriendListViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenRepository = TokenRepository(application)
    private val friendsRepository = OnlineFriendsRepository()

    private val _state = MutableStateFlow(FriendListState())
    val state: StateFlow<FriendListState> = _state.asStateFlow()

    init {
        refresh()
        observeFriendEvents()
    }

    private fun observeFriendEvents() {
        viewModelScope.launch {
            friendsRepository.observeFriendEvents().collect { event ->
                when (event) {
                    is FriendEvent.FriendListData -> {
                        _state.value = _state.value.copy(
                            friends = event.friends,
                            pendingCount = event.pendingRequests.size,
                            isLoading = false,
                            error = null
                        )
                    }
                    is FriendEvent.RequestAccepted,
                    is FriendEvent.RequestRejected,
                    is FriendEvent.FriendRemoved,
                    is FriendEvent.RequestSent -> {
                        // trigger refresh to keep list in sync
                        refresh()
                    }
                    else -> {}
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val userId = tokenRepository.getUserId()
            if (userId.isNullOrBlank()) {
                _state.value = _state.value.copy(isLoading = false, error = "User belum login")
                return@launch
            }
            friendsRepository.requestFriendList(userId)
        }
    }
}
