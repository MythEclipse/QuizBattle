package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.repository.DataModels.NotificationInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onNotificationClick: (NotificationInfo) -> Unit,
    private val onDeleteClick: (NotificationInfo) -> Unit
) : ListAdapter<NotificationInfo, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onNotificationClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        itemView: View,
        private val onNotificationClick: (NotificationInfo) -> Unit,
        private val onDeleteClick: (NotificationInfo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val titleTextView: TextView = itemView.findViewById(R.id.notificationTitleTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.notificationMessageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.notificationTimeTextView)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        
        private val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        
        fun bind(notification: NotificationInfo) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timeTextView.text = timeFormat.format(Date(notification.createdAt))
            
            unreadIndicator.visibility = if (notification.isRead) View.INVISIBLE else View.VISIBLE
            
            // Visual difference for read/unread
            itemView.alpha = if (notification.isRead) 0.7f else 1f
            
            itemView.setOnClickListener { onNotificationClick(notification) }
            deleteButton.setOnClickListener { onDeleteClick(notification) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationInfo>() {
        override fun areItemsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo): Boolean {
            return oldItem.notificationId == newItem.notificationId
        }

        override fun areContentsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo): Boolean {
            return oldItem == newItem
        }
    }
}
