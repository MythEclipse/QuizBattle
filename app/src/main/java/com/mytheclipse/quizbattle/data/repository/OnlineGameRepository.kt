package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import android.util.Log

class OnlineGameRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun observeGameEvents(): Flow<GameEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("game.") == true || type == "lobby.game.starting"
            }
            .map { message ->
                Log.d("OnlineGameRepo", "Received message: ${message["type"]}")
                parseGameEvent(message)
            }
    }
    
    private fun parseGameEvent(message: Map<String, Any>): GameEvent {
        val type = message["type"] as? String ?: ""
        Log.d("OnlineGameRepo", "Parsing event type: $type")
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
            "game.questions.all" -> {
                @Suppress("UNCHECKED_CAST")
                val questionsList = payload["questions"] as? List<Map<String, Any>> ?: emptyList()
                val questions = questionsList.map { qMap ->
                    @Suppress("UNCHECKED_CAST")
                    val answersRaw = qMap["answers"] as? List<Any> ?: emptyList()
                    DataModels.Question(
                        questionId = qMap["id"] as? String ?: "",
                        questionText = qMap["text"] as? String ?: "",
                        options = answersRaw.map { it.toString() },
                        category = qMap["category"] as? String
                    )
                }
                GameEvent.AllQuestions(
                    matchId = payload["matchId"] as? String ?: "",
                    questions = questions
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
                    timeBonus = 0, // Backend doesn't send timeBonus separately
                    playerHealth = (payload["playerHealth"] as? Double)?.toInt() ?: 100,
                    opponentHealth = (payload["opponentHealth"] as? Double)?.toInt() ?: 100
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
                
                @Suppress("UNCHECKED_CAST")
                val rewardsPayload = payload["rewards"] as? Map<String, Any> ?: emptyMap()
                
                @Suppress("UNCHECKED_CAST")
                val wRewards = rewardsPayload["winner"] as? Map<String, Number> ?: emptyMap()
                val winnerRewards = wRewards.mapValues { it.value.toInt() }
                
                @Suppress("UNCHECKED_CAST")
                val lRewards = rewardsPayload["loser"] as? Map<String, Number> ?: emptyMap()
                val loserRewards = lRewards.mapValues { it.value.toInt() }
                
                GameEvent.GameFinished(
                    matchId = payload["matchId"] as? String ?: "",
                    winner = winnerId,
                    playerScore = (winnerMap["finalScore"] as? Double)?.toInt() ?: 0,
                    playerCorrect = (winnerMap["correctAnswers"] as? Double)?.toInt() ?: 0,
                    opponentScore = (loserMap["finalScore"] as? Double)?.toInt() ?: 0,
                    opponentCorrect = (loserMap["correctAnswers"] as? Double)?.toInt() ?: 0,
                    winnerRewards = winnerRewards,
                    loserRewards = loserRewards
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
            // Handle real-time health updates
            "game.battle.update" -> {
                GameEvent.BattleUpdate(
                    matchId = payload["matchId"] as? String ?: "",
                    playerHealth = (payload["player1Health"] as? Double)?.toInt() ?: 100,
                    opponentHealth = (payload["player2Health"] as? Double)?.toInt() ?: 100
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
    
    data class AllQuestions(
        val matchId: String,
        val questions: List<DataModels.Question>
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
        val timeBonus: Int,
        val playerHealth: Int,  // Updated health after answer
        val opponentHealth: Int  // Opponent health from player's perspective
    ) : GameEvent()
    
    data class GameStarting(val countdown: Int) : GameEvent()
    
    data class GameFinished(
        val matchId: String,
        val winner: String,
        val playerScore: Int,
        val playerCorrect: Int,
        val opponentScore: Int,

        val opponentCorrect: Int,
        val winnerRewards: Map<String, Int> = emptyMap(),
        val loserRewards: Map<String, Int> = emptyMap()
    ) : GameEvent()
    
    data class OpponentAnswered(
        val isCorrect: Boolean,
        val animation: String
    ) : GameEvent()
    
    data class BattleUpdate(
        val matchId: String,
        val playerHealth: Int,
        val opponentHealth: Int
    ) : GameEvent()
    
    object OpponentDisconnected : GameEvent()
    object Unknown : GameEvent()
}
