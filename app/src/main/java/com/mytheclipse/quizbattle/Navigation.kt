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
    }
}
