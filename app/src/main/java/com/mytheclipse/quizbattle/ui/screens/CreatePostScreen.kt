package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.viewmodel.SocialMediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: SocialMediaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var content by remember { mutableStateOf("") }
    
    LaunchedEffect(state.postCreated) {
        if (state.postCreated) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                viewModel.createPost(content)
                            }
                        },
                        enabled = content.isNotBlank() && !state.isLoading
                    ) {
                        Text("Post")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("What's on your mind?") },
                maxLines = 20
            )
            
            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
