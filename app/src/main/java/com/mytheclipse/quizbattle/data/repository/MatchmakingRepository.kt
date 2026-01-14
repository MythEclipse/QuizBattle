package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.utils.AppLogger
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
                
                val matchId = payload["matchId"] as? String ?: ""
                val opponentName = opponentMap["username"] as? String ?: "Opponent"
                AppLogger.Match.found(matchId, opponentName)
                
                MatchmakingEvent.MatchFound(
                    matchId = matchId,
                    opponentId = opponentMap["userId"] as? String ?: "",
                    opponentName = opponentName,
                    opponentLevel = calculatedLevel,
                    opponentAvatar = opponentMap["avatarUrl"] as? String,
                    difficulty = settingsMap["difficulty"] as? String ?: "medium",
                    category = settingsMap["category"] as? String ?: "general",
                    totalQuestions = (settingsMap["totalQuestions"] as? Double)?.toInt() ?: 10,
                    timePerQuestion = (settingsMap["timePerQuestion"] as? Double)?.toInt() ?: 30
                )
            }
            "matchmaking.confirm.request" -> {
                @Suppress("UNCHECKED_CAST")
                val opponentMap = payload["opponent"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val settingsMap = payload["gameSettings"] as? Map<String, Any> ?: emptyMap()
                
                val points = (opponentMap["points"] as? Double)?.toInt() ?: 0
                val calculatedLevel = (points / 100) + 1
                
                // Backend sends timeToConfirm in seconds, convert to millis
                val timeToConfirmSec = (payload["timeToConfirm"] as? Double)?.toLong() ?: 30
                
                MatchmakingEvent.ConfirmRequest(
                    matchId = payload["matchId"] as? String ?: "",
                    opponentId = opponentMap["userId"] as? String ?: "",
                    opponentName = opponentMap["username"] as? String ?: "Opponent",
                    opponentLevel = calculatedLevel,
                    opponentPoints = points,
                    opponentAvatar = opponentMap["avatarUrl"] as? String,
                    difficulty = settingsMap["difficulty"] as? String ?: "medium",
                    category = settingsMap["category"] as? String ?: "general",
                    totalQuestions = (settingsMap["totalQuestions"] as? Double)?.toInt() ?: 10,
                    timePerQuestion = (settingsMap["timePerQuestion"] as? Double)?.toInt() ?: 30,
                    expiresIn = timeToConfirmSec * 1000
                )
            }
            "matchmaking.confirm.status" -> {
                val playerConfirmed = payload["playerConfirmed"] as? Boolean ?: false
                val opponentConfirmed = payload["opponentConfirmed"] as? Boolean ?: false
                val confirmedCount = (if (playerConfirmed) 1 else 0) + (if (opponentConfirmed) 1 else 0)
                
                MatchmakingEvent.ConfirmStatus(
                    matchId = payload["matchId"] as? String ?: "",
                    status = payload["status"] as? String ?: "waiting",
                    confirmedCount = confirmedCount,
                    totalPlayers = 2
                )
            }
            "matchmaking.cancelled" -> {
                MatchmakingEvent.Cancelled
            }
            else -> MatchmakingEvent.Unknown
        }
    }
    
    fun findMatch(userId: String, gameMode: String = "casual", difficulty: String? = null, category: String? = null) {
        AppLogger.Match.searching(gameMode)
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
    
    fun confirmMatch(userId: String, matchId: String, confirmed: Boolean) {
        AppLogger.log(AppLogger.LogLevel.INFO, "Match", if (confirmed) "Match confirmed: $matchId" else "Match declined: $matchId")
        val message = mapOf(
            "type" to "matchmaking.confirm",
            "payload" to mapOf(
                "userId" to userId,
                "matchId" to matchId,
                "confirmed" to confirmed
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun cancelMatchmaking(userId: String) {
        AppLogger.Match.cancelled("User requested")
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
    data class ConfirmRequest(
        val matchId: String,
        val opponentId: String,
        val opponentName: String,
        val opponentLevel: Int,
        val opponentPoints: Int,
        val opponentAvatar: String?,
        val difficulty: String,
        val category: String,
        val totalQuestions: Int,
        val timePerQuestion: Int,
        val expiresIn: Long
    ) : MatchmakingEvent()
    data class ConfirmStatus(
        val matchId: String,
        val status: String,
        val confirmedCount: Int,
        val totalPlayers: Int
    ) : MatchmakingEvent()
    object Cancelled : MatchmakingEvent()
    object Unknown : MatchmakingEvent()
}
