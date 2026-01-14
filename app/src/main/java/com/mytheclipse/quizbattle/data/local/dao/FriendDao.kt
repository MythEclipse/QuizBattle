package com.mytheclipse.quizbattle.data.local.dao

import androidx.room.*
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.local.entity.FriendStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    
    @Query("SELECT * FROM friends WHERE status = 'ACCEPTED' ORDER BY isOnline DESC, friendName ASC")
    fun getAllFriends(): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE status = 'PENDING_RECEIVED' ORDER BY addedAt DESC")
    fun getPendingReceivedRequests(): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE status = 'PENDING_SENT' ORDER BY addedAt DESC")
    fun getPendingSentRequests(): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE friendId = :friendId LIMIT 1")
    suspend fun getFriendByFriendId(friendId: String): Friend?
    
    @Query("SELECT * FROM friends WHERE id = :id LIMIT 1")
    suspend fun getFriendById(id: String): Friend?
    
    @Query("SELECT COUNT(*) FROM friends WHERE status = 'PENDING_RECEIVED'")
    fun getPendingRequestCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM friends WHERE status = 'ACCEPTED' AND isOnline = 1")
    fun getOnlineFriendsCount(): Flow<Int>
    
    @Query("SELECT * FROM friends WHERE status = 'ACCEPTED' AND isOnline = 1 ORDER BY friendName ASC")
    fun getOnlineFriends(): Flow<List<Friend>>
    
    @Query("SELECT * FROM friends WHERE friendName LIKE '%' || :query || '%' AND status = 'ACCEPTED'")
    fun searchFriends(query: String): Flow<List<Friend>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: Friend)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<Friend>)
    
    @Update
    suspend fun update(friend: Friend)
    
    @Query("UPDATE friends SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: FriendStatus)
    
    @Query("UPDATE friends SET isOnline = :isOnline, lastSeen = :lastSeen WHERE friendId = :friendId")
    suspend fun updateOnlineStatus(friendId: String, isOnline: Boolean, lastSeen: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun delete(friend: Friend)
    
    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM friends WHERE friendId = :friendId")
    suspend fun deleteByFriendId(friendId: String)
    
    @Query("DELETE FROM friends")
    suspend fun deleteAll()
}
