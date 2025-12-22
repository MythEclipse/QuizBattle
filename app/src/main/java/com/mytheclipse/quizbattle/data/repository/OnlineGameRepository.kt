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
            "game.started" -> {
                @Suppress("UNCHECKED_CAST")
                val gameState = payload["gameState"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val playersRaw = payload["players"] as? List<Map<String, Any>> ?: emptyList()
                val players = playersRaw.map { p ->
                    GamePlayer(
                        userId = p["userId"] as? String ?: "",
                        username = p["username"] as? String ?: "",
                        position = p["position"] as? String ?: "left"
                    )
                }
                GameEvent.GameStarted(
                    matchId = payload["matchId"] as? String ?: "",
                    totalQuestions = (gameState["totalQuestions"] as? Double)?.toInt() ?: 10,
                    timePerQuestion = (gameState["timePerQuestion"] as? Double)?.toInt() ?: 30,
                    players = players
                )
            }
            "game.question.new" -> {
                @Suppress("UNCHECKED_CAST")
                val questionMap = payload["question"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val answersRaw = questionMap["answers"] as? List<Any> ?: emptyList()
                val answers = answersRaw.map { it.toString() }
                
                GameEvent.QuestionNew(
                    matchId = payload["matchId"] as? String ?: "",
                    questionIndex = (payload["questionIndex"] as? Double)?.toInt() ?: 0,
                    question = DataModels.Question(
                        questionId = questionMap["id"] as? String ?: "",
                        questionText = questionMap["text"] as? String ?: "",
                        options = answers,
                        category = questionMap["category"] as? String
                    ),
                    timeLimit = (payload["timeLimit"] as? Double)?.toInt() ?: 30
                )
            }
            // Backend sends "game.answer.received" not "game.answer.result"
            "game.answer.received" -> {
                GameEvent.AnswerResult(
                    isCorrect = payload["isCorrect"] as? Boolean ?: false,
                    correctAnswer = ((payload["correctAnswerIndex"] as? Double)?.toInt() ?: 0).toString(),
                    points = (payload["points"] as? Double)?.toInt() ?: 0,
                    timeBonus = 0 // Backend doesn't send timeBonus separately
                )
            }
            "lobby.game.starting" -> {
                GameEvent.GameStarting(
                    countdown = (payload["countdown"] as? Double)?.toInt() ?: 3
                )
            }
            // Backend sends "game.over" not "game.finished"
            "game.over" -> {
                @Suppress("UNCHECKED_CAST")
                val winnerMap = payload["winner"] as? Map<String, Any> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val loserMap = payload["loser"] as? Map<String, Any> ?: emptyMap()
                
                val winnerId = winnerMap["userId"] as? String ?: ""
                
                GameEvent.GameFinished(
                    matchId = payload["matchId"] as? String ?: "",
                    winner = winnerId,
                    playerScore = (winnerMap["finalScore"] as? Double)?.toInt() ?: 0,
                    playerCorrect = (winnerMap["correctAnswers"] as? Double)?.toInt() ?: 0,
                    opponentScore = (loserMap["finalScore"] as? Double)?.toInt() ?: 0,
                    opponentCorrect = (loserMap["correctAnswers"] as? Double)?.toInt() ?: 0
                )
            }
            "game.opponent.answered" -> {
                @Suppress("UNCHECKED_CAST")
                val opponentPayload = payload
                GameEvent.OpponentAnswered(
                    isCorrect = opponentPayload["isCorrect"] as? Boolean ?: false,
                    animation = opponentPayload["animation"] as? String ?: "hurt"
                )
            }
            // Backend sends "game.player.disconnected" not "game.opponent.disconnected"
            "game.player.disconnected" -> {
                GameEvent.OpponentDisconnected
            }
            // Handle real-time score/health updates
            "game.battle.update" -> {
                @Suppress("UNCHECKED_CAST")
                val gameState = payload["gameState"] as? Map<String, Any> ?: emptyMap()
                GameEvent.BattleUpdate(
                    matchId = payload["matchId"] as? String ?: "",
                    playerScore = (gameState["playerScore"] as? Double)?.toInt() ?: 0,
                    opponentScore = (gameState["opponentScore"] as? Double)?.toInt() ?: 0,
                    playerHealth = (gameState["playerHealth"] as? Double)?.toInt() ?: 100,
                    opponentHealth = (gameState["opponentHealth"] as? Double)?.toInt() ?: 100
                )
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
    
    fun submitAnswer(
        userId: String, 
        matchId: String, 
        questionId: String, 
        questionIndex: Int,
        answerIndex: Int, 
        answerTimeMs: Int
    ) {
        val message = mapOf(
            "type" to "game.answer.submit",
            "payload" to mapOf(
                "userId" to userId,
                "matchId" to matchId,
                "questionId" to questionId,
                "questionIndex" to questionIndex,
                "answerIndex" to answerIndex,
                "answerTime" to answerTimeMs,
                "timestamp" to System.currentTimeMillis()
            )
        )
        webSocketManager.sendMessage(message)
    }
}

// Player info from game.started
data class GamePlayer(
    val userId: String,
    val username: String,
    val position: String // "left" or "right"
)

sealed class GameEvent {
    data class GameStarted(
        val matchId: String,
        val totalQuestions: Int,
        val timePerQuestion: Int,
        val players: List<GamePlayer> = emptyList()
    ) : GameEvent()
    
    data class QuestionNew(
        val matchId: String,
        val questionIndex: Int,
        val question: DataModels.Question,
        val timeLimit: Int
    ) : GameEvent()
    
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
    
    data class OpponentAnswered(
        val isCorrect: Boolean,
        val animation: String
    ) : GameEvent()
    
    data class BattleUpdate(
        val matchId: String,
        val playerScore: Int,
        val opponentScore: Int,
        val playerHealth: Int,
        val opponentHealth: Int
    ) : GameEvent()
    
    object OpponentDisconnected : GameEvent()
    object Unknown : GameEvent()
}
