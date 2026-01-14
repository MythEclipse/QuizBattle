package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.ChatMessageAdapter
import com.mytheclipse.quizbattle.databinding.ActivityChatRoomBinding
import com.mytheclipse.quizbattle.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatRoomActivity : BaseActivity() {
    
    private lateinit var binding: ActivityChatRoomBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter
    
    private var roomId: String = ""
    private var roomName: String = ""
    private var friendId: String? = null
    private var friendName: String? = null
    private var isPrivateChat: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        // Check if this is a private friend chat
        isPrivateChat = intent.getBooleanExtra("IS_PRIVATE", false)
        friendId = intent.getStringExtra("FRIEND_ID")
        friendName = intent.getStringExtra("FRIEND_NAME")
        
        if (isPrivateChat && friendId != null) {
            // Generate a consistent room ID for the private conversation
            roomName = friendName ?: "Chat"
        } else {
            roomId = intent.getStringExtra("ROOM_ID") ?: ""
            roomName = intent.getStringExtra("ROOM_NAME") ?: "Chat"
        }
        
        setupViews()
        setupListeners()
        observeState()
        
        if (isPrivateChat && friendId != null) {
            viewModel.loadPrivateChat(friendId!!)
        } else if (roomId.isNotEmpty()) {
            viewModel.loadRoomMessages(roomId)
        }
    }
    
    private fun setupViews() {
        binding.roomNameTextView.text = roomName
        
        adapter = ChatMessageAdapter(viewModel.state.value.currentUserId ?: "")
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatRoomActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatRoomActivity.adapter
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        
        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }
    
    private fun sendMessage() {
        val message = binding.messageEditText.text.toString().trim()
        if (message.isNotEmpty()) {
            if (isPrivateChat && friendId != null) {
                viewModel.sendPrivateMessage(friendId!!, message)
            } else if (roomId.isNotEmpty()) {
                viewModel.sendMessage(roomId, message)
            }
            binding.messageEditText.text?.clear()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading && state.messages.isEmpty()) View.VISIBLE else View.GONE
                
                if (state.messages.isEmpty() && !state.isLoading) {
                    binding.messagesRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.messagesRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    adapter.submitList(state.messages)
                    
                    // Scroll to bottom on new message
                    if (state.messages.isNotEmpty()) {
                        binding.messagesRecyclerView.scrollToPosition(state.messages.size - 1)
                    }
                }
                
                // Show typing indicator
                if (state.typingUsers.isNotEmpty()) {
                    binding.typingIndicator.visibility = View.VISIBLE
                    binding.typingIndicator.text = "${state.typingUsers.joinToString(", ")} is typing..."
                } else {
                    binding.typingIndicator.visibility = View.GONE
                }
            }
        }
    }
}
