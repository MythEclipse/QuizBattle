package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mytheclipse.quizbattle.ui.components.QuizBattleButton
import com.mytheclipse.quizbattle.ui.components.QuizBattlePasswordField
import com.mytheclipse.quizbattle.ui.components.QuizBattleTextField
import com.mytheclipse.quizbattle.ui.theme.*
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    
    // Handle success navigation
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && authState.user != null) {
            onNavigateToLogin()
            viewModel.resetState()
        }
    }
    
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
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo
            Text(
                text = "Quiz Battle",
                style = MaterialTheme.typography.displayMedium.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GradientTextRedStart,
                            GradientTextBlueEnd
                        )
                    ),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daftar Akun",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Silakan buat akun untuk mengakses aplikasi ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Username field
            QuizBattleTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                placeholder = "Masukkan username disini",
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email field
            QuizBattleTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Masukkan email disini",
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            QuizBattlePasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Kata Sandi",
                placeholder = "Masukkan kata sandi disini",
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm password field
            QuizBattlePasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Kata Sandi",
                placeholder = "Masukkan kata sandi disini",
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Register button
            QuizBattleButton(
                text = "Selanjutnya",
                onClick = {
                    viewModel.register(username, email, password, confirmPassword)
                },
                enabled = !authState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Show loading or error
            if (authState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            if (authState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sudah punya akun? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Masuk",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
