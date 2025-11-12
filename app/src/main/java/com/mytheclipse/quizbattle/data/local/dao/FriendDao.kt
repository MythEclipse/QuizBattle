package com.mytheclipse.quizbattle.data.local.dao

import androidx.room.*
import com.mytheclipse.quizbattle.data.local.entity.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE id = :friendId")
    suspend fun getFriendById(friendId: Long): Friend?
    
    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFriendsByUser(userId: Long): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE userId = :userId AND status = :status ORDER BY addedAt DESC")
    fun getFriendsByStatus(userId: Long, status: String): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE userId = :userId AND status = 'accepted' ORDER BY friendPoints DESC LIMIT :limit")
    suspend fun getTopFriends(userId: Long, limit: Int = 10): List<Friend>
    
    @Query("SELECT * FROM friends WHERE userId = :userId AND friendEmail = :friendEmail LIMIT 1")
    suspend fun getFriendByEmail(userId: Long, friendEmail: String): Friend?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend): Long
    
    @Update
    suspend fun updateFriend(friend: Friend)
    
    @Query("UPDATE friends SET status = :status WHERE id = :friendId")
    suspend fun updateFriendStatus(friendId: Long, status: String)
    
    @Delete
    suspend fun deleteFriend(friend: Friend)
    
    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteAllFriends(userId: Long)
}
