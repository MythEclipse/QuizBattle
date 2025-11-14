package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.remote.api.UsersApiService
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OnlineLeaderboardRepository {
    
    private val apiService = ApiConfig.createService(UsersApiService::class.java)
    private val webSocketManager = WebSocketManager.getInstance()
    
    suspend fun getGlobalLeaderboard(limit: Int = 50, offset: Int = 0): Result<List<DataModels.LeaderboardEntry>> {
        return try {
            val response = apiService.getAllUsers()
            if (response.success) {
                // Convert users to leaderboard entries
                val entries = response.users
                    .sortedByDescending { it.role } // Placeholder sorting
                    .mapIndexed { index, user ->
                        DataModels.LeaderboardEntry(
                            userId = user.id,
                            userName = user.name ?: "Unknown",
                            score = 0, // Default
                            wins = 0,
                            losses = 0,
                            mmr = 0 // Default
                        )
                    }
                    .take(limit)
                Result.success(entries)
            } else {
                Result.failure(Exception("Failed to fetch leaderboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun syncGlobalLeaderboard(userId: String, limit: Int = 50, offset: Int = 0) {
        val message = mapOf(
            "type" to "leaderboard.global.sync",
            "payload" to mapOf(
                "userId" to userId,
                "limit" to limit,
                "offset" to offset
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun syncFriendsLeaderboard(userId: String) {
        val message = mapOf(
            "type" to "leaderboard.friends.sync",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
    
    fun observeLeaderboardEvents(): Flow<LeaderboardEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("leaderboard.") == true
            }
            .map { message ->
                parseLeaderboardEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseLeaderboardEvent(message: Map<String, Any>): LeaderboardEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "leaderboard.global.data" -> {
                val leaderboardList = payload["leaderboard"] as? List<Map<String, Any>> ?: emptyList()
                val entries = leaderboardList.map { item ->
                    DataModels.LeaderboardEntry(
                        userId = item["userId"] as? String ?: "",
                        userName = item["username"] as? String ?: "",
                        score = (item["points"] as? Double)?.toInt() ?: 0,
                        wins = (item["wins"] as? Double)?.toInt() ?: 0,
                        losses = (item["losses"] as? Double)?.toInt() ?: 0,
                        mmr = (item["mmr"] as? Double)?.toInt() ?: 0
                    )
                }
                LeaderboardEvent.GlobalData(
                    entries = entries,
                    userRank = (payload["userRank"] as? Double)?.toInt() ?: 0,
                    totalPlayers = (payload["totalPlayers"] as? Double)?.toInt() ?: 0
                )
            }
            "leaderboard.friends.data" -> {
                val leaderboardList = payload["leaderboard"] as? List<Map<String, Any>> ?: emptyList()
                val entries = leaderboardList.map { item ->
                    DataModels.LeaderboardEntry(
                        userId = item["userId"] as? String ?: "",
                        userName = item["username"] as? String ?: "",
                        score = (item["points"] as? Double)?.toInt() ?: 0,
                        wins = (item["wins"] as? Double)?.toInt() ?: 0,
                        losses = (item["losses"] as? Double)?.toInt() ?: 0,
                        mmr = (item["mmr"] as? Double)?.toInt() ?: 0
                    )
                }
                LeaderboardEvent.FriendsData(
                    entries = entries,
                    userRank = (payload["userRank"] as? Double)?.toInt() ?: 0,
                    totalFriends = (payload["totalFriends"] as? Double)?.toInt() ?: 0
                )
            }
            else -> LeaderboardEvent.Unknown
        }
    }
}

sealed class LeaderboardEvent {
    data class GlobalData(
        val entries: List<DataModels.LeaderboardEntry>,
        val userRank: Int,
        val totalPlayers: Int
    ) : LeaderboardEvent()
    
    data class FriendsData(
        val entries: List<DataModels.LeaderboardEntry>,
        val userRank: Int,
        val totalFriends: Int
    ) : LeaderboardEvent()
    
    object Unknown : LeaderboardEvent()
}
