package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.mytheclipse.quizbattle.ui.screens.BattleScreen
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme

class BattleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_battle)
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            QuizBattleTheme {
                BattleScreen(
                    onNavigateToResult = { isVictory ->
                        val intent = Intent(this, BattleResultActivity::class.java).apply {
                            putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
