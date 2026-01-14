package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.model.FriendEvent
import com.mytheclipse.quizbattle.data.model.MatchInviteEvent
import com.mytheclipse.quizbattle.databinding.ActivityFriendListBinding
import com.mytheclipse.quizbattle.ui.FriendAdapter
import com.mytheclipse.quizbattle.ui.FriendRequestAdapter
import com.mytheclipse.quizbattle.viewmodel.FriendAction
import com.mytheclipse.quizbattle.viewmodel.FriendListState
import com.mytheclipse.quizbattle.viewmodel.FriendListViewModel
import com.mytheclipse.quizbattle.viewmodel.FriendTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Friend list screen with tabs for all friends, online friends, and pending requests
 * Supports adding friends, chat, and match invites
 */
class FriendListActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityFriendListBinding
    private val viewModel: FriendListViewModel by viewModels()
    
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var requestAdapter: FriendRequestAdapter
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupToolbar()
        setupTabs()
        setupRecyclerViews()
        setupClickListeners()
        observeState()
        observeActions()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { navigateBack() }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setSelectedTab(getTabForPosition(tab.position))
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun getTabForPosition(position: Int): FriendTab = when (position) {
        0 -> FriendTab.ALL
        1 -> FriendTab.ONLINE
        2 -> FriendTab.PENDING
        else -> FriendTab.ALL
    }
    
    private fun setupRecyclerViews() {
        friendAdapter = FriendAdapter(
            onChatClick = { friend -> viewModel.openChat(friend) },
            onInviteClick = { friend -> showInviteDialog(friend) },
            onRemoveClick = { friend -> showRemoveFriendDialog(friend) }
        )
        
        requestAdapter = FriendRequestAdapter(
            onAcceptClick = { friend -> viewModel.acceptFriendRequest(friend.id) },
            onRejectClick = { friend -> viewModel.rejectFriendRequest(friend.id) }
        )
        
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(this@FriendListActivity)
            adapter = friendAdapter
        }
        
        binding.rvPendingRequests.apply {
            layoutManager = LinearLayoutManager(this@FriendListActivity)
            adapter = requestAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddFriend.setOnClickListener { withDebounce { showAddFriendDialog() } }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refreshFriendList() }
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: FriendListState) {
        updateLoadingState(state.isLoading)
        updateConnectionState(state.isConnected)
        updateTabBadges(state)
        updateListDisplay(state)
        handleError(state.error)
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.swipeRefresh.isRefreshing = isLoading
    }
    
    private fun updateConnectionState(isConnected: Boolean) {
        // Show offline indicator when not connected
        binding.tvEmpty.apply {
            if (!isConnected && viewModel.state.value.friends.isEmpty()) {
                isVisible = true
                text = getString(R.string.friend_feature_requires_internet)
            }
        }
        
        // Disable add friend button when offline
        binding.fabAddFriend.isEnabled = isConnected
        binding.fabAddFriend.alpha = if (isConnected) 1f else 0.5f
    }
    
    private fun updateTabBadges(state: FriendListState) {
        binding.tabLayout.getTabAt(TAB_ONLINE)?.text = getString(R.string.online_with_count, state.onlineCount)
        binding.tabLayout.getTabAt(TAB_PENDING)?.apply {
            text = getString(R.string.requests)
            if (state.pendingCount > 0) {
                orCreateBadge.number = state.pendingCount
            } else {
                removeBadge()
            }
        }
    }
    
    private fun updateListDisplay(state: FriendListState) {
        when (state.selectedTab) {
            FriendTab.ALL -> showFriendsList(state.friends, getString(R.string.no_friends_message))
            FriendTab.ONLINE -> showFriendsList(state.onlineFriends, getString(R.string.no_online_friends))
            FriendTab.PENDING -> showPendingRequests(state.pendingReceived)
        }
    }
    
    private fun showFriendsList(friends: List<Friend>, emptyMessage: String) {
        with(binding) {
            rvFriends.isVisible = true
            rvPendingRequests.isVisible = false
            friendAdapter.submitList(friends)
            
            tvEmpty.isVisible = friends.isEmpty()
            tvEmpty.text = emptyMessage
        }
    }
    
    private fun showPendingRequests(requests: List<Friend>) {
        with(binding) {
            rvFriends.isVisible = false
            rvPendingRequests.isVisible = true
            requestAdapter.submitList(requests)
            
            tvEmpty.isVisible = requests.isEmpty()
            tvEmpty.text = getString(R.string.no_friend_requests)
        }
    }
    
    private fun handleError(error: String?) {
        error?.let {
            showToast(it)
            viewModel.clearError()
        }
    }
    
    // endregion
    
    // region Actions Observation
    
    private fun observeActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.action.collect { action ->
                    handleAction(action)
                }
            }
        }
    }
    
    private fun handleAction(action: FriendAction) {
        when (action) {
            is FriendAction.ShowToast -> showToast(action.message)
            is FriendAction.ShowFriendRequestDialog -> showFriendRequestReceivedDialog(action.event)
            is FriendAction.ShowMatchInviteDialog -> showMatchInviteDialog(action.event)
            is FriendAction.NavigateToMatch -> navigateToMatch(action.matchId)
            is FriendAction.NavigateToChat -> navigateToChat(action.friendId, action.friendName)
        }
    }
    
    // endregion
    
    // region Navigation
    
    private fun navigateToMatch(matchId: String) {
        startActivity(Intent(this, OnlineBattleActivity::class.java).apply {
            putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
        })
    }
    
    private fun navigateToChat(friendId: String, friendName: String) {
        startActivity(Intent(this, ChatRoomActivity::class.java).apply {
            putExtra(ChatRoomActivity.EXTRA_FRIEND_ID, friendId)
            putExtra(ChatRoomActivity.EXTRA_FRIEND_NAME, friendName)
            putExtra(ChatRoomActivity.EXTRA_IS_PRIVATE, true)
        })
    }
    
    // endregion
    
    // region Dialogs
    
    private fun showAddFriendDialog() {
        val materialContext = ContextThemeWrapper(this, R.style.Theme_QuizBattle_Dialog)
        val dialogView = LayoutInflater.from(materialContext).inflate(R.layout.dialog_add_friend, null)
        val etUserId = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserId)
        val etMessage = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMessage)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_friend)
            .setView(dialogView)
            .setPositiveButton(R.string.send) { _, _ ->
                val userId = etUserId.text?.toString()?.trim()
                val message = etMessage.text?.toString()?.trim()
                
                if (!userId.isNullOrEmpty()) {
                    viewModel.sendFriendRequest(userId, message)
                    showToast(getString(R.string.friend_request_sent))
                } else {
                    showToast(getString(R.string.enter_user_id))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showInviteDialog(friend: Friend) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.invite_to_battle)
            .setMessage(getString(R.string.invite_message, friend.friendName))
            .setPositiveButton(R.string.invite) { _, _ ->
                viewModel.sendMatchInvite(friend.friendId)
                showToast(getString(R.string.invite_sent, friend.friendName))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showRemoveFriendDialog(friend: Friend) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.remove_friend)
            .setMessage(getString(R.string.remove_friend_confirm, friend.friendName))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.removeFriend(friend.friendId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showFriendRequestReceivedDialog(event: FriendEvent.RequestReceived) {
        val message = buildFriendRequestMessage(event)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.friend_request)
            .setMessage(message)
            .setPositiveButton(R.string.accept) { _, _ ->
                viewModel.acceptFriendRequest(event.requestId)
            }
            .setNegativeButton(R.string.reject) { _, _ ->
                viewModel.rejectFriendRequest(event.requestId)
            }
            .setNeutralButton(R.string.later, null)
            .show()
    }
    
    private fun buildFriendRequestMessage(event: FriendEvent.RequestReceived): String {
        return buildString {
            append("${event.senderName} ${getString(R.string.wants_to_be_friends)}")
            if (event.senderPoints > 0) {
                append("\n${getString(R.string.points_format, event.senderPoints)}")
            }
            if (!event.message.isNullOrEmpty()) {
                append("\n\n${getString(R.string.message_label)}: ${event.message}")
            }
        }
    }
    
    private fun showMatchInviteDialog(event: MatchInviteEvent.InviteReceived) {
        val message = buildMatchInviteMessage(event)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.battle_invite)
            .setMessage(message)
            .setPositiveButton(R.string.accept) { _, _ ->
                viewModel.acceptMatchInvite(event.inviteId)
            }
            .setNegativeButton(R.string.reject) { _, _ ->
                viewModel.rejectMatchInvite(event.inviteId)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun buildMatchInviteMessage(event: MatchInviteEvent.InviteReceived): String {
        return buildString {
            append("${event.senderName} ${getString(R.string.invites_you_to_battle)}")
            append("\n\n${getString(R.string.points_format, event.senderPoints)}")
            append("\n${getString(R.string.wins_count, event.senderWins)}")
            append("\n\n${getString(R.string.settings)}:")
            append("\n• ${getString(R.string.difficulty)}: ${event.difficulty}")
            append("\n• ${getString(R.string.category)}: ${event.category}")
            append("\n• ${getString(R.string.total_questions)}: ${event.totalQuestions}")
            append("\n• ${getString(R.string.time_per_question)}: ${event.timePerQuestion}s")
            if (!event.message.isNullOrEmpty()) {
                append("\n\n${getString(R.string.message_label)}: ${event.message}")
            }
        }
    }
    
    // endregion
    
    companion object {
        private const val TAB_ALL = 0
        private const val TAB_ONLINE = 1
        private const val TAB_PENDING = 2
    }
}
