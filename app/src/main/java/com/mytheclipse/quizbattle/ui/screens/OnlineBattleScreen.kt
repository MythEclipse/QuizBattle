package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import com.mytheclipse.quizbattle.viewmodel.OnlineGameViewModel
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.LoadingState

@Composable
fun OnlineBattleScreen(
    matchId: String,
    onGameFinished: () -> Unit,
    viewModel: OnlineGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = rememberHapticFeedback()
    
    LaunchedEffect(matchId) {
        viewModel.connectToMatch(matchId)
    }
    
    LaunchedEffect(state.gameFinished) {
        if (state.gameFinished) {
            kotlinx.coroutines.delay(3000)
            onGameFinished()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player score
                ScoreCard(
                    label = "You",
                    score = state.playerScore,
                    isPlayer = true
                )
                
                // Timer
                TimerDisplay(timeLeft = state.timeLeft)
                
                // Opponent score
                ScoreCard(
                    label = "Opponent",
                    score = state.opponentScore,
                    isPlayer = false
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Question section
            if (state.currentQuestion != null) {
                QuestionSection(
                    question = state.currentQuestion!!,
                    onAnswerSelected = { answer ->
                        haptic.mediumTap()
                        viewModel.submitAnswer(answer)
                    },
                    isAnswered = state.isAnswered
                )
            } else if (state.error != null) {
                ErrorState(
                    message = state.error ?: "Koneksi terputus",
                    onRetry = { viewModel.connectToMatch(matchId) }
                )
            } else {
                LoadingState("Menunggu pertanyaan...")
            }
        }
        
        // Game finished overlay
        AnimatedVisibility(
            visible = state.gameFinished,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            GameFinishedOverlay(
                isVictory = state.isVictory,
                playerScore = state.playerScore,
                opponentScore = state.opponentScore
            )
        }
    }
}

@Composable
fun ScoreCard(
    label: String,
    score: Int,
    isPlayer: Boolean
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayer) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimerDisplay(timeLeft: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                timeLeft > 10 -> MaterialTheme.colorScheme.surface
                timeLeft > 5 -> Color(0xFFFFA726)
                else -> MaterialTheme.colorScheme.error
            }
        )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeLeft.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (timeLeft <= 5) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuestionSection(
    question: com.mytheclipse.quizbattle.data.repository.DataModels.Question,
    onAnswerSelected: (String) -> Unit,
    isAnswered: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Question text
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Answer options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                AnswerButton(
                    text = option,
                    onClick = { onAnswerSelected(option) },
                    enabled = !isAnswered
                )
            }
        }
    }
}

@Composable
fun AnswerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun GameFinishedOverlay(
    isVictory: Boolean,
    playerScore: Int,
    opponentScore: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVictory) "Victory!" else "Defeat",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isVictory) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Score", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = playerScore.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = opponentScore.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Returning to menu...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
