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

// Friend Request Messages
data class FriendRequestSendMessage(
    override val type: String = "friend.request.send",
    val payload: FriendRequestSendPayload
) : WebSocketMessage()

data class FriendRequestSendPayload(
    val userId: String,
    val targetUserId: String,
    val message: String? = null
)

data class FriendRequestReceivedMessage(
    override val type: String = "friend.request.received",
    val payload: FriendRequestReceivedPayload
) : WebSocketMessage()

data class FriendRequestReceivedPayload(
    val requestId: String,
    val sender: FriendSenderInfo,
    val message: String? = null
)

data class FriendSenderInfo(
    val userId: String,
    val username: String,
    val points: Int = 0,
    val avatarUrl: String? = null
)

data class FriendRequestRespondMessage(
    override val type: String = "friend.request.respond",
    val payload: FriendRequestRespondPayload
) : WebSocketMessage()

data class FriendRequestRespondPayload(
    val userId: String,
    val requestId: String,
    val accept: Boolean
)

// Match Invite Messages
data class MatchInviteSendMessage(
    override val type: String = "match.invite.send",
    val payload: MatchInviteSendPayload
) : WebSocketMessage()

data class MatchInviteSendPayload(
    val senderId: String,
    val receiverId: String,
    val gameSettings: InviteGameSettings,
    val message: String? = null
)

data class InviteGameSettings(
    val difficulty: String,
    val category: String,
    val totalQuestions: Int,
    val timePerQuestion: Int
)

data class MatchInviteReceivedMessage(
    override val type: String = "match.invite.received",
    val payload: MatchInviteReceivedPayload
) : WebSocketMessage()

data class MatchInviteReceivedPayload(
    val inviteId: String,
    val sender: MatchInviteSenderInfo,
    val gameSettings: InviteGameSettings,
    val message: String? = null,
    val expiresIn: Long = 60000
)

data class MatchInviteSenderInfo(
    val userId: String,
    val username: String,
    val points: Int = 0,
    val wins: Int = 0,
    val avatarUrl: String? = null
)

data class MatchInviteRespondMessage(
    override val type: String = "match.invite.respond",
    val payload: MatchInviteRespondPayload
) : WebSocketMessage()

data class MatchInviteRespondPayload(
    val userId: String,
    val inviteId: String,
    val accept: Boolean
)

data class MatchInviteAcceptedMessage(
    override val type: String = "match.invite.accepted",
    val payload: MatchInviteAcceptedPayload
) : WebSocketMessage()

data class MatchInviteAcceptedPayload(
    val inviteId: String,
    val matchId: String,
    val opponent: OpponentInfo,
    val gameSettings: InviteGameSettings,
    val startIn: Int = 5
)

// Matchmaking Confirmation Messages
data class MatchmakingConfirmRequestMessage(
    override val type: String = "matchmaking.confirm.request",
    val payload: MatchmakingConfirmRequestPayload
) : WebSocketMessage()

data class MatchmakingConfirmRequestPayload(
    val matchId: String,
    val opponent: OpponentInfo,
    val gameSettings: GameSettingsWS,
    val expiresIn: Long = 30000
)

data class MatchmakingConfirmMessage(
    override val type: String = "matchmaking.confirm",
    val payload: MatchmakingConfirmPayload
) : WebSocketMessage()

data class MatchmakingConfirmPayload(
    val userId: String,
    val matchId: String,
    val accept: Boolean
)

data class MatchmakingConfirmStatusMessage(
    override val type: String = "matchmaking.confirm.status",
    val payload: MatchmakingConfirmStatusPayload
) : WebSocketMessage()

data class MatchmakingConfirmStatusPayload(
    val matchId: String,
    val status: String, // waiting, both_confirmed, rejected
    val confirmedCount: Int,
    val totalPlayers: Int
)

// Generic WebSocket wrapper
data class WSMessage(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("payload")
    val payload: Map<String, Any>
)
