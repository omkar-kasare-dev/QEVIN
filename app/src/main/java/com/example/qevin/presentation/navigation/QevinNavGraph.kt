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
import com.example.qevin.presentation.calls.CallsScreen
import com.example.qevin.presentation.conversation.ConversationScreen
import com.example.qevin.presentation.conversation.NewConversationScreen
import com.example.qevin.presentation.debug.DebugOpenChatScreen
import com.example.qevin.presentation.home.QEVINHomeScreen
import com.example.qevin.presentation.onboarding.OnboardingScreen
import com.example.qevin.presentation.people.PeopleScreen
import com.example.qevin.presentation.profile.ProfileScreen
import com.example.qevin.presentation.search.SearchScreen
import com.example.qevin.presentation.settings.SettingsScreen
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
                    // Terms navigation
                },
                onPrivacyPolicyClick = {
                    // Privacy Policy navigation
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
            NewConversationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                // Temporary Implementation
                onNewGroupClick = {
                    navController.navigate(AppRoute.DebugOpenChat) // TEMPORARY
                },
                onContactSelected = { contactId ->
                    navController.navigate(AppRoute.conversation(contactId)) {
                        popUpTo(AppRoute.NewChat) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Temporary Composable for testing.
        // TEMPORARY debug entry. Delete after Phase 5.
        composable(AppRoute.DebugOpenChat) {
            DebugOpenChatScreen(
                onBackClick = { navController.popBackStack() },
                onOpenChat = { conversationId ->
                    navController.navigate(AppRoute.conversation(conversationId)) {
                        popUpTo(AppRoute.DebugOpenChat) { inclusive = true }
                    }
                }
            )
        }

        //-------------

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
                    // Voice call action
                },
                onVideoCallClick = {
                    // Video call action
                },
                onMoreClick = {
                    // Conversation options menu
                }
            )
        }

        // 10. Calls
        composable(AppRoute.Calls) {
            CallsScreen(
                onCallClick = { callId ->
                    navController.navigate(AppRoute.conversation(callId))
                }
            )
        }

        // 11. People
        composable(AppRoute.People) {
            PeopleScreen(
                onContactClick = { contactId ->
                    navController.navigate(AppRoute.conversation(contactId))
                }
            )
        }

        // 12. Profile
        composable(AppRoute.Profile) {

            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditProfileClick = {
                    // Edit Profile implementation: Phase 1, Step 5
                },
                onLoggedOut = {
                    Log.d(
                        "QEVIN_NAVIGATION",
                        "Logged out. Clearing back stack and opening Login."
                    )

                    // Clear the ENTIRE back stack (not just Home), so Back from
                    // Login can never return to a signed-in screen.
                    navController.navigate(AppRoute.Login) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 13. Settings
        composable(AppRoute.Settings) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 14. Privacy
        composable(AppRoute.Privacy) {
            NavigationPlaceholder("Privacy & Security")
        }

        // 15. Search
        composable(AppRoute.Search) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onResultClick = { conversationId ->
                    navController.navigate(AppRoute.conversation(conversationId))
                }
            )
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