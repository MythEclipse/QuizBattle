package com.mytheclipse.quizbattle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mytheclipse.quizbattle.R

@Composable
fun FantasyHealthBar(
    currentHealth: Int,
    maxHealth: Int = 100,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "$label: $currentHealth/$maxHealth",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Custom fantasy-style health bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progress = (currentHealth.toFloat() / maxHealth).coerceIn(0f, 1f)
                val barWidth = size.width
                val barHeight = size.height
                
                // Border/Frame (dark red/brown)
                drawRoundRect(
                    color = Color(0xFF5A1F1F),
                    topLeft = Offset(0f, 0f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Background (darker)
                drawRoundRect(
                    color = Color(0xFF2A0F0F),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(barWidth - 4.dp.toPx(), barHeight - 4.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                
                // Health fill with gradient effect
                if (progress > 0f) {
                    val fillWidth = (barWidth - 4.dp.toPx()) * progress
                    
                    // Main health color
                    val healthColor = when {
                        currentHealth > 60 -> Color(0xFFDC143C) // Crimson red
                        currentHealth > 30 -> Color(0xFFFF6347) // Tomato red
                        else -> Color(0xFF8B0000) // Dark red
                    }
                    
                    drawRoundRect(
                        color = healthColor,
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillWidth, barHeight - 4.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    
                    // Highlight effect on top
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillWidth, (barHeight - 4.dp.toPx()) * 0.4f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun FantasyManaBar(
    currentMana: Int,
    maxMana: Int = 100,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "$label: $currentMana/$maxMana",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progress = (currentMana.toFloat() / maxMana).coerceIn(0f, 1f)
                val barWidth = size.width
                val barHeight = size.height
                
                // Border/Frame (dark blue)
                drawRoundRect(
                    color = Color(0xFF1F1F5A),
                    topLeft = Offset(0f, 0f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Background (darker)
                drawRoundRect(
                    color = Color(0xFF0F0F2A),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(barWidth - 4.dp.toPx(), barHeight - 4.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                
                // Mana fill
                if (progress > 0f) {
                    val fillWidth = (barWidth - 4.dp.toPx()) * progress
                    
                    // Main mana color (blue)
                    val manaColor = when {
                        currentMana > 60 -> Color(0xFF4169E1) // Royal blue
                        currentMana > 30 -> Color(0xFF6495ED) // Cornflower blue
                        else -> Color(0xFF191970) // Midnight blue
                    }
                    
                    drawRoundRect(
                        color = manaColor,
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillWidth, barHeight - 4.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    
                    // Highlight effect
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillWidth, (barHeight - 4.dp.toPx()) * 0.4f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun FantasyTimerBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width
            val barHeight = size.height
            val safeProgress = progress.coerceIn(0f, 1f)
            
            // Border (golden/yellow)
            drawRoundRect(
                color = Color(0xFFB8860B),
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            
            // Background (dark)
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                size = Size(barWidth - 4.dp.toPx(), barHeight - 4.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
            
            // Timer fill
            if (safeProgress > 0f) {
                val fillWidth = (barWidth - 4.dp.toPx()) * safeProgress
                
                // Color based on time remaining
                val timerColor = when {
                    safeProgress > 0.5f -> Color(0xFF00CED1) // Dark turquoise
                    safeProgress > 0.3f -> Color(0xFFFFA500) // Orange
                    else -> Color(0xFFFF4500) // Red-orange
                }
                
                drawRoundRect(
                    color = timerColor,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(fillWidth, barHeight - 4.dp.toPx()),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
                
                // Highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.4f),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(fillWidth, (barHeight - 4.dp.toPx()) * 0.5f),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
            }
        }
    }
}
