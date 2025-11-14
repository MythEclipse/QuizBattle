package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class LobbyRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun createLobby(
        hostId: String,
        maxPlayers: Int,
        isPrivate: Boolean,
        difficulty: String,
        category: String,
        totalQuestions: Int,
        timePerQuestion: Int
    ) {
        val message = mapOf(
            "type" to "lobby.create",
            "payload" to mapOf(
                "hostId" to hostId,
                "maxPlayers" to maxPlayers,
                "isPrivate" to isPrivate,
                "gameSettings" to mapOf(
                    "difficulty" to difficulty,
                    "category" to category,
                    "totalQuestions" to totalQuestions,
                    "timePerQuestion" to timePerQuestion
                )
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun joinLobby(userId: String, lobbyCode: String) {
        val message = mapOf(
            "type" to "lobby.join",
            "payload" to mapOf(
                "userId" to userId,
                "lobbyCode" to lobbyCode
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun setReady(userId: String, lobbyId: String, isReady: Boolean) {
        val message = mapOf(
            "type" to "lobby.ready",
            "payload" to mapOf(
                "userId" to userId,
                "lobbyId" to lobbyId,
                "isReady" to isReady
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun startGame(hostId: String, lobbyId: String) {
        val message = mapOf(
            "type" to "lobby.start",
            "payload" to mapOf(
                "hostId" to hostId,
                "lobbyId" to lobbyId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun leaveLobby(userId: String, lobbyId: String) {
        val message = mapOf(
            "type" to "lobby.leave",
            "payload" to mapOf(
                "userId" to userId,
                "lobbyId" to lobbyId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun kickPlayer(hostId: String, lobbyId: String, targetUserId: String) {
        val message = mapOf(
            "type" to "lobby.kick",
            "payload" to mapOf(
                "hostId" to hostId,
                "lobbyId" to lobbyId,
                "targetUserId" to targetUserId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun requestLobbyList() {
        val message = mapOf(
            "type" to "lobby.list.sync",
            "payload" to emptyMap<String, Any>()
        )
        webSocketManager.sendMessage(message)
    }
    
    fun observeLobbyEvents(): Flow<LobbyEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("lobby.") == true
            }
            .map { message ->
                parseLobbyEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseLobbyEvent(message: Map<String, Any>): LobbyEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "lobby.created" -> {
                val settings = payload["gameSettings"] as? Map<String, Any> ?: emptyMap()
                LobbyEvent.LobbyCreated(
                    lobbyId = payload["lobbyId"] as? String ?: "",
                    lobbyCode = payload["lobbyCode"] as? String ?: "",
                    hostId = payload["hostId"] as? String ?: "",
                    maxPlayers = (payload["maxPlayers"] as? Double)?.toInt() ?: 4,
                    difficulty = settings["difficulty"] as? String ?: "medium",
                    category = settings["category"] as? String ?: "general"
                )
            }
            "lobby.player.joined" -> {
                val playersList = payload["players"] as? List<Map<String, Any>> ?: emptyList()
                val players = playersList.map { p ->
                    LobbyPlayer(
                        userId = p["userId"] as? String ?: "",
                        username = p["username"] as? String ?: "",
                        level = (p["level"] as? Double)?.toInt() ?: 1,
                        isReady = p["isReady"] as? Boolean ?: false,
                        isHost = p["isHost"] as? Boolean ?: false
                    )
                }
                LobbyEvent.PlayerJoined(
                    lobbyId = payload["lobbyId"] as? String ?: "",
                    players = players
                )
            }
            "lobby.player.ready" -> {
                LobbyEvent.PlayerReady(
                    userId = payload["userId"] as? String ?: "",
                    isReady = payload["isReady"] as? Boolean ?: false,
                    allPlayersReady = payload["allPlayersReady"] as? Boolean ?: false
                )
            }
            "lobby.game.starting" -> {
                LobbyEvent.GameStarting(
                    lobbyId = payload["lobbyId"] as? String ?: "",
                    countdown = (payload["countdown"] as? Double)?.toInt() ?: 3
                )
            }
            "lobby.list.data" -> {
                val lobbiesList = payload["lobbies"] as? List<Map<String, Any>> ?: emptyList()
                val lobbies = lobbiesList.map { l ->
                    DataModels.LobbyInfo(
                        lobbyId = l["lobbyId"] as? String ?: "",
                        lobbyName = l["lobbyName"] as? String ?: "",
                        hostId = l["hostId"] as? String ?: "",
                        hostName = l["hostName"] as? String ?: "",
                        currentPlayers = (l["currentPlayers"] as? Double)?.toInt() ?: 0,
                        maxPlayers = (l["maxPlayers"] as? Double)?.toInt() ?: 4,
                        isPrivate = l["isPrivate"] as? Boolean ?: false,
                        status = l["status"] as? String ?: "waiting"
                    )
                }
                LobbyEvent.LobbyListData(lobbies = lobbies)
            }
            else -> LobbyEvent.Unknown
        }
    }
}

data class LobbyPlayer(
    val userId: String,
    val username: String,
    val level: Int,
    val isReady: Boolean,
    val isHost: Boolean
)

sealed class LobbyEvent {
    data class LobbyCreated(
        val lobbyId: String,
        val lobbyCode: String,
        val hostId: String,
        val maxPlayers: Int,
        val difficulty: String,
        val category: String
    ) : LobbyEvent()
    
    data class PlayerJoined(
        val lobbyId: String,
        val players: List<LobbyPlayer>
    ) : LobbyEvent()
    
    data class PlayerReady(
        val userId: String,
        val isReady: Boolean,
        val allPlayersReady: Boolean
    ) : LobbyEvent()
    
    data class GameStarting(
        val lobbyId: String,
        val countdown: Int
    ) : LobbyEvent()
    
    data class LobbyListData(
        val lobbies: List<DataModels.LobbyInfo>
    ) : LobbyEvent()
    
    data class GameStarted(
        val matchId: String
    ) : LobbyEvent()
    
    object Unknown : LobbyEvent()
}
