package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.mytheclipse.quizbattle.ui.screens.MainScreen
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            QuizBattleTheme {
                MainScreen(
                    onNavigateToBattle = {
                        startActivity(Intent(this, BattleActivity::class.java))
                    },
                    onNavigateToFriendList = {
                        // TODO: Create FriendListActivity and navigate here
                    },
                    onNavigateToOnlineMenu = {
                        // TODO: Create OnlineMenuActivity and navigate here
                    },
                    onNavigateToFeed = {
                        // TODO: Create FeedActivity and navigate here
                    },
                    onNavigateToNotifications = {
                        // TODO: Create NotificationActivity and navigate here
                    },
                    onNavigateToProfile = {
                        // TODO: Create ProfileActivity and navigate here
                    }
                )
            }
        }
    }
}