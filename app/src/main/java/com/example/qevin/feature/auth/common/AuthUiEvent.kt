package com.example.qevin.feature.auth.common

sealed interface AuthUiEvent {

    data class LoginRequested(
        val identifier: String,
        val password: String
    ) : AuthUiEvent

    // Registration with email and password
    data class RegistrationRequested(
        val displayName: String,
        val email: String,
        val phoneNumber: String,
        val password: String
    ) : AuthUiEvent


    // Request phone OTP
    data class PhoneOtpRequested(
        val phoneNumber: String
    ) : AuthUiEvent

    // Verify phone OTP
    data class PhoneOtpVerificationRequested(
        val verificationId: String,
        val otp: String
    ) : AuthUiEvent

    data class ShowMessage(
        val message: String
    ) : AuthUiEvent

    data object LoginSuccess : AuthUiEvent
}