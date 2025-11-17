package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.mytheclipse.quizbattle.ui.screens.BattleResultScreen
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme

class BattleResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_battle_result)
        val isVictory = intent.getBooleanExtra(EXTRA_IS_VICTORY, false)
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            QuizBattleTheme {
                BattleResultScreen(
                    isVictory = isVictory,
                    onNavigateToMain = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onRematch = {
                        startActivity(Intent(this, BattleActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_IS_VICTORY = "extra_is_victory"
    }
}
