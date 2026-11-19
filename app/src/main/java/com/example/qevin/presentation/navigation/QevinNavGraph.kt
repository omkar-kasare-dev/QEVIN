package com.example.qevin.presentation.navigation

/*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qevin.presentation.home.QEVINHomeScreen
import com.example.qevin.presentation.splash.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun QevinNavGraph(
    navController: NavHostController,
    startDestination: String = AppRoute.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Splash
        composable(AppRoute.Splash) {

            SplashScreen()

            LaunchedEffect(Unit) {
                delay(2200)

                navController.navigate(AppRoute.Home) {
                    popUpTo(AppRoute.Splash) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        // Main Home
        composable(AppRoute.Home) {
            QEVINHomeScreen()
        }

        // Onboarding
        composable(AppRoute.Onboarding) {
            PlaceholderScreen("Onboarding")
        }

        // Authentication
        composable(AppRoute.Login) {
            PlaceholderScreen("Login")
        }

        composable(AppRoute.Register) {
            PlaceholderScreen("Create Account")
        }

        // New conversation
        composable(AppRoute.NewChat) {
            PlaceholderScreen("New Chat")
        }

        // Conversation with dynamic ID
        composable(
            route = AppRoute.Conversation,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val conversationId =
                backStackEntry.arguments?.getString("conversationId")

            PlaceholderScreen(
                title = "Conversation",
                subtitle = "Conversation ID: $conversationId"
            )
        }

        // Profile
        composable(AppRoute.Profile) {
            PlaceholderScreen("Profile")
        }

        // Settings
        composable(AppRoute.Settings) {
            PlaceholderScreen("Settings")
        }

        // Privacy
        composable(AppRoute.Privacy) {
            PlaceholderScreen("Privacy & Security")
        }

        // Search
        composable(AppRoute.Search) {
            PlaceholderScreen("Search")
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String = "This screen will be implemented in a future step."
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

 */




import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qevin.feature.auth.LoginScreen
import com.example.qevin.feature.auth.RegisterScreen
import com.example.qevin.presentation.home.QEVINHomeScreen
import com.example.qevin.presentation.onboarding.OnboardingScreen
import com.example.qevin.presentation.splash.SplashScreen

import kotlinx.coroutines.delay

@Composable
fun QevinNavGraph(
    navController: NavHostController,
    startDestination: String = AppRoute.Splash
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 1. Splash
        composable(AppRoute.Splash) {

            SplashScreen()

            LaunchedEffect(Unit) {
                delay(2000)

                navController.navigate(AppRoute.Onboarding) {
                    popUpTo(AppRoute.Splash) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        // 2. Onboarding
        composable(AppRoute.Onboarding) {

            OnboardingScreen(
                onFinish = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.Onboarding) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 3. Login
        composable(AppRoute.Login) {
            LoginScreen(
                onLoginClick = { identifier, password ->
                    // TODO: Trigger ViewModel login
                    // On success, navigate to Home/Chats and clear auth backstack:
                    // navController.navigate(AppRoute.Home) {
                    //     popUpTo(AppRoute.Login) { inclusive = true }
                    // }
                },
                onGoogleSignInClick = {
                    // TODO: Trigger Google Sign-In flow
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoute.Register) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 4. Register
        composable(AppRoute.Register) {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountClick = { name, mobileNumber, password ->
                    // TODO: Trigger ViewModel registration
                    // On success, navigate to Home/Chats and clear auth backstack:
                    // navController.navigate(AppRoute.Home) {
                    //     popUpTo(AppRoute.Login) { inclusive = true }
                    // }
                },
                onTermsClick = {
                    // TODO: Open Terms of Service screen or URL
                },
                onPrivacyPolicyClick = {
                    // TODO: Open Privacy Policy screen or URL
                }
            )
        }

        // 5. Forgot Password
        composable(AppRoute.ForgotPassword) {

            NavigationPlaceholder(
                title = "Forgot Password"
            )
        }

        // 6. OTP Verification
        composable(
            route = "${AppRoute.OtpVerification}/{phoneNumber}",
            arguments = listOf(
                navArgument("phoneNumber") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val phoneNumber = backStackEntry.arguments
                ?.getString("phoneNumber")
                .orEmpty()

            NavigationPlaceholder(
                title = "Verify Phone Number",
                description = "Verification for $phoneNumber"
            )
        }

        // 7. Home
        composable(AppRoute.Home) {
            QEVINHomeScreen()
        }

        // 8. New Chat
        composable(AppRoute.NewChat) {
            NavigationPlaceholder("New Chat")
        }

        // 9. Conversation
        composable(
            route = AppRoute.Conversation,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val conversationId = backStackEntry.arguments
                ?.getString("conversationId")
                .orEmpty()

            NavigationPlaceholder(
                title = "Conversation",
                description = "Conversation ID: $conversationId"
            )
        }

        // 10. Calls
        composable(AppRoute.Calls) {
            NavigationPlaceholder("Calls")
        }

        // 11. People
        composable(AppRoute.People) {
            NavigationPlaceholder("People")
        }

        // 12. Profile
        composable(AppRoute.Profile) {
            NavigationPlaceholder("Profile")
        }

        // 13. Settings
        composable(AppRoute.Settings) {
            NavigationPlaceholder("Settings")
        }

        // 14. Privacy
        composable(AppRoute.Privacy) {
            NavigationPlaceholder("Privacy & Security")
        }

        // 15. Search
        composable(AppRoute.Search) {
            NavigationPlaceholder("Search")
        }
    }
}

@Composable
private fun NavigationPlaceholder(
    title: String,
    description: String = "This screen will be implemented in a future step."
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
