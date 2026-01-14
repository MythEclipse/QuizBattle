package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.ChatRoomAdapter
import com.mytheclipse.quizbattle.data.model.ChatRoom
import com.mytheclipse.quizbattle.databinding.ActivityChatListBinding
import com.mytheclipse.quizbattle.viewmodel.ChatState
import com.mytheclipse.quizbattle.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

/**
 * Displays list of chat rooms
 * Supports pull-to-refresh and navigation to individual chat rooms
 */
class ChatListActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityChatListBinding
    private val viewModel: ChatViewModel by viewModels()
    
    private val adapter = ChatRoomAdapter { room -> 
        navigateToChatRoom(room) 
    }
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        observeState()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupRecyclerView() {
        binding.chatRoomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            adapter = this@ChatListActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        with(binding) {
            backButton.setOnClickListener { navigateBack() }
            
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.loadChatRooms()
            }
        }
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: ChatState) {
        updateLoadingState(state)
        updateRoomsList(state)
        handleError(state.error)
    }
    
    private fun updateLoadingState(state: ChatState) {
        with(binding) {
            swipeRefreshLayout.isRefreshing = state.isLoading
            progressBar.isVisible = state.isLoading && state.rooms.isEmpty()
        }
    }
    
    private fun updateRoomsList(state: ChatState) {
        val hasRooms = state.rooms.isNotEmpty()
        
        with(binding) {
            chatRoomsRecyclerView.isVisible = hasRooms
            emptyStateLayout.isVisible = !hasRooms && !state.isLoading
        }
        
        if (hasRooms) {
            adapter.submitList(state.rooms)
        }
    }
    
    private fun handleError(error: String?) {
        error?.let { showToast(it) }
    }
    
    // endregion
    
    // region Navigation
    
    private fun navigateToChatRoom(room: ChatRoom) {
        startActivity(Intent(this, ChatRoomActivity::class.java).apply {
            putExtra(ChatRoomActivity.EXTRA_ROOM_ID, room.roomId)
            putExtra(ChatRoomActivity.EXTRA_ROOM_NAME, room.roomName)
        })
    }
    
    // endregion
}
