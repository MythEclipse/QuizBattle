package com.mytheclipse.quizbattle.data.remote.websocket

import android.util.Log
import com.google.gson.Gson
import com.mytheclipse.quizbattle.data.remote.ApiConfig
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
    
    private val _messages = MutableSharedFlow<Map<String, Any>>()
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
    
    fun connect(userId: String, token: String) {
        if (_connectionState.value is ConnectionState.Connected) {
            return
        }
        
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(ApiConfig.WEBSOCKET_URL)
            .build()

        if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "Connecting to ${ApiConfig.WEBSOCKET_URL} (user=$userId)")
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
                reconnectAttempts = 0
                
                // Send auth message
                val authMessage = mapOf(
                    "type" to "auth:connect",
                    "payload" to mapOf(
                        "userId" to userId,
                        "token" to token
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
                    if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "onMessage: $text")
                    scope.launch {
                        _messages.emit(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.Disconnected
                if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "onClosing: code=$code reason=$reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
                attemptReconnect(userId, token)
                if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.e("WebSocket", "onFailure: ${t.message}", t)
            }
        })
    }
    
    private fun attemptReconnect(userId: String, token: String) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            _connectionState.value = ConnectionState.Error("Max reconnection attempts reached")
            return
        }
        
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(2000L * (reconnectAttempts + 1)) // Exponential backoff
            reconnectAttempts++
            connect(userId, token)
        }
    }
    
    fun sendMessage(message: Map<String, Any>) {
        try {
            val json = gson.toJson(message)
            val isConnected = _connectionState.value is ConnectionState.Connected
            
            if (isConnected && webSocket != null) {
                if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "sendMessage sending: $json")
                webSocket?.send(json)
            } else {
                // Queue message if not connected
                if (com.mytheclipse.quizbattle.BuildConfig.DEBUG) Log.d("WebSocket", "sendMessage queueing (not connected): $json")
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
