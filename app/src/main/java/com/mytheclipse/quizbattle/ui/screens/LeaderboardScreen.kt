package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.mytheclipse.quizbattle.ui.components.EmptyState
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.LeaderboardItemSkeleton
import com.mytheclipse.quizbattle.ui.components.SkeletonList
import com.mytheclipse.quizbattle.viewmodel.OnlineLeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: OnlineLeaderboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadGlobalLeaderboard()
        } else {
            viewModel.loadFriendsLeaderboard()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) {
                            viewModel.loadGlobalLeaderboard()
                        } else {
                            viewModel.loadFriendsLeaderboard()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                    text = { Text("Global") },
                    icon = { Icon(Icons.Default.Public, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Friends") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
            }
            
            // User rank card
            if (state.userRank != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Rank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#${state.userRank}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        // Show skeleton loading
                        SkeletonList(itemCount = 10) {
                            LeaderboardItemSkeleton()
                        }
                    }
                    state.error != null -> {
                        // Show error state
                        ErrorState(
                            message = state.error ?: "Terjadi kesalahan",
                            onRetry = {
                                if (selectedTab == 0) {
                                    viewModel.loadGlobalLeaderboard()
                                } else {
                                    viewModel.loadFriendsLeaderboard()
                                }
                            }
                        )
                    }
                    state.leaderboard.isEmpty() -> {
                        // Show empty state
                        EmptyState(
                            icon = Icons.Default.Leaderboard,
                            title = if (selectedTab == 0) "Belum Ada Ranking" else "Belum Ada Teman",
                            message = if (selectedTab == 0) 
                                "Belum ada pemain di leaderboard" 
                            else 
                                "Tambahkan teman untuk melihat ranking mereka",
                            actionText = if (selectedTab == 1) "Cari Teman" else null,
                            onAction = if (selectedTab == 1) { { /* Navigate to friends */ } } else null
                        )
                    }
                    else -> {
                        // Show leaderboard list
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(state.leaderboard) { index, entry ->
                                LeaderboardItem(
                                    rank = index + 1,
                                    entry = entry,
                                    isCurrentUser = entry.isCurrentUser
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    entry: com.mytheclipse.quizbattle.data.repository.DataModels.LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val containerColor = when {
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        rank == 1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
        rank == 2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
        rank == 3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score: ${entry.score}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Wins: ${entry.wins}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // MMR/Rating
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${entry.mmr} MMR",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
