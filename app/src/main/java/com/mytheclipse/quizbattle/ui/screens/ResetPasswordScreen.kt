package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mytheclipse.quizbattle.ui.components.QuizBattleButton
import com.mytheclipse.quizbattle.ui.components.QuizBattleTextField
import com.mytheclipse.quizbattle.ui.theme.*
import com.mytheclipse.quizbattle.utils.rememberHapticFeedback
import androidx.compose.runtime.rememberCoroutineScope
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.remote.api.AuthApiService
import com.mytheclipse.quizbattle.data.remote.api.ResetPasswordRequest
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Password baru akan dikirimkan melalui email yang telah didaftarkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field
            QuizBattleTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Masukkan email disini",
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Send button
            QuizBattleButton(
                text = "Kirim",
                onClick = {
                    haptic.mediumTap()
                    error = null
                    val emailTrim = email.trim()
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()
                    if (!isEmailValid) {
                        error = "Format email tidak valid"
                        return@QuizBattleButton
                    }
                    scope.launch {
                        try {
                            val service = ApiConfig.createService(AuthApiService::class.java)
                            val resp = service.resetPassword(ResetPasswordRequest(emailTrim))
                            if (resp.success) {
                                showSuccessDialog = true
                            } else {
                                error = resp.error ?: "Gagal mengirim tautan reset"
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Terjadi kesalahan koneksi"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Berhasil!",
                    style = MaterialTheme.typography.headlineLarge
                )
            },
            text = {
                Text(
                    text = "Anda bisa mengecek email anda untuk mengatur ulang kata sandi Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                QuizBattleButton(
                    text = "OK",
                    onClick = {
                        showSuccessDialog = false
                        onSuccess()
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}
