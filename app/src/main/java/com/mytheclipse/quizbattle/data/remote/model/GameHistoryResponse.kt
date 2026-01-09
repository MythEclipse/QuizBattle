package com.mytheclipse.quizbattle.data.remote.model

import com.google.gson.annotations.SerializedName

data class GameHistoryResponse(
    val success: Boolean,
    val data: List<ApiGameHistoryItem>,
    val error: String? = null
)

data class ApiGameHistoryItem(
    val id: String,
    val player1Id: String,
    val player2Id: String,
    val winnerId: String?,
    val gameMode: String,
    val difficulty: String,
    val category: String,
    val status: String,
    val player1Score: Int,
    val player2Score: Int,
    val totalQuestions: Int,
    val createdAt: String,
    val isWinner: Boolean,
    val opponent: ApiOpponent?
)

data class ApiOpponent(
    val id: String,
    val name: String,
    val image: String?
)
