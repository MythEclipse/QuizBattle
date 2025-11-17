package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.ConfettiBurst
import com.mytheclipse.quizbattle.ui.components.DimmedOverlay
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import com.mytheclipse.quizbattle.viewmodel.MatchmakingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakingScreen(
    onNavigateBack: () -> Unit,
    onMatchFound: (String) -> Unit,
    viewModel: MatchmakingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = rememberHapticFeedback()
    var showCelebration by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.matchFound) {
        if (state.matchFound != null) {
            // Play a short celebration before navigating
            showCelebration = true
            haptic.success()
            kotlinx.coroutines.delay(900)
            showCelebration = false
            onMatchFound(state.matchFound?.matchId ?: "")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finding Match") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated searching indicator
                if (state.isSearching) {
                    SearchingAnimation()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Searching for opponent...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (state.queuePosition != null && state.queuePosition!! > 0) {
                        Text(
                            text = "Position in queue: ${state.queuePosition}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = {
                            haptic.mediumTap()
                            viewModel.cancelMatchmaking()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                } else {
                    Text(
                        text = "Ready to battle?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.findMatch() },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Start Matchmaking",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (showCelebration) {
                DimmedOverlay()
                ConfettiBurst(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun SearchingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "searching")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.3f))
        )
        
        // Middle circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.5f))
        )
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
