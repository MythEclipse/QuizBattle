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
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.mytheclipse.quizbattle.ui.components.ErrorState
import com.mytheclipse.quizbattle.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: SocialMediaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var content by remember { mutableStateOf("") }
    val haptic = rememberHapticFeedback()
    
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank()) {
                                haptic.mediumTap()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingState(message = "Mengirim postingan...")
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Gagal membuat postingan",
                        onRetry = {
                            if (content.isNotBlank()) {
                                haptic.mediumTap()
                                viewModel.createPost(content)
                            }
                        }
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                    }
                }
            }
        }
    }
}
