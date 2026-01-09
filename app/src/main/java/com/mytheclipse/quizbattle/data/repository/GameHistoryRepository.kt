package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.GameHistoryDao
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import kotlinx.coroutines.flow.Flow
    
    private val apiService = com.mytheclipse.quizbattle.data.remote.ApiConfig.createService(com.mytheclipse.quizbattle.data.remote.api.GameHistoryApiService::class.java)

    fun getGameHistoryByUser(userId: Long): Flow<List<UiGameHistory>> {
        return gameHistoryDao.getGameHistoryByUser(userId)
            .map { list ->
                list.map { it.toUiModel() }
            }
    }
    
    suspend fun getRemoteHistory(): List<com.mytheclipse.quizbattle.data.model.UiGameHistory> {
        return try {
            val response = apiService.getGameHistory()
            if (response.success) {
                response.data.map { apiItem ->
                    // Determine if I am player 1 or 2
                    // If opponent ID matches player2Id, then I am player 1.
                    // If opponent ID matches player1Id, then I am player 2.
                    val opponentId = apiItem.opponent?.id ?: ""
                    val isPlayer1 = opponentId == apiItem.player2Id
                    
                    val myScore = if (isPlayer1) apiItem.player1Score else apiItem.player2Score
                    val oppScore = if (isPlayer1) apiItem.player2Score else apiItem.player1Score
                    
                    com.mytheclipse.quizbattle.data.model.UiGameHistory(
                        id = apiItem.id,
                        opponentName = apiItem.opponent?.name ?: "Unknown",
                        userScore = myScore,
                        opponentScore = oppScore,
                        isVictory = apiItem.isWinner,
                        playedAt = parseIsoDate(apiItem.createdAt),
                        gameMode = apiItem.gameMode,
                        totalQuestions = apiItem.totalQuestions
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // Extension function to map local entity to UI model
    private fun GameHistory.toUiModel(): com.mytheclipse.quizbattle.data.model.UiGameHistory {
        return com.mytheclipse.quizbattle.data.model.UiGameHistory(
            id = this.id.toString(),
            opponentName = this.opponentName,
            userScore = this.userScore,
            opponentScore = this.opponentScore,
            isVictory = this.isVictory,
            playedAt = this.playedAt,
            gameMode = this.gameMode,
            totalQuestions = this.totalQuestions
        )
    }

    private fun parseIsoDate(isoDate: String): Long {
        return try {
            // Need to handle ISO 8601 date string from backend (e.g. 2024-01-01T12:00:00.000Z)
            // Simplified parsing or usage SimpleDateFormat
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(isoDate)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    suspend fun getRecentGames(userId: Long, limit: Int = 10): List<GameHistory> {
        return gameHistoryDao.getRecentGames(userId, limit)
    }
    
    fun getWonGames(userId: Long): Flow<List<GameHistory>> {
        return gameHistoryDao.getWonGames(userId)
    }
    
    fun getLostGames(userId: Long): Flow<List<GameHistory>> {
        return gameHistoryDao.getLostGames(userId)
    }
    
    suspend fun getWinCount(userId: Long): Int {
        return gameHistoryDao.getWinCount(userId)
    }
    
    suspend fun getLossCount(userId: Long): Int {
        return gameHistoryDao.getLossCount(userId)
    }
    
    suspend fun getTotalGames(userId: Long): Int {
        return gameHistoryDao.getTotalGames(userId)
    }
    
    suspend fun insertGame(gameHistory: GameHistory): Long {
        return gameHistoryDao.insertGame(gameHistory)
    }
    
    suspend fun updateGame(gameHistory: GameHistory) {
        gameHistoryDao.updateGame(gameHistory)
    }
    
    suspend fun deleteGame(gameHistory: GameHistory) {
        gameHistoryDao.deleteGame(gameHistory)
    }
    
    suspend fun deleteUserGameHistory(userId: Long) {
        gameHistoryDao.deleteUserGameHistory(userId)
    }
}
