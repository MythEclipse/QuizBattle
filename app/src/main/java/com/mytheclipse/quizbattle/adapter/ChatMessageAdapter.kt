package com.mytheclipse.quizbattle.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.repository.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for displaying chat messages.
 * 
 * Displays messages in a chat bubble style with different
 * appearance for sent and received messages.
 *
 * @param currentUserId The ID of the current user to differentiate message ownership
 */
class ChatMessageAdapter(
    private val currentUserId: String
) : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // region Adapter Implementation
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view, currentUserId)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    // endregion

    // region ViewHolder
    
    class MessageViewHolder(
        itemView: View,
        private val currentUserId: String
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        
        fun bind(message: ChatMessage) {
            val isOwnMessage = message.userId == currentUserId
            
            configureBubbleAppearance(isOwnMessage)
            bindMessageContent(message, isOwnMessage)
        }
        
        private fun configureBubbleAppearance(isOwnMessage: Boolean) {
            val params = messageContainer.layoutParams as LinearLayout.LayoutParams
            
            if (isOwnMessage) {
                params.gravity = Gravity.END
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_own)
            } else {
                params.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_other)
            }
            
            messageContainer.layoutParams = params
        }
        
        private fun bindMessageContent(message: ChatMessage, isOwnMessage: Boolean) {
            senderNameTextView.visibility = if (isOwnMessage) View.GONE else View.VISIBLE
            if (!isOwnMessage) {
                senderNameTextView.text = message.userName
            }
            
            messageTextView.text = message.message
            timeTextView.text = formatTime(message.createdAt)
        }
        
        private fun formatTime(timestamp: Long): String {
            return TIME_FORMAT.format(Date(timestamp))
        }
        
        companion object {
            private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        }
    }
    
    // endregion

    // region DiffCallback
    
    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
    
    // endregion
}
