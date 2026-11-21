package com.example.qevin.domain.repository

import android.app.Activity
import com.example.qevin.domain.model.QevinUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<QevinUser?>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<QevinUser>

    suspend fun registerWithEmail(
        displayName: String,
        email: String,
        password: String
    ): Result<QevinUser>
/*
    // Send phone verification OTP
    suspend fun sendPhoneOtp(
        phoneNumber: String
    ): Result<String>


 */
    // Verify phone OTP
    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onOtpCodeSent: (String) -> Unit,
        onAutoVerified: (QevinUser) -> Unit,
        onOtpRequestFailed: (Exception) -> Unit
    )

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otp: String
    ): Result<QevinUser>


    suspend fun logout(): Result<Unit>
}
