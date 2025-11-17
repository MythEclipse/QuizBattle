package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.ui.theme.*
import com.mytheclipse.quizbattle.ui.components.EmptyState
import com.mytheclipse.quizbattle.ui.components.SkeletonList
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import com.mytheclipse.quizbattle.viewmodel.FriendListViewModel
import com.mytheclipse.quizbattle.data.repository.FriendInfo
import com.mytheclipse.quizbattle.utils.androidViewModel

data class Friend(
    val name: String,
    val avatarColor: Color = Color.LightGray
)

@Composable
fun FriendListScreen(
    onNavigateBack: () -> Unit,
    viewModel: FriendListViewModel = androidViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val haptic = rememberHapticFeedback()
    val state by viewModel.state.collectAsState()
    val filteredFriends = state.friends.filter { it.username.contains(searchQuery, ignoreCase = true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            
            Text(
                text = "Daftar Teman",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black
            )
            
            IconButton(onClick = { 
                haptic.lightTap()
                /* Add friend */ 
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add friend",
                    tint = Color.Black
                )
            }
        }
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text(
                    text = "Cari",
                    color = TextPlaceholder
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPlaceholder
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderGray,
                unfocusedBorderColor = BorderGray,
                focusedContainerColor = BackgroundGray,
                unfocusedContainerColor = BackgroundGray
            ),
            shape = RoundedCornerShape(15.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Friends list with states
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    SkeletonList(
                        itemCount = 8,
                        itemSkeleton = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray)
                                )
                            }
                        }
                    )
                }
                filteredFriends.isEmpty() && searchQuery.isNotEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Tidak ada teman ditemukan",
                        message = "Coba kata kunci lain"
                    )
                }
                state.friends.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Add,
                        title = "Belum ada teman",
                        message = "Tambahkan teman untuk bermain bersama",
                        actionText = "Cari Teman",
                        onAction = { /* Navigate to search */ }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredFriends,
                            key = { friend -> friend.userId }
                        ) { friend ->
                            Column {
                                FriendInfoItem(
                                    friend = friend,
                                    onClick = { /* Handle friend click */ }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = BorderSeparator,
                                    thickness = 1.dp
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
fun FriendItem(
    friend: Friend,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(friend.avatarColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name
        Text(
            text = friend.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp
            ),
            color = Color.Black
        )
    }
}

@Composable
fun FriendInfoItem(
    friend: FriendInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials fallback
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF90A4AE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.username.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = friend.username,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp
                ),
                color = Color.Black
            )
            Text(
                text = "${friend.wins}W / ${friend.losses}L · ${friend.status}",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}
