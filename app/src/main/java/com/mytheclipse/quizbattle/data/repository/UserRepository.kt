package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.UserDao
import com.mytheclipse.quizbattle.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getTopUsers(limit: Int = 10): Flow<List<User>> {
        return userDao.getTopUsers(limit)
    }
    
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()
    }
    
    suspend fun registerUser(username: String, email: String, password: String): Result<User> {
        return try {
            // Check if email already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Email sudah terdaftar"))
            }
            
            // Check if username already exists
            val existingUsername = userDao.getUserByUsername(username)
            if (existingUsername != null) {
                return Result.failure(Exception("Username sudah digunakan"))
            }
            
            val user = User(
                username = username,
                email = email,
                password = password, // In production, hash this password
                isLoggedIn = false
            )
            
            val userId = userDao.insertUser(user)
            Result.success(user.copy(id = userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val user = userDao.login(email, password)
            if (user != null) {
                // Logout all other users first
                userDao.logoutAllUsers()
                // Set this user as logged in
                userDao.setUserLoggedIn(user.id)
                Result.success(user.copy(isLoggedIn = true))
            } else {
                Result.failure(Exception("Email atau password salah"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logoutUser() {
        userDao.logoutAllUsers()
    }
    
    suspend fun updateUserStats(userId: Long, points: Int, wins: Int, losses: Int) {
        userDao.updateUserStats(userId, points, wins, losses)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
    
    /**
     * Creates a new user from API login response or updates existing user.
     * Always sets isLoggedIn = true.
     * This should be used after successful API login instead of registerUser().
     */
    suspend fun createOrLoginFromApi(username: String, email: String): Result<User> {
        return try {
            // Logout all other users first
            userDao.logoutAllUsers()
            
            // Check if user exists by email
            val existingUser = userDao.getUserByEmail(email)
            
            if (existingUser != null) {
                // Update existing user and set logged in
                userDao.setUserLoggedIn(existingUser.id)
                Result.success(existingUser.copy(isLoggedIn = true))
            } else {
                // Create new user with isLoggedIn = true
                val user = User(
                    username = username,
                    email = email,
                    password = "", // Password handled by API, not stored locally
                    isLoggedIn = true
                )
                val userId = userDao.insertUser(user)
                Result.success(user.copy(id = userId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
