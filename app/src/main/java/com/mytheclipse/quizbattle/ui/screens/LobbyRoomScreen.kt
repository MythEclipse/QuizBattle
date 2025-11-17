package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.viewmodel.LobbyViewModel
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.LoadingState
import com.mytheclipse.quizbattle.utils.androidViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyRoomScreen(
    lobbyId: String,
    onNavigateBack: () -> Unit,
    onGameStarting: (String) -> Unit,
    viewModel: LobbyViewModel = androidViewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = rememberHapticFeedback()
    
    LaunchedEffect(lobbyId) {
        viewModel.observeLobbyEvents()
    }
    
    LaunchedEffect(state.gameStarting) {
        if (state.gameStarting) {
            onGameStarting(state.matchId ?: "")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveLobby()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Terjadi kesalahan di lobby",
                        onRetry = {
                            haptic.mediumTap()
                            viewModel.observeLobbyEvents()
                        }
                    )
                }
                state.lobbyCode == null && state.players.isEmpty() && !state.gameStarting -> {
                    LoadingState(message = "Menghubungkan ke lobby...")
                }
                else -> {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Lobby info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Lobby Code",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.lobbyCode ?: "------",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { /* Copy to clipboard */ }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Players section
            Text(
                text = "Players (${state.players.size}/4)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.players) { player ->
                    PlayerItem(
                        player = player,
                        isHost = state.isHost,
                        onKick = { viewModel.kickPlayer(player.userId) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!state.isHost) {
                    Button(
                        onClick = { 
                            haptic.mediumTap()
                            viewModel.setReady(!state.isReady) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isReady) 
                                Color(0xFF4CAF50) 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (state.isReady) Icons.Default.Check else Icons.Default.HourglassEmpty,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.isReady) "Ready" else "Not Ready")
                    }
                } else {
                    Button(
                        onClick = { 
                            haptic.mediumTap()
                            viewModel.startGame() 
                        },
                        modifier = Modifier.weight(1f),
                        enabled = state.players.all { it.isReady || it.isHost }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Game")
                    }
                }
                
                OutlinedButton(
                    onClick = {
                        haptic.lightTap()
                        viewModel.leaveLobby()
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leave")
                }
            }
        }
                }
            }
        }
    }
}

@Composable
fun PlayerItem(
    player: com.mytheclipse.quizbattle.data.repository.DataModels.PlayerInfo,
    isHost: Boolean,
    onKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (player.isHost) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "HOST",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = if (player.isReady || player.isHost) "Ready" else "Not Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (player.isReady || player.isHost) 
                            Color(0xFF4CAF50) 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (isHost && !player.isHost) {
                IconButton(onClick = onKick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Kick player",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
