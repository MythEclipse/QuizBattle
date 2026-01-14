package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.repository.ChatRoom

class ChatRoomAdapter(
    private val onRoomClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view, onRoomClick)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatRoomViewHolder(
        itemView: View,
        private val onRoomClick: (ChatRoom) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val roomNameTextView: TextView = itemView.findViewById(R.id.roomNameTextView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)
        
        fun bind(room: ChatRoom) {
            roomNameTextView.text = room.roomName
            lastMessageTextView.text = room.lastMessage ?: "No messages yet"
            
            if (room.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = if (room.unreadCount > 99) "99+" else room.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }
            
            itemView.setOnClickListener { onRoomClick(room) }
        }
    }

    class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.roomId == newItem.roomId
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
}
