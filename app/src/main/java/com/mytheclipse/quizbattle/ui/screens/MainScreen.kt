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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.ui.components.QuizBattleButton
import com.mytheclipse.quizbattle.ui.theme.*

@Composable
fun MainScreen(
    onNavigateToBattle: () -> Unit,
    onNavigateToFriendList: () -> Unit
) {
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
            // 2nd place
            LeaderboardItem(
                rank = 2,
                name = "Meghan Jes...",
                points = "40 pts",
                avatarColor = Color.Gray,
                modifier = Modifier.padding(top = 20.dp)
            )
            
            // 1st place
            LeaderboardItem(
                rank = 1,
                name = "Bryan Wolf",
                points = "43 pts",
                avatarColor = PrimaryBlue,
                isTop = true
            )
            
            // 3rd place
            LeaderboardItem(
                rank = 3,
                name = "Alex Turner",
                points = "38 pts",
                avatarColor = Color.Gray,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Buttons
        QuizBattleButton(
            text = "Main(offline)",
            onClick = onNavigateToBattle,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleButton(
            text = "Main(online)",
            onClick = onNavigateToBattle,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleButton(
            text = "Tantangan Teman",
            onClick = onNavigateToBattle,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizBattleButton(
            text = "Daftar Teman",
            onClick = onNavigateToFriendList,
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
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(if (isTop) 84.dp else 74.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(3.dp, avatarColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for avatar image
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray,
                    shape = CircleShape
                ) {}
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
