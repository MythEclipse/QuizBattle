package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.utils.NetworkMonitor
import kotlinx.coroutines.delay

/**
 * Connection status banner that shows at the top of the screen
 */
@Composable
fun ConnectionStatusBanner(
    networkMonitor: NetworkMonitor,
    webSocketManager: WebSocketManager = WebSocketManager.getInstance()
) {
    val isNetworkAvailable by networkMonitor.isConnected.collectAsState(initial = true)
    val connectionState by webSocketManager.connectionState.collectAsState()
    val queuedMessageCount = remember { mutableStateOf(0) }
    
    // Update queued message count periodically
    LaunchedEffect(Unit) {
        while (true) {
            queuedMessageCount.value = webSocketManager.getQueuedMessageCount()
            delay(2000)
        }
    }
    
    // Determine what to show
    val shouldShowBanner = !isNetworkAvailable || 
                          connectionState is WebSocketManager.ConnectionState.Error ||
                          connectionState is WebSocketManager.ConnectionState.Connecting ||
                          queuedMessageCount.value > 0
    
    AnimatedVisibility(
        visible = shouldShowBanner,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        when {
            !isNetworkAvailable -> {
                OfflineBanner()
            }
            connectionState is WebSocketManager.ConnectionState.Connecting -> {
                ConnectingBanner()
            }
            connectionState is WebSocketManager.ConnectionState.Error -> {
                val error = (connectionState as WebSocketManager.ConnectionState.Error).message
                ErrorBanner(error)
            }
            queuedMessageCount.value > 0 -> {
                SyncingBanner(queuedMessageCount.value)
            }
        }
    }
}

@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFF6B6B),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Offline",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tidak ada koneksi internet",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConnectingBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "connecting")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFA726),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Connecting",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Menghubungkan...",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorBanner(errorMessage: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEF5350),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Error",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Koneksi bermasalah",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SyncingBanner(messageCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "syncing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF66BB6A).copy(alpha = alpha),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mengirim $messageCount pesan...",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Compact connection indicator for toolbar
 */
@Composable
fun ConnectionIndicator(
    networkMonitor: NetworkMonitor,
    modifier: Modifier = Modifier
) {
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    
    if (!isConnected) {
        Box(
            modifier = modifier
                .size(8.dp)
                .background(
                    color = Color.Red,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

/**
 * Bottom sheet showing connection details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDetailsSheet(
    onDismiss: () -> Unit,
    networkMonitor: NetworkMonitor,
    webSocketManager: WebSocketManager = WebSocketManager.getInstance()
) {
    val isNetworkAvailable by networkMonitor.isConnected.collectAsState(initial = true)
    val connectionState by webSocketManager.connectionState.collectAsState()
    val queuedMessageCount = remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            queuedMessageCount.value = webSocketManager.getQueuedMessageCount()
            delay(1000)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Status Koneksi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Network status
            ConnectionDetailItem(
                label = "Jaringan",
                value = if (isNetworkAvailable) "Terhubung" else "Tidak terhubung",
                color = if (isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // WebSocket status
            val wsStatus = when (connectionState) {
                is WebSocketManager.ConnectionState.Connected -> "Terhubung"
                is WebSocketManager.ConnectionState.Connecting -> "Menghubungkan..."
                is WebSocketManager.ConnectionState.Disconnected -> "Terputus"
                is WebSocketManager.ConnectionState.Error -> "Error"
            }
            val wsColor = when (connectionState) {
                is WebSocketManager.ConnectionState.Connected -> Color(0xFF4CAF50)
                is WebSocketManager.ConnectionState.Connecting -> Color(0xFFFFA726)
                else -> Color(0xFFF44336)
            }
            
            ConnectionDetailItem(
                label = "WebSocket",
                value = wsStatus,
                color = wsColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Queued messages
            ConnectionDetailItem(
                label = "Pesan Tertunda",
                value = "${queuedMessageCount.value} pesan",
                color = if (queuedMessageCount.value > 0) Color(0xFFFFA726) else Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectionDetailItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = color,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
