package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.ui.components.QuizBattleButton
import com.mytheclipse.quizbattle.ui.components.QuizBattleOutlinedButton
import com.mytheclipse.quizbattle.ui.theme.*
import com.mytheclipse.quizbattle.viewmodel.MainViewModel

@Suppress("UNUSED_PARAMETER")
@Composable
fun MainScreen(
    onNavigateToBattle: () -> Unit,
    onNavigateToFriendList: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Leaderboard title
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Leaderboard Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Display top 3 users from database or placeholder
            val topUsers = state.topUsers.take(3)
            
            // 2nd place
            LeaderboardItem(
                rank = 2,
                name = topUsers.getOrNull(1)?.username ?: "Player 2",
                points = "${topUsers.getOrNull(1)?.points ?: 0} pts",
                avatarColor = Color.Gray,
                modifier = Modifier.padding(top = 20.dp)
            )
            
            // 1st place
            LeaderboardItem(
                rank = 1,
                name = topUsers.getOrNull(0)?.username ?: "Player 1",
                points = "${topUsers.getOrNull(0)?.points ?: 0} pts",
                avatarColor = PrimaryBlue,
                isTop = true
            )
            
            // 3rd place
            LeaderboardItem(
                rank = 3,
                name = topUsers.getOrNull(2)?.username ?: "Player 3",
                points = "${topUsers.getOrNull(2)?.points ?: 0} pts",
                avatarColor = Color.Gray,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Read currentUser once to avoid smart-cast errors on delegated property
        val currentUser = state.currentUser

        // User info section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentUser != null) "Halo, ${currentUser.username}!" else "Mode: Guest",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (currentUser != null) "Points: ${currentUser.points}" else "Login untuk menyimpan progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Buttons
        QuizBattleButton(
            text = "Main Quiz (Offline)",
            onClick = onNavigateToBattle,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleButton(
            text = "Login / Register",
            onClick = { /* Coming Soon */ },
            backgroundColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Coming Soon",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleOutlinedButton(
            text = "Main Online (Coming Soon)",
            onClick = { /* Coming Soon */ },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleOutlinedButton(
            text = "Tantangan Teman (Coming Soon)",
            onClick = { /* Coming Soon */ },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    name: String,
    points: String,
    avatarColor: Color,
    modifier: Modifier = Modifier,
    isTop: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Avatar with rank badge
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            // Avatar circle with image
            Box(
                modifier = Modifier
                    .size(if (isTop) 84.dp else 74.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(3.dp, avatarColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Use player avatar for leaderboard
                Image(
                    painter = painterResource(id = R.drawable.player_avatar),
                    contentDescription = "Player $rank",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Rank badge
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = 4.dp),
                shape = CircleShape,
                color = PrimaryBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Name
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Points
        Text(
            text = points,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp
            ),
            color = Color.Black
        )
    }
}
