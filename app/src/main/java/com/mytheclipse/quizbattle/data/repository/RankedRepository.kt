package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class RankedRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun requestRankedStats(userId: String) {
        val message = mapOf(
            "type" to "ranked.stats.sync",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
    
    fun requestRankedLeaderboard(userId: String, tier: String? = null, limit: Int = 50, offset: Int = 0) {
        val message = buildMap {
            put("type", "ranked.leaderboard.sync")
            put("payload", buildMap {
                put("userId", userId)
                tier?.let { put("tier", it) }
                put("limit", limit)
                put("offset", offset)
            })
        }
        webSocketManager.sendMessage(message)
    }
    
    fun observeRankedEvents(): Flow<RankedEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("ranked.") == true
            }
            .map { message ->
                parseRankedEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseRankedEvent(message: Map<String, Any>): RankedEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "ranked.stats.data" -> {
                RankedEvent.RankedStatsData(
                    userId = payload["userId"] as? String ?: "",
                    tier = payload["tier"] as? String ?: "bronze",
                    division = (payload["division"] as? Double)?.toInt() ?: 1,
                    mmr = (payload["mmr"] as? Double)?.toInt() ?: 0,
                    rankedPoints = (payload["rankedPoints"] as? Double)?.toInt() ?: 0,
                    wins = (payload["wins"] as? Double)?.toInt() ?: 0,
                    losses = (payload["losses"] as? Double)?.toInt() ?: 0,
                    winRate = (payload["winRate"] as? Double) ?: 0.0,
                    rank = (payload["rank"] as? Double)?.toInt() ?: 0,
                    topPercentage = (payload["topPercentage"] as? Double) ?: 0.0
                )
            }
            "ranked.leaderboard.data" -> {
                val leaderboardList = payload["leaderboard"] as? List<Map<String, Any>> ?: emptyList()
                val entries = leaderboardList.map { item ->
                    RankedLeaderboardEntry(
                        rank = (item["rank"] as? Double)?.toInt() ?: 0,
                        userId = item["userId"] as? String ?: "",
                        username = item["username"] as? String ?: "",
                        tier = item["tier"] as? String ?: "bronze",
                        division = (item["division"] as? Double)?.toInt() ?: 1,
                        mmr = (item["mmr"] as? Double)?.toInt() ?: 0,
                        rankedPoints = (item["rankedPoints"] as? Double)?.toInt() ?: 0,
                        wins = (item["wins"] as? Double)?.toInt() ?: 0,
                        losses = (item["losses"] as? Double)?.toInt() ?: 0,
                        winRate = (item["winRate"] as? Double) ?: 0.0
                    )
                }
                RankedEvent.RankedLeaderboardData(
                    entries = entries,
                    userRank = (payload["userRank"] as? Double)?.toInt() ?: 0,
                    totalPlayers = (payload["totalPlayers"] as? Double)?.toInt() ?: 0
                )
            }
            else -> RankedEvent.Unknown
        }
    }
}

data class RankedLeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val tier: String,
    val division: Int,
    val mmr: Int,
    val rankedPoints: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double
)

sealed class RankedEvent {
    data class RankedStatsData(
        val userId: String,
        val tier: String,
        val division: Int,
        val mmr: Int,
        val rankedPoints: Int,
        val wins: Int,
        val losses: Int,
        val winRate: Double,
        val rank: Int,
        val topPercentage: Double
    ) : RankedEvent()
    
    data class RankedLeaderboardData(
        val entries: List<RankedLeaderboardEntry>,
        val userRank: Int,
        val totalPlayers: Int
    ) : RankedEvent()
    
    object Unknown : RankedEvent()
}
