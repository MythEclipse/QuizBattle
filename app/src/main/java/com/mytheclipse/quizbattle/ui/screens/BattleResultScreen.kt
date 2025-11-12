package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.ui.components.QuizBattleButton
import com.mytheclipse.quizbattle.ui.theme.*

@Composable
fun BattleResultScreen(
    isVictory: Boolean,
    onNavigateToMain: () -> Unit,
    onRematch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Result text with gradient
            if (isVictory) {
                Text(
                    text = "VICTORY",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GradientRedStart,
                                GradientBlueEnd
                            )
                        ),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Text(
                    text = "DEFEAT",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefeatRed
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Result card placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVictory) "You Won!" else "Better luck next time!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizBattleButton(
                    text = "Main Menu",
                    onClick = onNavigateToMain,
                    modifier = Modifier.weight(1f)
                )
                
                QuizBattleButton(
                    text = "Rematch",
                    onClick = onRematch,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
