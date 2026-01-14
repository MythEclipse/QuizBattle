package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.NotificationAdapter
import com.mytheclipse.quizbattle.data.repository.DataModels.NotificationInfo
import com.mytheclipse.quizbattle.databinding.ActivityNotificationBinding
import com.mytheclipse.quizbattle.viewmodel.NotificationState
import com.mytheclipse.quizbattle.viewmodel.NotificationViewModel

/**
 * Activity for displaying user notifications.
 * 
 * Shows a list of notifications with support for:
 * - Mark as read on click
 * - Swipe to delete
 * - Mark all as read
 * - Different notification types (battle invite, friend request, etc.)
 */
class NotificationActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    
    private val adapter = NotificationAdapter(
        onNotificationClick = { notification ->
            handleNotificationClick(notification)
        },
        onDeleteClick = { notification ->
            viewModel.deleteNotification(notification.notificationId)
        }
    )
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        setupSwipeRefresh()
        observeState()
    }
    
    // endregion

    // region Setup
    
    private fun setupRecyclerView() {
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = this@NotificationActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            navigateBack()
        }
        
        binding.markAllReadButton.setOnClickListener {
            withDebounce { viewModel.markAllAsRead() }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeState() {
        collectState(viewModel.state) { state ->
            handleState(state)
        }
    }
    
    private fun handleState(state: NotificationState) {
        updateLoadingState(state)
        updateUnreadBadge(state.unreadCount)
        updateNotificationsList(state)
        handleError(state.error)
    }
    
    private fun updateLoadingState(state: NotificationState) {
        binding.swipeRefreshLayout.isRefreshing = state.isLoading
        
        val showInitialLoading = state.isLoading && state.notifications.isEmpty()
        binding.progressBar.visibility = if (showInitialLoading) View.VISIBLE else View.GONE
    }
    
    private fun updateUnreadBadge(unreadCount: Int) {
        val hasUnread = unreadCount > 0
        
        binding.unreadBadge.visibility = if (hasUnread) View.VISIBLE else View.GONE
        binding.markAllReadButton.visibility = if (hasUnread) View.VISIBLE else View.GONE
        
        if (hasUnread) {
            binding.unreadBadge.text = formatUnreadCount(unreadCount)
        }
    }
    
    private fun formatUnreadCount(count: Int): String {
        return if (count > MAX_BADGE_COUNT) MAX_BADGE_TEXT else count.toString()
    }
    
    private fun updateNotificationsList(state: NotificationState) {
        val isEmpty = state.notifications.isEmpty() && !state.isLoading
        
        binding.notificationsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        if (!isEmpty) {
            adapter.submitList(state.notifications)
        }
    }
    
    private fun handleError(error: String?) {
        error?.let { showToast(it) }
    }
    
    // endregion

    // region Notification Actions
    
    private fun handleNotificationClick(notification: NotificationInfo) {
        if (!notification.isRead) {
            viewModel.markAsRead(notification.notificationId)
        }
        navigateBasedOnType(notification.type)
    }
    
    private fun navigateBasedOnType(type: String) {
        when (type) {
            TYPE_BATTLE_INVITE -> handleBattleInvite()
            TYPE_FRIEND_REQUEST -> navigateTo<FriendListActivity>()
            else -> { /* No action for unknown types */ }
        }
    }
    
    private fun handleBattleInvite() {
        // Navigate to battle - can be extended later
    }
    
    // endregion

    companion object {
        private const val TYPE_BATTLE_INVITE = "battle_invite"
        private const val TYPE_FRIEND_REQUEST = "friend_request"
        
        private const val MAX_BADGE_COUNT = 99
        private const val MAX_BADGE_TEXT = "99+"
    }
}
