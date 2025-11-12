package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.R
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
            
            // Result card with avatar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Winner avatar
                    Image(
                        painter = painterResource(
                            id = if (isVictory) R.drawable.player_avatar else R.drawable.bot_avatar
                        ),
                        contentDescription = if (isVictory) "Player Won" else "Bot Won",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isVictory) "You Won!" else "Bot Wins!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isVictory) "Selamat, kamu menang!" else "Better luck next time!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
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
