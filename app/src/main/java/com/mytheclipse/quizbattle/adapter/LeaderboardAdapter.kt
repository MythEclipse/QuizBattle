package com.mytheclipse.quizbattle.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.repository.DataModels
import com.mytheclipse.quizbattle.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : ListAdapter<DataModels.LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaderboardViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
    
    class LeaderboardViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(entry: DataModels.LeaderboardEntry, position: Int) {
            with(binding) {
                // Rank with special styling for top 3
                rankTextView.text = position.toString()
                when (position) {
                    1 -> {
                        rankTextView.text = "🥇"
                        rankTextView.setTextColor(Color.parseColor("#FFD700"))
                    }
                    2 -> {
                        rankTextView.text = "🥈"
                        rankTextView.setTextColor(Color.parseColor("#C0C0C0"))
                    }
                    3 -> {
                        rankTextView.text = "🥉"
                        rankTextView.setTextColor(Color.parseColor("#CD7F32"))
                    }
                    else -> {
                        rankTextView.setTextColor(root.context.getColor(R.color.text_primary))
                    }
                }
                
                // Avatar - use first letter of username
                val initial = entry.userName.firstOrNull()?.uppercaseChar() ?: '?'
                avatarTextView.text = initial.toString()
                
                // Player info
                usernameTextView.text = entry.userName
                winsTextView.text = "${entry.wins} wins"
                
                // Calculate and display win rate
                val winRate = if (entry.wins + entry.losses > 0) {
                    (entry.wins.toDouble() / (entry.wins + entry.losses) * 100)
                } else 0.0
                winRateTextView.text = String.format("%.0f%% win rate", winRate)
                
                // Score/Points
                pointsTextView.text = entry.score.toString()
            }
        }
    }
    
    class LeaderboardDiffCallback : DiffUtil.ItemCallback<DataModels.LeaderboardEntry>() {
        override fun areItemsTheSame(
            oldItem: DataModels.LeaderboardEntry,
            newItem: DataModels.LeaderboardEntry
        ): Boolean {
            return oldItem.userId == newItem.userId
        }
        
        override fun areContentsTheSame(
            oldItem: DataModels.LeaderboardEntry,
            newItem: DataModels.LeaderboardEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}

