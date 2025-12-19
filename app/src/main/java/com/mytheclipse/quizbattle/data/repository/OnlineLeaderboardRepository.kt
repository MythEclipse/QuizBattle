package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OnlineLeaderboardRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
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
                        mmr = (item["points"] as? Double)?.toInt() ?: 0 // Use points as mmr
                    )
                }
                // userRank is an object: { rank: number, points: number, percentile: number }
                val userRankObj = payload["userRank"] as? Map<String, Any>
                val userRank = (userRankObj?.get("rank") as? Double)?.toInt() ?: 0
                
                LeaderboardEvent.GlobalData(
                    entries = entries,
                    userRank = userRank,
                    totalPlayers = (payload["totalPlayers"] as? Double)?.toInt() ?: entries.size
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
                        mmr = (item["points"] as? Double)?.toInt() ?: 0 // Use points as mmr
                    )
                }
                // userRank is an object: { rank: number, points: number }
                val userRankObj = payload["userRank"] as? Map<String, Any>
                val userRank = (userRankObj?.get("rank") as? Double)?.toInt() ?: 0
                
                LeaderboardEvent.FriendsData(
                    entries = entries,
                    userRank = userRank,
                    totalFriends = (payload["totalFriends"] as? Double)?.toInt() ?: entries.size
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
