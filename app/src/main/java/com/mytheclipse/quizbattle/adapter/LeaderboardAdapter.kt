package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.repository.DataModels
import com.mytheclipse.quizbattle.databinding.ItemLeaderboardBinding

/**
 * Adapter for displaying leaderboard entries
 * Uses DiffUtil for efficient updates
 */
class LeaderboardAdapter : ListAdapter<DataModels.LeaderboardEntry, LeaderboardAdapter.ViewHolder>(DiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
    
    /**
     * ViewHolder with clean binding logic
     */
    class ViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(entry: DataModels.LeaderboardEntry, rank: Int) {
            with(binding) {
                setupRank(rank)
                setupAvatar(entry.userName)
                setupPlayerInfo(entry)
                pointsTextView.text = entry.score.toString()
            }
        }
        
        private fun ItemLeaderboardBinding.setupRank(rank: Int) {
            when (rank) {
                RANK_FIRST -> setMedal(MEDAL_GOLD, COLOR_GOLD)
                RANK_SECOND -> setMedal(MEDAL_SILVER, COLOR_SILVER)
                RANK_THIRD -> setMedal(MEDAL_BRONZE, COLOR_BRONZE)
                else -> setNumericRank(rank)
            }
        }
        
        private fun ItemLeaderboardBinding.setMedal(medal: String, color: Int) {
            rankTextView.text = medal
            rankTextView.setTextColor(color)
        }
        
        private fun ItemLeaderboardBinding.setNumericRank(rank: Int) {
            rankTextView.text = rank.toString()
            rankTextView.setTextColor(
                ContextCompat.getColor(root.context, R.color.text_primary)
            )
        }
        
        private fun ItemLeaderboardBinding.setupAvatar(userName: String) {
            val initial = userName.firstOrNull()?.uppercaseChar() ?: DEFAULT_INITIAL
            avatarTextView.text = initial.toString()
        }
        
        private fun ItemLeaderboardBinding.setupPlayerInfo(entry: DataModels.LeaderboardEntry) {
            usernameTextView.text = entry.userName
            winsTextView.text = root.context.getString(R.string.wins_count, entry.wins)
            winRateTextView.text = formatWinRate(entry.wins, entry.losses)
        }
        
        private fun formatWinRate(wins: Int, losses: Int): String {
            val totalGames = wins + losses
            val winRate = if (totalGames > 0) {
                (wins.toDouble() / totalGames * WIN_RATE_MULTIPLIER)
            } else {
                0.0
            }
            return String.format(WIN_RATE_FORMAT, winRate)
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    companion object DiffCallback : DiffUtil.ItemCallback<DataModels.LeaderboardEntry>() {
        
        override fun areItemsTheSame(
            oldItem: DataModels.LeaderboardEntry,
            newItem: DataModels.LeaderboardEntry
        ): Boolean = oldItem.userId == newItem.userId
        
        override fun areContentsTheSame(
            oldItem: DataModels.LeaderboardEntry,
            newItem: DataModels.LeaderboardEntry
        ): Boolean = oldItem == newItem
        
        // ===== Constants =====
        private const val RANK_FIRST = 1
        private const val RANK_SECOND = 2
        private const val RANK_THIRD = 3
        
        private const val MEDAL_GOLD = "🥇"
        private const val MEDAL_SILVER = "🥈"
        private const val MEDAL_BRONZE = "🥉"
        
        private const val COLOR_GOLD = 0xFFFFD700.toInt()
        private const val COLOR_SILVER = 0xFFC0C0C0.toInt()
        private const val COLOR_BRONZE = 0xFFCD7F32.toInt()
        
        private const val DEFAULT_INITIAL = '?'
        private const val WIN_RATE_MULTIPLIER = 100
        private const val WIN_RATE_FORMAT = "%.0f%% win rate"
    }
}

