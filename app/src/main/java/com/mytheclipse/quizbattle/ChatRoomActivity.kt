package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.ChatMessageAdapter
import com.mytheclipse.quizbattle.databinding.ActivityChatRoomBinding
import com.mytheclipse.quizbattle.viewmodel.ChatState
import com.mytheclipse.quizbattle.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

/**
 * Chat room screen with message list, input field, and typing indicator
 * Supports both group and private chats
 */
class ChatRoomActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityChatRoomBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter
    
    private var roomId: String = ""
    private var roomName: String = ""
    private var friendId: String? = null
    private var friendName: String? = null
    private val isPrivateChat: Boolean get() = friendId != null
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        extractIntentExtras()
        setupViews()
        setupClickListeners()
        observeState()
        loadMessages()
    }
    
    // endregion
    
    // region Setup
    
    private fun extractIntentExtras() {
        val isPrivate = intent.getBooleanExtra(EXTRA_IS_PRIVATE, false)
        friendId = intent.getStringExtra(EXTRA_FRIEND_ID)
        friendName = intent.getStringExtra(EXTRA_FRIEND_NAME)
        
        if (isPrivate && friendId != null) {
            roomName = friendName ?: DEFAULT_ROOM_NAME
        } else {
            roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: ""
            roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: DEFAULT_ROOM_NAME
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
    
    private fun setupClickListeners() {
        with(binding) {
            backButton.setOnClickListener { navigateBack() }
            sendButton.setOnClickListener { withDebounce { sendMessage() } }
            
            messageEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    true
                } else false
            }
        }
    }
    
    private fun loadMessages() {
        when {
            isPrivateChat && friendId != null -> viewModel.loadPrivateChat(friendId!!)
            roomId.isNotEmpty() -> viewModel.loadRoomMessages(roomId)
        }
    }
    
    // endregion
    
    // region Message Handling
    
    private fun sendMessage() {
        val message = binding.messageEditText.text.toString().trim()
        if (message.isEmpty()) return
        
        when {
            isPrivateChat && friendId != null -> viewModel.sendPrivateMessage(friendId!!, message)
            roomId.isNotEmpty() -> viewModel.sendMessage(roomId, message)
        }
        
        binding.messageEditText.text?.clear()
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
        updateMessagesList(state)
        updateTypingIndicator(state)
    }
    
    private fun updateLoadingState(state: ChatState) {
        binding.progressBar.isVisible = state.isLoading && state.messages.isEmpty()
    }
    
    private fun updateMessagesList(state: ChatState) {
        val hasMessages = state.messages.isNotEmpty()
        
        with(binding) {
            messagesRecyclerView.isVisible = hasMessages
            emptyStateLayout.isVisible = !hasMessages && !state.isLoading
        }
        
        if (hasMessages) {
            adapter.submitList(state.messages)
            scrollToBottom()
        }
    }
    
    private fun scrollToBottom() {
        val messageCount = adapter.itemCount
        if (messageCount > 0) {
            binding.messagesRecyclerView.scrollToPosition(messageCount - 1)
        }
    }
    
    private fun updateTypingIndicator(state: ChatState) {
        with(binding.typingIndicator) {
            if (state.typingUsers.isNotEmpty()) {
                isVisible = true
                text = getString(R.string.typing_indicator, state.typingUsers.joinToString(", "))
            } else {
                isVisible = false
            }
        }
    }
    
    // endregion
    
    companion object {
        const val EXTRA_ROOM_ID = "ROOM_ID"
        const val EXTRA_ROOM_NAME = "ROOM_NAME"
        const val EXTRA_IS_PRIVATE = "IS_PRIVATE"
        const val EXTRA_FRIEND_ID = "FRIEND_ID"
        const val EXTRA_FRIEND_NAME = "FRIEND_NAME"
        
        private const val DEFAULT_ROOM_NAME = "Chat"
    }
}
