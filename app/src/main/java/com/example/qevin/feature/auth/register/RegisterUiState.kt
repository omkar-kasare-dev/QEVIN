package com.example.qevin.feature.auth.register

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Registration fields
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",

    // Phone OTP
    val isOtpSent: Boolean = false,
    val otp: String = "",
    val verificationId: String? = null,
    val isPhoneVerified: Boolean = false,
    val isEmailAccountCreated: Boolean = false,

    // Registration status
    val isRegistrationCompleted: Boolean = false
)
