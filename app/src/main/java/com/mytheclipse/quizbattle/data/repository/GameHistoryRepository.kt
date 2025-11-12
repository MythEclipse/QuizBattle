package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.GameHistoryDao
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import kotlinx.coroutines.flow.Flow

class GameHistoryRepository(private val gameHistoryDao: GameHistoryDao) {
    
    fun getGameHistoryByUser(userId: Long): Flow<List<GameHistory>> {
        return gameHistoryDao.getGameHistoryByUser(userId)
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
