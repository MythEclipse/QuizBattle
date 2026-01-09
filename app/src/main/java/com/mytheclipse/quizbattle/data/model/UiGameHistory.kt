package com.mytheclipse.quizbattle.data.model

data class UiGameHistory(
    val id: String,
    val opponentName: String,
    val userScore: Int,
    val opponentScore: Int,
    val isVictory: Boolean,
    val playedAt: Long, // timestamp
    val gameMode: String,
    val totalQuestions: Int
)
