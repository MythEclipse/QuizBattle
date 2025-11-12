package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000) // Show splash for 2 seconds
        onNavigateToMain()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo placeholder - In real app, use Image composable
            Spacer(modifier = Modifier.height(80.dp))
            
            // Quiz Battle text with gradient
            Text(
                text = "Quiz\nBattle",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GradientTextRedStart,
                            GradientTextBlueEnd
                        )
                    ),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
