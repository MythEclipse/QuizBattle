package com.mytheclipse.quizbattle.data.local.dao

import androidx.room.*
import com.mytheclipse.quizbattle.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?
    
    @Query("SELECT * FROM users ORDER BY points DESC LIMIT :limit")
    fun getTopUsers(limit: Int = 10): Flow<List<User>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
    
    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setUserLoggedIn(userId: Long)
    
    @Query("UPDATE users SET points = points + :points, wins = wins + :wins, losses = losses + :losses, totalGames = totalGames + 1 WHERE id = :userId")
    suspend fun updateUserStats(userId: Long, points: Int, wins: Int, losses: Int)
    
    @Delete
    suspend fun deleteUser(user: User)
}
