package com.example.qevin.presentation.navigation


import android.util.Log
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qevin.feature.auth.LoginScreen
import com.example.qevin.feature.auth.RegisterScreen
import com.example.qevin.feature.auth.login.LoginNavigationEvent
import com.example.qevin.feature.auth.login.LoginViewModel
import com.example.qevin.feature.auth.register.RegisterViewModel
import com.example.qevin.presentation.conversation.ConversationScreen
import com.example.qevin.presentation.home.QEVINHomeScreen
import com.example.qevin.presentation.onboarding.OnboardingScreen
import com.example.qevin.presentation.splash.SplashDestination
import com.example.qevin.presentation.splash.SplashScreen
import com.example.qevin.presentation.splash.SplashViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

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

            val splashViewModel: SplashViewModel = hiltViewModel()

            SplashScreen()

            LaunchedEffect(splashViewModel) {

                Log.d(
                    "QEVIN_NAVIGATION",
                    "Waiting for authentication session"
                )

                val destination = splashViewModel.destination
                    .filterNotNull()
                    .first()

                delay(1500)

                val targetRoute = when (destination) {

                    SplashDestination.HOME -> {
                        Log.d(
                            "QEVIN_NAVIGATION",
                            "Existing session found. Opening Home."
                        )

                        AppRoute.Home
                    }

                    SplashDestination.ONBOARDING -> {
                        Log.d(
                            "QEVIN_NAVIGATION",
                            "No session found. Opening Onboarding."
                        )

                        AppRoute.Onboarding
                    }
                }

                navController.navigate(targetRoute) {

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

            val loginViewModel: LoginViewModel = hiltViewModel()

            // Observe one-time login navigation events
            LaunchedEffect(loginViewModel) {
                loginViewModel.navigationEvents.collect { event ->

                    when (event) {

                        LoginNavigationEvent.LoginSuccess -> {

                            Log.d(
                                "QEVIN_NAVIGATION",
                                "Login successful. Navigating to Home."
                            )

                            navController.navigate(AppRoute.Home) {
                                popUpTo(AppRoute.Login) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                     }
                }
            }

            LoginScreen(
                onGoogleSignInClick = {
                    // Google sign-in integration will be added later.
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoute.Register)
                }
            )
        }


        //------------------------------------
        // 4. Register

        composable(
            route = AppRoute.Register
        ) {
            val viewModel: RegisterViewModel = hiltViewModel()

            RegisterScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onRegistrationCompleted = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Register) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onTermsClick = {
                    // Existing Terms navigation
                },
                onPrivacyPolicyClick = {
                    // Existing Privacy Policy navigation
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

            QEVINHomeScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(
                        AppRoute.conversation(conversationId)
                    )
                },
                onNewChatClick = {
                    navController.navigate(AppRoute.NewChat)
                },
                onSearchClick = {
                    navController.navigate(AppRoute.Search)
                },
                onProfileClick = {
                    navController.navigate(AppRoute.Profile)
                },
                onBottomTabClick = { tab ->
                    when (tab) {
                        "CALLS" -> navController.navigate(AppRoute.Calls)
                        "PEOPLE" -> navController.navigate(AppRoute.People)
                        "SETTINGS" -> navController.navigate(AppRoute.Settings)
                    }
                }
            )
        }

        // 8. New Chat
        composable(AppRoute.NewChat) {
            NavigationPlaceholder("New Chat")
        }

        // 9. Conversation
        /*
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
*/
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

            ConversationScreen(
                conversationId = conversationId,
                onBackClick = {
                    navController.popBackStack()
                },
                onVoiceCallClick = {
                    // Voice call implementation will be added later.
                },
                onVideoCallClick = {
                    // Video call implementation will be added later.
                },
                onMoreClick = {
                    // Conversation menu will be added later.
                }
            )
        }

        //-------------------------------
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
