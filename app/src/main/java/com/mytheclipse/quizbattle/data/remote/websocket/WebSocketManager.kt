package com.mytheclipse.quizbattle.data.remote.websocket

import android.util.Log
import com.google.gson.Gson
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.utils.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager {
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _messages = MutableSharedFlow<Map<String, Any>>(
        replay = 50,
        extraBufferCapacity = 50
    )
    val messages: SharedFlow<Map<String, Any>> = _messages
    
    // Message queue for offline messages
    private val messageQueue = mutableListOf<Map<String, Any>>()
    private val maxQueueSize = 50
    
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    fun connect(userId: String, token: String, username: String, deviceId: String) {
        val currentState = _connectionState.value
        if (currentState is ConnectionState.Connected || currentState is ConnectionState.Connecting) {
            return
        }
        
        AppLogger.WebSocket.connecting(ApiConfig.WEBSOCKET_URL)
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(ApiConfig.WEBSOCKET_URL)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                AppLogger.WebSocket.connected()
                _connectionState.value = ConnectionState.Connected
                reconnectAttempts = 0
                
                // Send auth message with username and deviceId
                val authMessage = mapOf(
                    "type" to "auth:connect",
                    "payload" to mapOf(
                        "userId" to userId,
                        "token" to token,
                        "username" to username,
                        "deviceId" to deviceId
                    )
                )
                sendMessage(authMessage)

                if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "onOpen: auth message sent for user=$userId")
                
                // Send queued messages
                sendQueuedMessages()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val message = gson.fromJson(text, Map::class.java) as Map<String, Any>
                    val type = message["type"] as? String ?: "unknown"
                    AppLogger.WebSocket.messageReceived(type)
                    scope.launch {
                        _messages.emit(message)
                    }
                } catch (e: Exception) {
                    AppLogger.WebSocket.error("Failed to parse message", e)
                }
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                AppLogger.WebSocket.disconnected("code=$code, reason=$reason")
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.Disconnected
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                AppLogger.WebSocket.error("Connection failed", t)
                _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
                attemptReconnect(userId, token, username, deviceId)
            }
        })
    }
    
    private fun attemptReconnect(userId: String, token: String, username: String, deviceId: String) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            AppLogger.WebSocket.error("Max reconnection attempts reached ($maxReconnectAttempts)")
            _connectionState.value = ConnectionState.Error("Max reconnection attempts reached")
            return
        }
        
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(2000L * (reconnectAttempts + 1)) // Exponential backoff
            reconnectAttempts++
            AppLogger.WebSocket.reconnecting(reconnectAttempts)
            connect(userId, token, username, deviceId)
        }
    }
    
    fun sendMessage(message: Map<String, Any>) {
        try {
            val json = gson.toJson(message)
            val isConnected = _connectionState.value is ConnectionState.Connected
            val type = message["type"] as? String ?: "unknown"
            
            if (isConnected && webSocket != null) {
                AppLogger.WebSocket.messageSent(type)
                webSocket?.send(json)
            } else {
                // Queue message if not connected
                AppLogger.WebSocket.messageSent(type, mapOf("status" to "queued"))
                queueMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Queue message on error
            if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.e("WebSocket", "sendMessage exception; queueing: ${e.message}", e)
            queueMessage(message)
        }
    }
    
    private fun queueMessage(message: Map<String, Any>) {
        synchronized(messageQueue) {
            if (messageQueue.size < maxQueueSize) {
                messageQueue.add(message)
            } else {
                // Remove oldest message if queue is full
                messageQueue.removeAt(0)
                messageQueue.add(message)
            }
        }
    }
    
    private fun sendQueuedMessages() {
        synchronized(messageQueue) {
            if (messageQueue.isNotEmpty()) {
                val messagesToSend = messageQueue.toList()
                messageQueue.clear()
                
                scope.launch {
                    messagesToSend.forEach { message ->
                        delay(100) // Small delay between messages
                        try {
                            val json = gson.toJson(message)
                            if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "sendQueuedMessages sending: $json")
                            webSocket?.send(json)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.e("WebSocket", "sendQueuedMessages error: ${e.message}", e)
                            // Re-queue failed message
                            queueMessage(message)
                        }
                    }
                }
            }
        }
    }
    
    fun clearMessageQueue() {
        synchronized(messageQueue) {
            messageQueue.clear()
        }
    }
    
    fun getQueuedMessageCount(): Int {
        synchronized(messageQueue) {
            return messageQueue.size
        }
    }
    
    fun disconnect() {
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    fun sendPing(userId: String) {
        val pingMessage = mapOf(
            "type" to "connection.ping",
            "payload" to mapOf("userId" to userId)
        )
        sendMessage(pingMessage)
    }
    
    /**
     * Send a typed message with JSONObject payload
     */
    fun send(type: String, payload: org.json.JSONObject) {
        try {
            val payloadMap = mutableMapOf<String, Any>()
            payload.keys().forEach { key ->
                val value = payload.get(key)
                if (value is org.json.JSONObject) {
                    val nestedMap = mutableMapOf<String, Any>()
                    value.keys().forEach { nestedKey ->
                        nestedMap[nestedKey] = value.get(nestedKey)
                    }
                    payloadMap[key] = nestedMap
                } else {
                    payloadMap[key] = value
                }
            }
            val message = mapOf(
                "type" to type,
                "payload" to payloadMap
            )
            sendMessage(message)
        } catch (e: Exception) {
            if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.e("WebSocket", "send error: ${e.message}", e)
        }
    }
    
    /**
     * Send a typed message with Map payload
     */
    fun send(type: String, payload: Map<String, Any>) {
        val message = mapOf(
            "type" to type,
            "payload" to payload
        )
        sendMessage(message)
    }
    
    companion object {
        @Volatile
        private var instance: WebSocketManager? = null
        
        fun getInstance(): WebSocketManager {
            return instance ?: synchronized(this) {
                instance ?: WebSocketManager().also { instance = it }
            }
        }
    }
}
