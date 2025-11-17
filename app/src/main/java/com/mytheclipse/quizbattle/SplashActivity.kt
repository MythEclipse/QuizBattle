package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.mytheclipse.quizbattle.ui.screens.SplashScreen
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            QuizBattleTheme {
                SplashScreen(
                    onNavigateToMain = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
