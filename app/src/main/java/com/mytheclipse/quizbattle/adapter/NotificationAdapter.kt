package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.data.repository.DataModels.NotificationInfo
import com.mytheclipse.quizbattle.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying notifications
 * Uses ViewBinding and lambda callbacks for click handling
 */
class NotificationAdapter(
    private val onNotificationClick: (NotificationInfo) -> Unit,
    private val onDeleteClick: (NotificationInfo) -> Unit
) : ListAdapter<NotificationInfo, NotificationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onNotificationClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemNotificationBinding,
        private val onNotificationClick: (NotificationInfo) -> Unit,
        private val onDeleteClick: (NotificationInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(notification: NotificationInfo) {
            with(binding) {
                setupContent(notification)
                setupReadState(notification.isRead)
                setupClickListeners(notification)
            }
        }
        
        private fun ItemNotificationBinding.setupContent(notification: NotificationInfo) {
            notificationTitleTextView.text = notification.title
            notificationMessageTextView.text = notification.message
            notificationTimeTextView.text = formatTime(notification.createdAt)
        }
        
        private fun ItemNotificationBinding.setupReadState(isRead: Boolean) {
            unreadIndicator.isVisible = !isRead
            root.alpha = if (isRead) ALPHA_READ else ALPHA_UNREAD
        }
        
        private fun ItemNotificationBinding.setupClickListeners(notification: NotificationInfo) {
            root.setOnClickListener { onNotificationClick(notification) }
            deleteButton.setOnClickListener { onDeleteClick(notification) }
        }
        
        private fun formatTime(timestamp: Long): String {
            return SimpleDateFormat(TIME_FORMAT, Locale("id", "ID")).format(Date(timestamp))
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationInfo>() {
        override fun areItemsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo) =
            oldItem.notificationId == newItem.notificationId

        override fun areContentsTheSame(oldItem: NotificationInfo, newItem: NotificationInfo) =
            oldItem == newItem
        
        private const val TIME_FORMAT = "dd MMM, HH:mm"
        private const val ALPHA_READ = 0.7f
        private const val ALPHA_UNREAD = 1f
    }
}
