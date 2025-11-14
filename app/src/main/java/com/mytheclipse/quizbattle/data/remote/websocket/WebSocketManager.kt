package com.mytheclipse.quizbattle.data.remote.websocket

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
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val message = gson.fromJson(text, Map::class.java) as Map<String, Any>
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
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
                attemptReconnect(userId, token)
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
            webSocket?.send(json)
        } catch (e: Exception) {
            e.printStackTrace()
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
