package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.viewmodel.DailyMissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyMissionsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        viewModel.loadMissions()
        viewModel.loadAchievements()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Missions & Achievements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily Missions") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Achievements") }
                )
            }
            
            when (selectedTab) {
                0 -> MissionsTab(
                    missions = state.missions,
                    isLoading = state.isLoading,
                    onClaimReward = { missionId -> viewModel.claimMission(missionId) }
                )
                1 -> AchievementsTab(
                    achievements = state.achievements,
                    isLoading = state.isLoading
                )
            }
        }
    }
}

@Composable
fun MissionsTab(
    missions: List<com.mytheclipse.quizbattle.data.repository.DataModels.Mission>,
    isLoading: Boolean,
    onClaimReward: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (missions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "All missions completed!",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missions) { mission ->
                    MissionItem(
                        mission = mission,
                        onClaimReward = { onClaimReward(mission.missionId) }
                    )
                }
            }
        }
    }
}

@Composable
fun MissionItem(
    mission: com.mytheclipse.quizbattle.data.repository.DataModels.Mission,
    onClaimReward: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (mission.isCompleted) {
                    if (mission.isClaimed) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Claimed",
                            tint = Color(0xFF4CAF50)
                        )
                    } else {
                        Button(
                            onClick = onClaimReward,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Claim")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${mission.currentProgress}/${mission.targetProgress}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (mission.currentProgress.toFloat() / mission.targetProgress),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reward
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
                Text(
                    text = "${mission.reward} coins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AchievementsTab(
    achievements: List<com.mytheclipse.quizbattle.data.repository.DataModels.Achievement>,
    isLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (achievements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No achievements yet",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementItem(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementItem(
    achievement: com.mytheclipse.quizbattle.data.repository.DataModels.Achievement
) {
    val containerColor = if (achievement.isUnlocked) {
        Color(0xFFFFD700).copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked)
                            Color(0xFFFFD700)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (achievement.isUnlocked) Color.White else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (achievement.isUnlocked && achievement.unlockedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked ${formatAchievementTime(achievement.unlockedAt!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            if (!achievement.isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

private fun formatAchievementTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "${diff / 604800_000}w ago"
    }
}
