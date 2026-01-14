package com.mytheclipse.quizbattle.core.domain.repository

import com.mytheclipse.quizbattle.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import com.mytheclipse.quizbattle.core.domain.Result

/**
 * Interface defining user-related data operations
 * Follows Repository Pattern for clean architecture
 */
interface IUserRepository {
    
    /**
     * Get top users sorted by ranking/score
     */
    fun getTopUsers(limit: Int = 10): Flow<List<User>>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: Long): User?
    
    /**
     * Get user by email
     */
    suspend fun getUserByEmail(email: String): User?
    
    /**
     * Get user by username
     */
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Get currently logged in user
     */
    suspend fun getLoggedInUser(): User?
    
    /**
     * Register a new user
     */
    suspend fun registerUser(username: String, email: String, password: String): Result<User>
    
    /**
     * Login user with email and password
     */
    suspend fun loginUser(email: String, password: String): Result<User>
    
    /**
     * Logout current user
     */
    suspend fun logoutUser()
    
    /**
     * Update user statistics
     */
    suspend fun updateUserStats(userId: Long, points: Int, wins: Int, losses: Int)
    
    /**
     * Update user profile
     */
    suspend fun updateUser(user: User)
    
    /**
     * Delete user
     */
    suspend fun deleteUser(user: User)
    
    /**
     * Create or update user from API login response
     */
    suspend fun createOrLoginFromApi(username: String, email: String): Result<User>
    
    /**
     * Get or create guest user for offline play
     */
    suspend fun getOrCreateGuestUser(): User
    
    /**
     * Upload avatar image
     */
    suspend fun uploadAvatar(file: java.io.File): Result<String>
}
