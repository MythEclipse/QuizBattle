package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.model.ChatRoom

/**
 * RecyclerView adapter for displaying chat rooms/conversations.
 * 
 * Shows room name, last message preview, and unread message count badge.
 *
 * @param onRoomClick Callback invoked when a room is clicked
 */
class ChatRoomAdapter(
    private val onRoomClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    // region Adapter Implementation
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view, onRoomClick)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    // endregion

    // region ViewHolder
    
    class ChatRoomViewHolder(
        itemView: View,
        private val onRoomClick: (ChatRoom) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val roomNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)
        
        fun bind(room: ChatRoom) {
            bindRoomInfo(room)
            bindUnreadBadge(room.unreadCount)
            setupClickListener(room)
        }
        
        private fun bindRoomInfo(room: ChatRoom) {
            roomNameTextView.text = room.roomName
            lastMessageTextView.text = room.lastMessage ?: itemView.context.getString(R.string.empty_chat)
        }
        
        private fun bindUnreadBadge(unreadCount: Int) {
            if (unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = formatUnreadCount(unreadCount)
            } else {
                unreadBadge.visibility = View.GONE
            }
        }
        
        private fun formatUnreadCount(count: Int): String {
            return if (count > MAX_BADGE_COUNT) MAX_BADGE_TEXT else count.toString()
        }
        
        private fun setupClickListener(room: ChatRoom) {
            itemView.setOnClickListener { onRoomClick(room) }
        }
        
        companion object {
            private const val MAX_BADGE_COUNT = 99
            private const val MAX_BADGE_TEXT = "99+"
        }
    }
    
    // endregion

    // region DiffCallback
    
    class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.roomId == newItem.roomId
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
    
    // endregion
}
