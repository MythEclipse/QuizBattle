package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.viewmodel.RankedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankedScreen(
    onNavigateBack: () -> Unit,
    onStartRanked: () -> Unit,
    viewModel: RankedViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadRankedStats()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranked Mode") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rank Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = getTierColor(state.tier)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = state.tier,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Division ${state.division}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${state.mmr} MMR",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Progress Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ranked Points",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.rankedPoints}/100",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = state.rankedPoints / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${100 - state.rankedPoints} RP to next division",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Win Rate",
                    value = "${state.winRate}%",
                    icon = Icons.Default.TrendingUp
                )
                
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Games",
                    value = state.totalGames.toString(),
                    icon = Icons.Default.SportsEsports
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Wins",
                    value = state.wins.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
                
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Losses",
                    value = state.losses.toString(),
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Play Button
            Button(
                onClick = onStartRanked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Ranked",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getTierColor(tier: String): Color {
    return when (tier.lowercase()) {
        "bronze" -> Color(0xFFCD7F32)
        "silver" -> Color(0xFFC0C0C0)
        "gold" -> Color(0xFFFFD700)
        "platinum" -> Color(0xFFE5E4E2)
        "diamond" -> Color(0xFFB9F2FF)
        "master" -> Color(0xFF9D4EDD)
        "grandmaster" -> Color(0xFFFF006E)
        else -> Color(0xFF6C757D) // Default gray
    }
}
