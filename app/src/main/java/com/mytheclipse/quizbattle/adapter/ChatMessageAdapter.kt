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

class ChatMessageAdapter(
    private val currentUserId: String
) : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view, currentUserId)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        itemView: View,
        private val currentUserId: String
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        fun bind(message: ChatMessage) {
            val isOwnMessage = message.userId == currentUserId
            
            // Adjust gravity based on message ownership
            val params = messageContainer.layoutParams as LinearLayout.LayoutParams
            if (isOwnMessage) {
                params.gravity = Gravity.END
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_own)
                senderNameTextView.visibility = View.GONE
            } else {
                params.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_other)
                senderNameTextView.visibility = View.VISIBLE
                senderNameTextView.text = message.userName
            }
            messageContainer.layoutParams = params
            
            messageTextView.text = message.message
            timeTextView.text = timeFormat.format(Date(message.createdAt))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
