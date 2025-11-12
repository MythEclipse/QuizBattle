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
import androidx.compose.ui.platform.LocalContext
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
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    
    // Handle success navigation
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && authState.user != null) {
            onNavigateToMain()
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
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo and title
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
            
            // Welcome text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selamat Datang",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Mohon isi email dan kata sandi untuk melanjutkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Forgot password
            Text(
                text = "Lupa Kata Sandi?",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onNavigateToResetPassword() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login button
            QuizBattleButton(
                text = "Masuk",
                onClick = {
                    viewModel.login(email, password)
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
            
            // Register link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Belum punya akun? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Daftar",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
