package com.mytheclipse.quizbattle.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized Logger for QuizBattle
 * Provides consistent logging format across the app
 */
object AppLogger {
    
    private const val TAG_PREFIX = "QuizBattle"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    private fun formatMessage(tag: String, message: String, context: Map<String, Any?>? = null): String {
        val timestamp = dateFormat.format(Date())
        val contextStr = context?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
        return if (contextStr.isNotEmpty()) {
            "[$timestamp] [$contextStr] $message"
        } else {
            "[$timestamp] $message"
        }
    }
    
    // Auth Logger
    object Auth {
        private const val TAG = "$TAG_PREFIX/Auth"
        
        fun loginAttempt(email: String) {
            Log.i(TAG, formatMessage(TAG, "Login attempt for $email"))
        }
        
        fun loginSuccess(userId: String, email: String) {
            Log.i(TAG, formatMessage(TAG, "Login successful for $email", mapOf("userId" to userId)))
        }
        
        fun loginFailed(email: String, reason: String) {
            Log.w(TAG, formatMessage(TAG, "Login failed for $email: $reason"))
        }
        
        fun registerAttempt(email: String) {
            Log.i(TAG, formatMessage(TAG, "Registration attempt for $email"))
        }
        
        fun registerSuccess(userId: String, email: String) {
            Log.i(TAG, formatMessage(TAG, "Registration successful for $email", mapOf("userId" to userId)))
        }
        
        fun registerFailed(email: String, reason: String) {
            Log.w(TAG, formatMessage(TAG, "Registration failed for $email: $reason"))
        }
        
        fun tokenRefreshed(userId: String) {
            Log.d(TAG, formatMessage(TAG, "Token refreshed", mapOf("userId" to userId)))
        }
        
        fun logout(userId: String) {
            Log.i(TAG, formatMessage(TAG, "User logged out", mapOf("userId" to userId)))
        }
    }
    
    // WebSocket Logger
    object WebSocket {
        private const val TAG = "$TAG_PREFIX/WS"
        
        fun connecting(url: String) {
            Log.i(TAG, formatMessage(TAG, "Connecting to WebSocket: $url"))
        }
        
        fun connected(sessionId: String? = null) {
            Log.i(TAG, formatMessage(TAG, "WebSocket connected", sessionId?.let { mapOf("session" to it) }))
        }
        
        fun disconnected(reason: String? = null) {
            Log.i(TAG, formatMessage(TAG, "WebSocket disconnected${reason?.let { ": $it" } ?: ""}"))
        }
        
        fun messageSent(type: String, payload: Map<String, Any?>? = null) {
            Log.d(TAG, formatMessage(TAG, "Message sent: $type", payload))
        }
        
        fun messageReceived(type: String, payload: Map<String, Any?>? = null) {
            Log.d(TAG, formatMessage(TAG, "Message received: $type", payload))
        }
        
        fun error(error: String, throwable: Throwable? = null) {
            if (throwable != null) {
                Log.e(TAG, formatMessage(TAG, "WebSocket error: $error"), throwable)
            } else {
                Log.e(TAG, formatMessage(TAG, "WebSocket error: $error"))
            }
        }
        
        fun reconnecting(attempt: Int) {
            Log.i(TAG, formatMessage(TAG, "Reconnecting...", mapOf("attempt" to attempt)))
        }
    }
    
    // Friend Logger
    object Friend {
        private const val TAG = "$TAG_PREFIX/Friend"
        
        fun requestSent(targetUserId: String) {
            Log.i(TAG, formatMessage(TAG, "Friend request sent to $targetUserId"))
        }
        
        fun requestReceived(fromUserId: String, fromUsername: String) {
            Log.i(TAG, formatMessage(TAG, "Friend request received from $fromUsername", mapOf("userId" to fromUserId)))
        }
        
        fun requestAccepted(friendId: String) {
            Log.i(TAG, formatMessage(TAG, "Friend request accepted", mapOf("friendId" to friendId)))
        }
        
        fun requestRejected(requestId: String) {
            Log.i(TAG, formatMessage(TAG, "Friend request rejected", mapOf("requestId" to requestId)))
        }
        
        fun removed(friendId: String) {
            Log.i(TAG, formatMessage(TAG, "Friend removed", mapOf("friendId" to friendId)))
        }
        
        fun listFetched(count: Int) {
            Log.d(TAG, formatMessage(TAG, "Friend list fetched: $count friends"))
        }
        
        fun inviteSent(receiverId: String) {
            Log.i(TAG, formatMessage(TAG, "Match invite sent", mapOf("receiverId" to receiverId)))
        }
        
        fun inviteReceived(senderId: String) {
            Log.i(TAG, formatMessage(TAG, "Match invite received", mapOf("senderId" to senderId)))
        }
        
        fun error(action: String, throwable: Throwable) {
            Log.e(TAG, formatMessage(TAG, "Friend error: $action"), throwable)
        }
    }
    
    // Match/Game Logger
    object Match {
        private const val TAG = "$TAG_PREFIX/Match"
        
        fun searching(mode: String) {
            Log.i(TAG, formatMessage(TAG, "Searching for match: $mode"))
        }
        
        fun found(matchId: String, opponentName: String) {
            Log.i(TAG, formatMessage(TAG, "Match found: vs $opponentName", mapOf("matchId" to matchId)))
        }
        
        fun started(matchId: String) {
            Log.i(TAG, formatMessage(TAG, "Match started", mapOf("matchId" to matchId)))
        }
        
        fun ended(matchId: String, won: Boolean, score: Int) {
            Log.i(TAG, formatMessage(TAG, "Match ended: ${if (won) "WON" else "LOST"}", 
                mapOf("matchId" to matchId, "score" to score)))
        }
        
        fun answerSubmitted(questionIndex: Int, correct: Boolean, timeMs: Long) {
            Log.d(TAG, formatMessage(TAG, "Answer submitted: ${if (correct) "correct" else "incorrect"}", 
                mapOf("question" to questionIndex, "timeMs" to timeMs)))
        }
        
        fun questionReceived(questionIndex: Int, totalQuestions: Int) {
            Log.d(TAG, formatMessage(TAG, "Question received: $questionIndex/$totalQuestions"))
        }
        
        fun opponentAnswered(correct: Boolean) {
            Log.d(TAG, formatMessage(TAG, "Opponent answered: ${if (correct) "correct" else "incorrect"}"))
        }
        
        fun cancelled(reason: String) {
            Log.i(TAG, formatMessage(TAG, "Match cancelled: $reason"))
        }
        
        fun error(action: String, throwable: Throwable) {
            Log.e(TAG, formatMessage(TAG, "Match error: $action"), throwable)
        }
    }
    
    // Network Logger
    object Network {
        private const val TAG = "$TAG_PREFIX/Network"
        
        fun apiRequest(method: String, endpoint: String) {
            Log.d(TAG, formatMessage(TAG, "API Request: $method $endpoint"))
        }
        
        fun apiResponse(endpoint: String, statusCode: Int, durationMs: Long) {
            Log.d(TAG, formatMessage(TAG, "API Response: $endpoint", 
                mapOf("status" to statusCode, "duration" to "${durationMs}ms")))
        }
        
        fun apiError(endpoint: String, error: String, throwable: Throwable? = null) {
            if (throwable != null) {
                Log.e(TAG, formatMessage(TAG, "API Error: $endpoint - $error"), throwable)
            } else {
                Log.e(TAG, formatMessage(TAG, "API Error: $endpoint - $error"))
            }
        }
        
        fun connectionStateChanged(isConnected: Boolean) {
            Log.i(TAG, formatMessage(TAG, "Connection state: ${if (isConnected) "ONLINE" else "OFFLINE"}"))
        }
    }
    
    // UI Logger
    object UI {
        private const val TAG = "$TAG_PREFIX/UI"
        
        fun screenOpened(screenName: String, params: Map<String, Any?>? = null) {
            Log.d(TAG, formatMessage(TAG, "Screen opened: $screenName", params))
        }
        
        fun screenClosed(screenName: String) {
            Log.d(TAG, formatMessage(TAG, "Screen closed: $screenName"))
        }
        
        fun buttonClicked(buttonName: String, context: Map<String, Any?>? = null) {
            Log.d(TAG, formatMessage(TAG, "Button clicked: $buttonName", context))
        }
        
        fun navigationEvent(from: String, to: String) {
            Log.d(TAG, formatMessage(TAG, "Navigation: $from -> $to"))
        }
        
        fun error(message: String, throwable: Throwable? = null) {
            if (throwable != null) {
                Log.e(TAG, formatMessage(TAG, "UI Error: $message"), throwable)
            } else {
                Log.e(TAG, formatMessage(TAG, "UI Error: $message"))
            }
        }
    }
    
    // Game Logic Logger
    object Game {
        private const val TAG = "$TAG_PREFIX/Game"
        
        fun initialized(mode: String) {
            Log.i(TAG, formatMessage(TAG, "Game initialized: $mode"))
        }
        
        fun stateChanged(oldState: String, newState: String) {
            Log.d(TAG, formatMessage(TAG, "Game state: $oldState -> $newState"))
        }
        
        fun healthChanged(oldHealth: Int, newHealth: Int, reason: String) {
            Log.d(TAG, formatMessage(TAG, "Health changed: $oldHealth -> $newHealth ($reason)"))
        }
        
        fun scoreUpdated(score: Int, delta: Int) {
            Log.d(TAG, formatMessage(TAG, "Score updated: $score (${if (delta > 0) "+" else ""}$delta)"))
        }
        
        fun powerUpActivated(powerUpType: String) {
            Log.i(TAG, formatMessage(TAG, "Power-up activated: $powerUpType"))
        }
        
        fun error(action: String, throwable: Throwable) {
            Log.e(TAG, formatMessage(TAG, "Game error: $action"), throwable)
        }
    }
    
    // Database Logger
    object Database {
        private const val TAG = "$TAG_PREFIX/Database"
        
        fun queryExecuted(query: String, resultCount: Int, durationMs: Long) {
            Log.d(TAG, formatMessage(TAG, "Query executed: $resultCount results in ${durationMs}ms"))
        }
        
        fun insertSuccess(table: String, count: Int) {
            Log.d(TAG, formatMessage(TAG, "Inserted $count row(s) into $table"))
        }
        
        fun updateSuccess(table: String, count: Int) {
            Log.d(TAG, formatMessage(TAG, "Updated $count row(s) in $table"))
        }
        
        fun deleteSuccess(table: String, count: Int) {
            Log.d(TAG, formatMessage(TAG, "Deleted $count row(s) from $table"))
        }
        
        fun error(operation: String, throwable: Throwable) {
            Log.e(TAG, formatMessage(TAG, "Database error: $operation"), throwable)
        }
    }
    
    // Utility function for general logging
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val fullTag = "$TAG_PREFIX/$tag"
        val formattedMsg = formatMessage(fullTag, message)
        
        when (level) {
            LogLevel.DEBUG -> Log.d(fullTag, formattedMsg, throwable)
            LogLevel.INFO -> Log.i(fullTag, formattedMsg, throwable)
            LogLevel.WARN -> Log.w(fullTag, formattedMsg, throwable)
            LogLevel.ERROR -> Log.e(fullTag, formattedMsg, throwable)
        }
    }
}
