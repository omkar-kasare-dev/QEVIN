package com.example.qevin.presentation.navigation

/*
object AppRoute {

    const val Splash = "splash"

    const val Onboarding = "onboarding"

    const val Login = "login"
    const val Register = "register"

    const val Home = "home"
    const val NewChat = "new_chat"

    const val Conversation = "conversation/{conversationId}"
    const val Profile = "profile"
    const val Settings = "settings"
    const val Privacy = "privacy"
    const val Search = "search"

    fun conversation(conversationId: String): String {
        return "conversation/$conversationId"
    }
}

 */



object AppRoute {

    // First launch
    const val Splash = "splash"
    const val Onboarding = "onboarding"

    // Authentication
    const val Login = "login"
    const val Register = "register"
    const val ForgotPassword = "forgot_password"
    const val OtpVerification = "otp_verification"

    // Main application
    const val Home = "home"
    const val NewChat = "new_chat"
    const val Calls = "calls"
    const val People = "people"

    // Individual screens
    const val Conversation = "conversation/{conversationId}"
    const val Profile = "profile"
    const val Settings = "settings"
    const val Privacy = "privacy"
    const val Search = "search"

    // Dynamic route builders
    fun conversation(conversationId: String): String {
        return "conversation/${android.net.Uri.encode(conversationId)}"
    }

    fun otpVerification(phoneNumber: String): String {
        return "$OtpVerification/${android.net.Uri.encode(phoneNumber)}"
    }
}
