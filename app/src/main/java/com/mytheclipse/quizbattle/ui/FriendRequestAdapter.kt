package com.mytheclipse.quizbattle.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.databinding.ItemFriendRequestBinding

class FriendRequestAdapter(
    private val onAcceptClick: (Friend) -> Unit,
    private val onRejectClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendRequestAdapter.RequestViewHolder>(FriendRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemFriendRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.apply {
                tvName.text = friend.friendName
                tvPoints.text = "${friend.points} poin"
                
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
                
                // Message is not stored in Friend entity, so hide it
                // In real implementation, you'd store the message
                tvMessage.visibility = View.GONE
                
                // Click listeners
                btnAccept.setOnClickListener { onAcceptClick(friend) }
                btnReject.setOnClickListener { onRejectClick(friend) }
            }
        }
    }

    class FriendRequestDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
