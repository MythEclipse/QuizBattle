package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R

import com.mytheclipse.quizbattle.databinding.ItemGameHistoryBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

import com.mytheclipse.quizbattle.data.model.UiGameHistory

class GameHistoryAdapter : ListAdapter<UiGameHistory, GameHistoryAdapter.GameHistoryViewHolder>(GameHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHistoryViewHolder {
        val binding = ItemGameHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameHistoryViewHolder, position: Int) {
        val gameHistory = getItem(position)
        holder.bind(gameHistory)
    }

    class GameHistoryViewHolder(
        private val binding: ItemGameHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gameHistory: UiGameHistory) {
            // Set result icon
            binding.resultIconTextView.text = if (gameHistory.isVictory) "🏆" else "😔"
            
            // Set opponent name
            binding.opponentNameTextView.text = "vs ${gameHistory.opponentName}"
            
            // Set score
            binding.scoreTextView.text = "${gameHistory.userScore} - ${gameHistory.opponentScore}"
            
            // Set game info
            val gameMode = when (gameHistory.gameMode) {
                "ranked" -> "Ranked"
                "casual" -> "Casual"
                "friend" -> "Friend"
                "online" -> "Online"
                else -> "Offline"
            }
            binding.gameInfoTextView.text = "$gameMode • ${gameHistory.totalQuestions} questions"
            
            // Set date
            binding.dateTextView.text = formatTimeAgo(gameHistory.playedAt)
            
            // Set background color based on result
            val cardView = binding.root
            if (gameHistory.isVictory) {
                cardView.strokeColor = cardView.context.getColor(R.color.primary_green)
                cardView.strokeWidth = 2
            } else {
                cardView.strokeColor = cardView.context.getColor(R.color.primary_red)
                cardView.strokeWidth = 2
            }
        }
        
        private fun formatTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "${minutes}m ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "${hours}h ago"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "${days}d ago"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    class GameHistoryDiffCallback : DiffUtil.ItemCallback<UiGameHistory>() {
        override fun areItemsTheSame(oldItem: UiGameHistory, newItem: UiGameHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UiGameHistory, newItem: UiGameHistory): Boolean {
            return oldItem == newItem
        }
    }
}
