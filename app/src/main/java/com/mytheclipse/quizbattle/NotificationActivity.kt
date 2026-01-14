package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.NotificationAdapter
import com.mytheclipse.quizbattle.databinding.ActivityNotificationBinding
import com.mytheclipse.quizbattle.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

class NotificationActivity : BaseActivity() {
    
    private lateinit var binding: ActivityNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private val adapter = NotificationAdapter(
        onNotificationClick = { notification ->
            if (!notification.isRead) {
                viewModel.markAsRead(notification.notificationId)
            }
            handleNotificationAction(notification)
        },
        onDeleteClick = { notification ->
            viewModel.deleteNotification(notification.notificationId)
        }
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupViews()
        setupListeners()
        observeState()
    }
    
    private fun setupViews() {
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = this@NotificationActivity.adapter
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.markAllReadButton.setOnClickListener {
            viewModel.markAllAsRead()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.swipeRefreshLayout.isRefreshing = state.isLoading
                binding.progressBar.visibility = if (state.isLoading && state.notifications.isEmpty()) View.VISIBLE else View.GONE
                
                // Update badge
                if (state.unreadCount > 0) {
                    binding.unreadBadge.visibility = View.VISIBLE
                    binding.unreadBadge.text = if (state.unreadCount > 99) "99+" else state.unreadCount.toString()
                    binding.markAllReadButton.visibility = View.VISIBLE
                } else {
                    binding.unreadBadge.visibility = View.GONE
                    binding.markAllReadButton.visibility = View.GONE
                }
                
                // Notifications list
                if (state.notifications.isEmpty() && !state.isLoading) {
                    binding.notificationsRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.notificationsRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    adapter.submitList(state.notifications)
                }
                
                state.error?.let { error ->
                    android.widget.Toast.makeText(this@NotificationActivity, error, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun handleNotificationAction(notification: com.mytheclipse.quizbattle.data.repository.DataModels.NotificationInfo) {
        // Handle different notification types
        when (notification.type) {
            "battle_invite" -> {
                // Navigate to battle
            }
            "friend_request" -> {
                // Navigate to friends
                startActivity(android.content.Intent(this, FriendListActivity::class.java))
            }
            else -> {
                // Default action
            }
        }
    }
}
