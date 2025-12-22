package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MatchmakingRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun observeMatchmakingEvents(): Flow<MatchmakingEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("matchmaking.") == true
            }
            .map { message ->
                parseMatchmakingEvent(message)
            }
    }
    
    private fun parseMatchmakingEvent(message: Map<String, Any>): MatchmakingEvent {
        val type = message["type"] as? String ?: ""
        @Suppress("UNCHECKED_CAST")
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "matchmaking.searching" -> {
                MatchmakingEvent.Searching(
                    queuePosition = (payload["queuePosition"] as? Double)?.toInt() ?: 0,
                    estimatedWaitTime = (payload["estimatedWaitTime"] as? Double)?.toInt() ?: 0
                )
            }
            "matchmaking.found" -> {
                @Suppress("UNCHECKED_CAST")
                val opponentMap = payload["opponent"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val settingsMap = payload["gameSettings"] as? Map<String, Any> ?: emptyMap()
                
                // Backend sends 'points' and 'wins', calculate level from points
                val points = (opponentMap["points"] as? Double)?.toInt() ?: 0
                val calculatedLevel = (points / 100) + 1 // Simple level calculation
                
                MatchmakingEvent.MatchFound(
                    matchId = payload["matchId"] as? String ?: "",
                    opponentId = opponentMap["userId"] as? String ?: "",
                    opponentName = opponentMap["username"] as? String ?: "Opponent",
                    opponentLevel = calculatedLevel,
                    opponentAvatar = opponentMap["avatarUrl"] as? String,
                    difficulty = settingsMap["difficulty"] as? String ?: "medium",
                    category = settingsMap["category"] as? String ?: "general",
                    totalQuestions = (settingsMap["totalQuestions"] as? Double)?.toInt() ?: 10,
                    timePerQuestion = (settingsMap["timePerQuestion"] as? Double)?.toInt() ?: 30
                )
            }
            "matchmaking.cancelled" -> {
                MatchmakingEvent.Cancelled
            }
            else -> MatchmakingEvent.Unknown
        }
    }
    
    fun findMatch(userId: String, gameMode: String = "casual", difficulty: String? = null, category: String? = null) {
        val message = buildMap {
            put("type", "matchmaking.find")
            put("payload", buildMap {
                put("userId", userId)
                put("gameMode", gameMode)
                difficulty?.let { put("difficulty", it) }
                category?.let { put("category", it) }
            })
        }
        webSocketManager.sendMessage(message)
    }
    
    fun cancelMatchmaking(userId: String) {
        val message = mapOf(
            "type" to "matchmaking.cancel",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
}

sealed class MatchmakingEvent {
    data class Searching(val queuePosition: Int, val estimatedWaitTime: Int) : MatchmakingEvent()
    data class MatchFound(
        val matchId: String,
        val opponentId: String,
        val opponentName: String,
        val opponentLevel: Int,
        val opponentAvatar: String?,
        val difficulty: String,
        val category: String,
        val totalQuestions: Int,
        val timePerQuestion: Int
    ) : MatchmakingEvent()
    object Cancelled : MatchmakingEvent()
    object Unknown : MatchmakingEvent()
}
