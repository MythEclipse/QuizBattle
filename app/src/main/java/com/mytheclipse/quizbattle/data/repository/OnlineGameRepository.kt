package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OnlineGameRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun observeGameEvents(): Flow<GameEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("game.") == true || type == "lobby.game.starting"
            }
            .map { message ->
                parseGameEvent(message)
            }
    }
    
    private fun parseGameEvent(message: Map<String, Any>): GameEvent {
        val type = message["type"] as? String ?: ""
        @Suppress("UNCHECKED_CAST")
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "game.answer.result" -> {
                GameEvent.AnswerResult(
                    isCorrect = payload["isCorrect"] as? Boolean ?: false,
                    correctAnswer = payload["correctAnswer"] as? String ?: "",
                    points = (payload["points"] as? Double)?.toInt() ?: 0,
                    timeBonus = (payload["timeBonus"] as? Double)?.toInt() ?: 0
                )
            }
            "lobby.game.starting" -> {
                GameEvent.GameStarting(
                    countdown = (payload["countdown"] as? Double)?.toInt() ?: 3
                )
            }
            "game.finished" -> {
                @Suppress("UNCHECKED_CAST")
                val playerStatsMap = payload["playerStats"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val opponentStatsMap = payload["opponentStats"] as? Map<String, Any> ?: emptyMap()
                
                GameEvent.GameFinished(
                    matchId = payload["matchId"] as? String ?: "",
                    winner = payload["winner"] as? String ?: "",
                    playerScore = (playerStatsMap["score"] as? Double)?.toInt() ?: 0,
                    playerCorrect = (playerStatsMap["correctAnswers"] as? Double)?.toInt() ?: 0,
                    opponentScore = (opponentStatsMap["score"] as? Double)?.toInt() ?: 0,
                    opponentCorrect = (opponentStatsMap["correctAnswers"] as? Double)?.toInt() ?: 0
                )
            }
            "game.opponent.answered" -> {
                GameEvent.OpponentAnswered
            }
            "game.opponent.disconnected" -> {
                GameEvent.OpponentDisconnected
            }
            else -> GameEvent.Unknown
        }
    }
    
    fun connectToMatch(matchId: String) {
        val message = mapOf(
            "type" to "game.connect",
            "payload" to mapOf(
                "matchId" to matchId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun submitAnswer(userId: String, matchId: String, questionId: String, answer: String, timeSpent: Int) {
        val message = mapOf(
            "type" to "game.answer.submit",
            "payload" to mapOf(
                "userId" to userId,
                "matchId" to matchId,
                "questionId" to questionId,
                "answer" to answer,
                "timeSpent" to timeSpent
            )
        )
        webSocketManager.sendMessage(message)
    }
}

sealed class GameEvent {
    data class AnswerResult(
        val isCorrect: Boolean,
        val correctAnswer: String,
        val points: Int,
        val timeBonus: Int
    ) : GameEvent()
    
    data class GameStarting(val countdown: Int) : GameEvent()
    
    data class GameFinished(
        val matchId: String,
        val winner: String,
        val playerScore: Int,
        val playerCorrect: Int,
        val opponentScore: Int,
        val opponentCorrect: Int
    ) : GameEvent()
    
    object OpponentAnswered : GameEvent()
    object OpponentDisconnected : GameEvent()
    object Unknown : GameEvent()
}
