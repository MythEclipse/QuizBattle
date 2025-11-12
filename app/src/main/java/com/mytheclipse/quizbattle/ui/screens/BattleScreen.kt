package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.ui.components.AnimatedGoblin
import com.mytheclipse.quizbattle.ui.components.AnimatedKnight
import com.mytheclipse.quizbattle.ui.components.FantasyHealthBar
import com.mytheclipse.quizbattle.ui.components.FantasyTimerBar
import com.mytheclipse.quizbattle.ui.components.GoblinAnimation
import com.mytheclipse.quizbattle.ui.components.KnightAnimation
import com.mytheclipse.quizbattle.ui.components.QuizAnswerButton
import com.mytheclipse.quizbattle.ui.theme.*
import com.mytheclipse.quizbattle.viewmodel.BattleViewModel
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(
    onNavigateToResult: (Boolean) -> Unit, // true for victory, false for defeat
    viewModel: BattleViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Handle game over
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            delay(500)
            // Victory based on health: opponent HP = 0 or player HP > opponent HP
            val isVictory = if (state.opponentHealth <= 0 && state.playerHealth > 0) {
                true
            } else if (state.playerHealth <= 0 && state.opponentHealth > 0) {
                false
            } else {
                state.playerHealth > state.opponentHealth
            }
            onNavigateToResult(isVictory)
        }
    }

    // Timer effect - only when not answered
    LaunchedEffect(state.currentQuestionIndex, state.isAnswered) {
        if (!state.isAnswered) {
            var progress = 1f
            while (progress > 0 && !state.isAnswered) {
                delay(100)
                progress -= 0.01f
                viewModel.updateTimeProgress(progress.coerceAtLeast(0f))
            }
            // Time's up - only if still not answered
            if (!state.isAnswered) {
                viewModel.timeUp()
                delay(1500)
                viewModel.nextQuestion()
            }
        }
    }

    // Auto-advance after answering
    LaunchedEffect(state.isAnswered, state.currentQuestionIndex) {
        if (state.isAnswered) {
            delay(1500)
            viewModel.nextQuestion()
        }
    }

    // Show loading
    if (state.isLoading || state.questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = state.questions[state.currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Soal ${state.currentQuestionIndex + 1}/${state.questions.size}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            
            // Timer indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "⏱",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${(state.timeProgress * 10).toInt()}s",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (state.timeProgress < 0.3f) MaterialTheme.colorScheme.error else TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Define all animation states first
        val playerAnimation = when {
            state.playerHealth <= 0 -> KnightAnimation.DEAD
            state.playerTookDamage -> KnightAnimation.HURT
            state.playerAttacking -> KnightAnimation.ATTACK
            else -> KnightAnimation.IDLE
        }
        
        val enemyAnimation = when {
            state.opponentHealth <= 0 -> GoblinAnimation.DEAD
            state.opponentTookDamage -> GoblinAnimation.HURT
            state.playerTookDamage -> GoblinAnimation.ATTACK
            else -> GoblinAnimation.IDLE
        }
        
        // Knight positioning with animation
        val knightOffsetX by animateDpAsState(
            targetValue = when {
                state.playerAttacking -> 80.dp
                state.playerTookDamage -> (-20).dp
                else -> 0.dp
            },
            animationSpec = tween(durationMillis = 300),
            label = "knight_offset"
        )
        
        // Goblin positioning with animation
        val goblinOffsetX by animateDpAsState(
            targetValue = when {
                state.playerTookDamage -> (-80).dp
                state.opponentTookDamage -> 20.dp
                else -> 0.dp
            },
            animationSpec = tween(durationMillis = 300),
            label = "goblin_offset"
        )
        
        // White flash effect when hurt (more intense)
        val playerFlashAlpha by animateFloatAsState(
            targetValue = if (state.playerTookDamage) 1f else 0f,
            animationSpec = tween(durationMillis = 150),
            label = "player_flash"
        )
        
        val enemyFlashAlpha by animateFloatAsState(
            targetValue = if (state.opponentTookDamage) 1f else 0f,
            animationSpec = tween(durationMillis = 150),
            label = "enemy_flash"
        )
        
        // Screen shake effect
        val shakeOffsetX by animateDpAsState(
            targetValue = when {
                state.playerTookDamage -> if ((System.currentTimeMillis() / 50) % 2 == 0L) 8.dp else (-8).dp
                state.opponentTookDamage -> if ((System.currentTimeMillis() / 50) % 2 == 0L) (-8).dp else 8.dp
                else -> 0.dp
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            label = "shake"
        )

        // Battleground Arena
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C) // Dark battleground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .offset(x = shakeOffsetX) // Add shake to entire arena
            ) {
                
                // Knight on left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 32.dp, bottom = 16.dp)
                        .offset(x = knightOffsetX)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        AnimatedKnight(
                            animation = playerAnimation,
                            size = 180.dp,
                            flashAlpha = playerFlashAlpha * 0.8f
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "KNIGHT",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
                
                // Goblin on right - smaller size (it's a goblin, not a human!)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 80.dp, bottom = 16.dp) // Closer to knight
                        .offset(x = goblinOffsetX)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        AnimatedGoblin(
                            animation = enemyAnimation,
                            size = 120.dp, // Smaller - goblins are smaller than knights
                            flashAlpha = enemyFlashAlpha * 0.8f
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "GOBLIN",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Health bars below battleground
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player health
            Column(modifier = Modifier.weight(1f)) {
                FantasyHealthBar(
                    currentHealth = state.playerHealth,
                    maxHealth = 100,
                    label = "KNIGHT HP"
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Enemy health
            Column(modifier = Modifier.weight(1f)) {
                FantasyHealthBar(
                    currentHealth = state.opponentHealth,
                    maxHealth = 100,
                    label = "GOBLIN HP"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Timer progress bar with Fantasy style
        FantasyTimerBar(
            progress = state.timeProgress
        )

        
        Spacer(modifier = Modifier.height(16.dp))

        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, BorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp
                    ),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // Answer buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizAnswerButton(
                    text = currentQuestion.answers[0],
                    onClick = {
                        if (!state.isAnswered) {
                            viewModel.answerQuestion(0)
                        }
                    },
                    isSelected = state.selectedAnswerIndex == 0,
                    isCorrect = if (state.isAnswered && state.selectedAnswerIndex == 0) 0 == currentQuestion.correctAnswerIndex else null,
                    enabled = !state.isAnswered,
                    modifier = Modifier.weight(1f)
                )

                QuizAnswerButton(
                    text = currentQuestion.answers[1],
                    onClick = {
                        if (!state.isAnswered) {
                            viewModel.answerQuestion(1)
                        }
                    },
                    isSelected = state.selectedAnswerIndex == 1,
                    isCorrect = if (state.isAnswered && state.selectedAnswerIndex == 1) 1 == currentQuestion.correctAnswerIndex else null,
                    enabled = !state.isAnswered,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizAnswerButton(
                    text = currentQuestion.answers[2],
                    onClick = {
                        if (!state.isAnswered) {
                            viewModel.answerQuestion(2)
                        }
                    },
                    isSelected = state.selectedAnswerIndex == 2,
                    isCorrect = if (state.isAnswered && state.selectedAnswerIndex == 2) 2 == currentQuestion.correctAnswerIndex else null,
                    enabled = !state.isAnswered,
                    modifier = Modifier.weight(1f)
                )

                QuizAnswerButton(
                    text = currentQuestion.answers[3],
                    onClick = {
                        if (!state.isAnswered) {
                            viewModel.answerQuestion(3)
                        }
                    },
                    isSelected = state.selectedAnswerIndex == 3,
                    isCorrect = if (state.isAnswered && state.selectedAnswerIndex == 3) 3 == currentQuestion.correctAnswerIndex else null,
                    enabled = !state.isAnswered,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

