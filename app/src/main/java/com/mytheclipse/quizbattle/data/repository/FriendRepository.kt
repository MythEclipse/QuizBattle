package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.local.dao.FriendDao
import com.mytheclipse.quizbattle.data.local.entity.Friend
import kotlinx.coroutines.flow.Flow

class FriendRepository(private val friendDao: FriendDao) {
    
    fun getFriendsByUser(userId: Long): Flow<List<Friend>> {
        return friendDao.getFriendsByUser(userId)
    }
    
    fun getFriendsByStatus(userId: Long, status: String): Flow<List<Friend>> {
        return friendDao.getFriendsByStatus(userId, status)
    }
    
    suspend fun getTopFriends(userId: Long, limit: Int = 10): List<Friend> {
        return friendDao.getTopFriends(userId, limit)
    }
    
    suspend fun getFriendByEmail(userId: Long, friendEmail: String): Friend? {
        return friendDao.getFriendByEmail(userId, friendEmail)
    }
    
    suspend fun insertFriend(friend: Friend): Long {
        return friendDao.insertFriend(friend)
    }
    
    suspend fun updateFriend(friend: Friend) {
        friendDao.updateFriend(friend)
    }
    
    suspend fun updateFriendStatus(friendId: Long, status: String) {
        friendDao.updateFriendStatus(friendId, status)
    }
    
    suspend fun deleteFriend(friend: Friend) {
        friendDao.deleteFriend(friend)
    }
    
    suspend fun deleteAllFriends(userId: Long) {
        friendDao.deleteAllFriends(userId)
    }
}
