package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.ui.components.EmptyState
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.LobbyItemSkeleton
import com.mytheclipse.quizbattle.ui.components.SkeletonList
import com.mytheclipse.quizbattle.viewmodel.LobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyListScreen(
    onNavigateBack: () -> Unit,
    onLobbyJoined: (String) -> Unit,
    viewModel: LobbyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.lobbyId) {
        if (state.lobbyId != null) {
            onLobbyJoined(state.lobbyId ?: "")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobbies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.listLobbies() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Lobby")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { showJoinDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Login, contentDescription = "Join by Code")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    SkeletonList(itemCount = 6) {
                        LobbyItemSkeleton()
                    }
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Gagal memuat lobby",
                        onRetry = { viewModel.listLobbies() }
                    )
                }
                state.lobbies.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.MeetingRoom,
                        title = "Belum Ada Lobby",
                        message = "Buat lobby baru atau join dengan kode",
                        actionText = "Buat Lobby",
                        onAction = { showCreateDialog = true }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.lobbies) { lobby ->
                            LobbyItem(
                                lobby = lobby,
                                onJoin = { viewModel.joinLobby(lobby.lobbyId) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateLobbyDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, maxPlayers, isPrivate ->
                viewModel.createLobby(name, maxPlayers, isPrivate)
                showCreateDialog = false
            }
        )
    }
    
    if (showJoinDialog) {
        JoinLobbyDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code ->
                viewModel.joinLobbyByCode(code)
                showJoinDialog = false
            }
        )
    }
}

@Composable
fun LobbyItem(
    lobby: com.mytheclipse.quizbattle.data.repository.DataModels.LobbyInfo,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lobby.lobbyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${lobby.currentPlayers}/${lobby.maxPlayers}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (lobby.isPrivate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Private",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = onJoin,
                enabled = lobby.currentPlayers < lobby.maxPlayers
            ) {
                Text("Join")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, Boolean) -> Unit
) {
    var lobbyName by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf("4") }
    var isPrivate by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Lobby") },
        text = {
            Column {
                OutlinedTextField(
                    value = lobbyName,
                    onValueChange = { lobbyName = it },
                    label = { Text("Lobby Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxPlayers,
                    onValueChange = { maxPlayers = it },
                    label = { Text("Max Players (2-8)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                    Text("Private Lobby")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val players = maxPlayers.toIntOrNull() ?: 4
                    onCreate(lobbyName, players, isPrivate)
                },
                enabled = lobbyName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinLobbyDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Lobby") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Lobby Code") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onJoin(code) },
                enabled = code.isNotBlank()
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
