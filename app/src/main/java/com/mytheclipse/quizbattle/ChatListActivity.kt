package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.ChatRoomAdapter
import com.mytheclipse.quizbattle.databinding.ActivityChatListBinding
import com.mytheclipse.quizbattle.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatListActivity : BaseActivity() {
    
    private lateinit var binding: ActivityChatListBinding
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatRoomAdapter { room ->
        // Navigate to chat room
        val intent = android.content.Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("ROOM_ID", room.roomId)
        intent.putExtra("ROOM_NAME", room.roomName)
        startActivity(intent)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupViews()
        setupListeners()
        observeState()
    }
    
    private fun setupViews() {
        binding.chatRoomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            adapter = this@ChatListActivity.adapter
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadChatRooms()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.swipeRefreshLayout.isRefreshing = state.isLoading
                binding.progressBar.visibility = if (state.isLoading && state.rooms.isEmpty()) View.VISIBLE else View.GONE
                
                if (state.rooms.isEmpty() && !state.isLoading) {
                    binding.chatRoomsRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.chatRoomsRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    adapter.submitList(state.rooms)
                }
                
                state.error?.let { error ->
                    android.widget.Toast.makeText(this@ChatListActivity, error, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
