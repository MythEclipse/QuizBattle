package com.mytheclipse.quizbattle

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mytheclipse.quizbattle.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ResetPassword : Screen("reset_password")
    object Main : Screen("main")
    object Battle : Screen("battle")
    object BattleResult : Screen("battle_result/{isVictory}") {
        fun createRoute(isVictory: Boolean) = "battle_result/$isVictory"
    }
    object FriendList : Screen("friend_list")
    
    // Online Mode Screens
    object OnlineMenu : Screen("online_menu")
    object Matchmaking : Screen("matchmaking")
    object OnlineBattle : Screen("online_battle/{matchId}") {
        fun createRoute(matchId: String) = "online_battle/$matchId"
    }
    object OnlineBattleResult : Screen("online_battle_result/{isVictory}") {
        fun createRoute(isVictory: Boolean) = "online_battle_result/$isVictory"
    }
    object LobbyList : Screen("lobby_list")
    object Lobby : Screen("lobby/{lobbyId}") {
        fun createRoute(lobbyId: String) = "lobby/$lobbyId"
    }
    object ChatList : Screen("chat_list")
    object ChatRoom : Screen("chat_room/{roomId}") {
        fun createRoute(roomId: String) = "chat_room/$roomId"
    }
    object Feed : Screen("feed")
    object CreatePost : Screen("create_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Missions : Screen("missions")
    object Ranked : Screen("ranked")
    object Leaderboard : Screen("leaderboard")
}

@Composable
fun QuizBattleNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToResetPassword = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToBattle = {
                    navController.navigate(Screen.Battle.route)
                },
                onNavigateToFriendList = {
                    navController.navigate(Screen.FriendList.route)
                },
                onNavigateToOnlineMenu = {
                    navController.navigate(Screen.OnlineMenu.route)
                },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Battle.route) {
            BattleScreen(
                onNavigateToResult = { isVictory ->
                    navController.navigate(Screen.BattleResult.createRoute(isVictory)) {
                        popUpTo(Screen.Battle.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.BattleResult.route,
            arguments = listOf(
                navArgument("isVictory") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val isVictory = backStackEntry.arguments?.getBoolean("isVictory") ?: false
            BattleResultScreen(
                isVictory = isVictory,
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onRematch = {
                    navController.navigate(Screen.Battle.route) {
                        popUpTo(Screen.BattleResult.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.FriendList.route) {
            FriendListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Online Mode Screens
        composable(Screen.OnlineMenu.route) {
            OnlineMenuScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuickMatch = { navController.navigate(Screen.Matchmaking.route) },
                onRankedMatch = { navController.navigate(Screen.Ranked.route) },
                onLobbyList = { navController.navigate(Screen.LobbyList.route) },
                onLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onFeed = { navController.navigate(Screen.Feed.route) },
                onChat = { navController.navigate(Screen.ChatList.route) },
                onMissions = { navController.navigate(Screen.Missions.route) },
                onNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        
        composable(Screen.Matchmaking.route) {
            MatchmakingScreen(
                onNavigateBack = { navController.popBackStack() },
                onMatchFound = { matchId ->
                    navController.navigate(Screen.OnlineBattle.createRoute(matchId)) {
                        popUpTo(Screen.Matchmaking.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.OnlineBattle.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            OnlineBattleScreen(
                matchId = matchId,
                onGameFinished = {
                    navController.navigate(Screen.OnlineMenu.route) {
                        popUpTo(Screen.OnlineBattle.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.LobbyList.route) {
            LobbyListScreen(
                onNavigateBack = { navController.popBackStack() },
                onLobbyJoined = { lobbyId ->
                    navController.navigate(Screen.Lobby.createRoute(lobbyId))
                }
            )
        }
        
        composable(
            route = Screen.Lobby.route,
            arguments = listOf(navArgument("lobbyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: ""
            LobbyRoomScreen(
                lobbyId = lobbyId,
                onNavigateBack = { navController.popBackStack() },
                onGameStarting = { matchId ->
                    navController.navigate(Screen.OnlineBattle.createRoute(matchId)) {
                        popUpTo(Screen.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoomSelected = { roomId, roomName ->
                    navController.navigate(Screen.ChatRoom.createRoute(roomId))
                }
            )
        }
        
        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            ChatRoomScreen(
                roomId = roomId,
                roomName = "Chat Room", // TODO: Pass room name
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreatePost = { navController.navigate(Screen.CreatePost.route) }
            )
        }
        
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Missions.route) {
            MissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Ranked.route) {
            RankedScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartRanked = { navController.navigate(Screen.Matchmaking.route) }
            )
        }
        
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onDeleteAccount = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
