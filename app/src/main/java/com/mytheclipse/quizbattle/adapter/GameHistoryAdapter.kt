package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.data.model.UiGameHistory
import com.mytheclipse.quizbattle.databinding.ItemGameHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying game history entries
 */
class GameHistoryAdapter : ListAdapter<UiGameHistory, GameHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGameHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemGameHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: UiGameHistory) {
            with(binding) {
                setupResultIcon(game.isVictory)
                setupPlayerInfo(game)
                setupGameInfo(game)
                setupCardStyle(game.isVictory)
            }
        }
        
        private fun ItemGameHistoryBinding.setupResultIcon(isVictory: Boolean) {
            resultIconTextView.text = if (isVictory) ICON_VICTORY else ICON_DEFEAT
        }
        
        private fun ItemGameHistoryBinding.setupPlayerInfo(game: UiGameHistory) {
            opponentNameTextView.text = root.context.getString(R.string.vs_opponent, game.opponentName)
            scoreTextView.text = "${game.userScore} - ${game.opponentScore}"
            dateTextView.text = formatTimeAgo(game.playedAt)
        }
        
        private fun ItemGameHistoryBinding.setupGameInfo(game: UiGameHistory) {
            val gameMode = getGameModeDisplayName(game.gameMode)
            gameInfoTextView.text = "$gameMode • ${game.totalQuestions} questions"
        }
        
        private fun ItemGameHistoryBinding.setupCardStyle(isVictory: Boolean) {
            val color = if (isVictory) R.color.primary_green else R.color.primary_red
            root.strokeColor = ContextCompat.getColor(root.context, color)
            root.strokeWidth = STROKE_WIDTH
        }
        
        private fun getGameModeDisplayName(mode: String): String = when (mode) {
            MODE_RANKED -> "Ranked"
            MODE_CASUAL -> "Casual"
            MODE_FRIEND -> "Friend"
            MODE_ONLINE -> "Online"
            else -> "Offline"
        }
        
        private fun formatTimeAgo(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Baru saja"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    "${TimeUnit.MILLISECONDS.toMinutes(diff)} menit lalu"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    "${TimeUnit.MILLISECONDS.toHours(diff)} jam lalu"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    "${TimeUnit.MILLISECONDS.toDays(diff)} hari lalu"
                }
                else -> {
                    SimpleDateFormat(DATE_FORMAT, Locale("id", "ID")).format(Date(timestamp))
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UiGameHistory>() {
        override fun areItemsTheSame(oldItem: UiGameHistory, newItem: UiGameHistory) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UiGameHistory, newItem: UiGameHistory) = 
            oldItem == newItem
        
        // Constants
        private const val ICON_VICTORY = "🏆"
        private const val ICON_DEFEAT = "😔"
        private const val STROKE_WIDTH = 2
        private const val DATE_FORMAT = "dd MMM yyyy"
        
        private const val MODE_RANKED = "ranked"
        private const val MODE_CASUAL = "casual"
        private const val MODE_FRIEND = "friend"
        private const val MODE_ONLINE = "online"
    }
}
