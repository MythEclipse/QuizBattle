package com.mytheclipse.quizbattle.data.local.dao

import androidx.room.*
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history WHERE id = :gameId")
    suspend fun getGameById(gameId: Long): GameHistory?
    
    @Query("SELECT * FROM game_history WHERE userId = :userId ORDER BY playedAt DESC")
    fun getGameHistoryByUser(userId: Long): Flow<List<GameHistory>>
    
    @Query("SELECT * FROM game_history WHERE userId = :userId ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentGames(userId: Long, limit: Int = 10): List<GameHistory>
    
    @Query("SELECT * FROM game_history WHERE userId = :userId AND isVictory = 1 ORDER BY playedAt DESC")
    fun getWonGames(userId: Long): Flow<List<GameHistory>>
    
    @Query("SELECT * FROM game_history WHERE userId = :userId AND isVictory = 0 ORDER BY playedAt DESC")
    fun getLostGames(userId: Long): Flow<List<GameHistory>>
    
    @Query("SELECT COUNT(*) FROM game_history WHERE userId = :userId AND isVictory = 1")
    suspend fun getWinCount(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM game_history WHERE userId = :userId AND isVictory = 0")
    suspend fun getLossCount(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM game_history WHERE userId = :userId")
    suspend fun getTotalGames(userId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(gameHistory: GameHistory): Long
    
    @Update
    suspend fun updateGame(gameHistory: GameHistory)
    
    @Delete
    suspend fun deleteGame(gameHistory: GameHistory)
    
    @Query("DELETE FROM game_history WHERE userId = :userId")
    suspend fun deleteUserGameHistory(userId: Long)
}
