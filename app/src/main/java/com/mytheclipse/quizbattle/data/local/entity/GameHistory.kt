package com.mytheclipse.quizbattle.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val opponentName: String, // For offline: "AI Bot" or friend name
    val userScore: Int,
    val opponentScore: Int,
    val isVictory: Boolean,
    val totalQuestions: Int,
    val playedAt: Long = System.currentTimeMillis(),
    val gameMode: String = "offline" // "offline", "online", "friend"
)
