package com.mytheclipse.quizbattle.data.remote.model

import com.google.gson.annotations.SerializedName

sealed class WebSocketMessage {
    abstract val type: String
}

// Authentication
data class AuthConnectMessage(
    override val type: String = "auth:connect",
    val payload: AuthConnectPayload
) : WebSocketMessage()

data class AuthConnectPayload(
    val userId: String,
    val token: String,
    val username: String,
    val deviceId: String
)

data class AuthConnectedMessage(
    override val type: String = "auth:connected",
    val payload: AuthConnectedPayload
) : WebSocketMessage()

data class AuthConnectedPayload(
    val sessionId: String,
    val userId: String,
    val timestamp: Long
)

// Matchmaking
data class MatchmakingFindMessage(
    override val type: String = "matchmaking.find",
    val payload: MatchmakingFindPayload
) : WebSocketMessage()

data class MatchmakingFindPayload(
    val userId: String,
    val gameMode: String = "casual", // Options: ranked, casual, friend
    val difficulty: String? = null,
    val category: String? = null
)

data class MatchmakingSearchingMessage(
    override val type: String = "matchmaking.searching",
    val payload: MatchmakingSearchingPayload
) : WebSocketMessage()

data class MatchmakingSearchingPayload(
    val userId: String,
    val queuePosition: Int,
    val estimatedWaitTime: Int
)

data class MatchFoundMessage(
    override val type: String = "matchmaking.match_found",
    val payload: MatchFoundPayload
) : WebSocketMessage()

data class MatchFoundPayload(
    val matchId: String,
    val opponent: OpponentInfo,
    val gameSettings: GameSettingsWS
)

data class OpponentInfo(
    val userId: String,
    val username: String,
    val level: Int,
    val avatar: String?
)

data class GameSettingsWS(
    val difficulty: String,
    val category: String,
    val totalQuestions: Int,
    val timePerQuestion: Int
)

// Game
data class GameAnswerSubmitMessage(
    override val type: String = "game.answer.submit",
    val payload: GameAnswerSubmitPayload
) : WebSocketMessage()

data class GameAnswerSubmitPayload(
    val userId: String,
    val matchId: String,
    val questionId: String,
    val answer: String,
    val timeSpent: Int
)

data class GameAnswerResultMessage(
    override val type: String = "game.answer.result",
    val payload: GameAnswerResultPayload
) : WebSocketMessage()

data class GameAnswerResultPayload(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val points: Int,
    val timeBonus: Int
)

data class GameStartingMessage(
    override val type: String = "lobby.game.starting",
    val payload: GameStartingPayload
) : WebSocketMessage()

data class GameStartingPayload(
    val lobbyId: String,
    val countdown: Int
)

data class GameFinishedMessage(
    override val type: String = "game.finished",
    val payload: GameFinishedPayload
) : WebSocketMessage()

data class GameFinishedPayload(
    val matchId: String,
    val winner: String,
    val playerStats: PlayerStats,
    val opponentStats: PlayerStats
)

data class PlayerStats(
    val userId: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int
)

// Connection
data class PingMessage(
    override val type: String = "connection.ping",
    val payload: PingPayload
) : WebSocketMessage()

data class PingPayload(
    val userId: String
)

data class PongMessage(
    override val type: String = "connection.pong",
    val payload: PongPayload
) : WebSocketMessage()

data class PongPayload(
    val timestamp: Long
)

// Generic WebSocket wrapper
data class WSMessage(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("payload")
    val payload: Map<String, Any>
)
