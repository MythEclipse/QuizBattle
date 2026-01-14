package com.mytheclipse.quizbattle.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.databinding.ItemFriendBinding

class FriendAdapter(
    private val onChatClick: (Friend) -> Unit,
    private val onInviteClick: (Friend) -> Unit,
    private val onRemoveClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(
        private val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.apply {
                tvName.text = friend.friendName
                
                // Status text
                val statusText = if (friend.isOnline) "Online" else "Offline"
                tvStatus.text = "$statusText • ${friend.points} pts"
                
                // Wins
                tvWins.text = "🏆 ${friend.wins} kemenangan"
                
                // Online indicator
                onlineIndicator.visibility = if (friend.isOnline) View.VISIBLE else View.GONE
                
                // Avatar
                if (!friend.friendAvatarUrl.isNullOrEmpty()) {
                    ivAvatar.load(friend.friendAvatarUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile)
                }
                
                // Disable invite if offline
                btnInvite.isEnabled = friend.isOnline
                btnInvite.alpha = if (friend.isOnline) 1f else 0.5f
                
                // Click listeners
                btnChat.setOnClickListener { onChatClick(friend) }
                btnInvite.setOnClickListener { onInviteClick(friend) }
                
                btnMore.setOnClickListener { view ->
                    showPopupMenu(view, friend)
                }
                
                root.setOnClickListener { onChatClick(friend) }
            }
        }
        
        private fun showPopupMenu(view: View, friend: Friend) {
            PopupMenu(view.context, view).apply {
                menuInflater.inflate(R.menu.menu_friend_item, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_remove -> {
                            onRemoveClick(friend)
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
