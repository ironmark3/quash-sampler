package com.g.quash_sampler.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.g.quash_sampler.ui.home.HomeScreen
import com.g.quash_sampler.ui.login.LoginScreen
import com.g.quash_sampler.ui.onboarding.OnboardingScreen
import com.g.quash_sampler.ui.otp.OtpScreen
import com.g.quash_sampler.ui.profile.ProfileScreen
import com.g.quash_sampler.ui.splash.SplashScreen
import com.g.quash_sampler.ui.bugreport.BugReportScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Otp : Screen("otp/{sessionId}") {
        fun createRoute(sessionId: String) = "otp/$sessionId"
    }
    data object Onboarding : Screen("onboarding/{userId}") {
        fun createRoute(userId: String) = "onboarding/$userId"
    }
    data object Home : Screen("home/{userId}") {
        fun createRoute(userId: String) = "home/$userId"
    }
    data object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    data object BugReport : Screen("bug-report/{userId}") {
        fun createRoute(userId: String) = "bug-report/$userId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToOtp = { sessionId ->
                    navController.navigate(Screen.Otp.createRoute(sessionId))
                }
            )
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            OtpScreen(
                sessionId = sessionId,
                onNavigateToHome = { userId ->
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = { userId ->
                    navController.navigate(Screen.Onboarding.createRoute(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            // Create a temporary user with minimal data for onboarding
            // The OnboardingViewModel will load the full user data
            OnboardingScreen(
                user = com.g.quash_sampler.domain.model.User(
                    id = userId,
                    name = "New User"
                ),
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkipOnboarding = {
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            HomeScreen(
                userId = userId,
                onNavigateToProfile = { profileUserId ->
                    navController.navigate(Screen.Profile.createRoute(profileUserId))
                },
                onNavigateToBugReport = { bugReportUserId ->
                    navController.navigate(Screen.BugReport.createRoute(bugReportUserId))
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.BugReport.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            BugReportScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        popUpTo(Screen.BugReport.route) { inclusive = true }
                    }
                }
            )
        }
    }
}