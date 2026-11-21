package com.example.qevin.data.repository


import android.app.Activity
import com.example.qevin.data.remote.FirebaseAuthDataSource
import com.example.qevin.domain.model.QevinUser
import com.example.qevin.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    // Observe current authenticated user
    override val currentUser: Flow<QevinUser?> =
        authDataSource.authState.map { firebaseUser ->
            firebaseUser?.toQevinUser()
        }

    // Email login
    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<QevinUser> = safeResult {
        authDataSource.signInWithEmail(
            email = email,
            password = password
        ).toQevinUser()
    }

    // Email registration
    override suspend fun registerWithEmail(
        displayName: String,
        email: String,
        password: String
    ): Result<QevinUser> = safeResult {
        authDataSource.createUserWithEmail(
            displayName = displayName,
            email = email,
            password = password
        ).toQevinUser()
    }

    // Send phone OTP
    override fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onOtpCodeSent: (String) -> Unit,
        onAutoVerified: (QevinUser) -> Unit,
        onOtpRequestFailed: (Exception) -> Unit
    ) {
        authDataSource.sendPhoneOtp(
            activity = activity,
            phoneNumber = phoneNumber,
            onOtpCodeSent = onOtpCodeSent,
            onAutoVerified = { firebaseUser ->
                onAutoVerified(firebaseUser.toQevinUser())
            },
            onOtpRequestFailed = onOtpRequestFailed
        )
    }

    // Verify phone OTP
    override suspend fun verifyPhoneOtp(
        verificationId: String,
        otp: String
    ): Result<QevinUser> = safeResult {
        authDataSource.verifyPhoneOtp(
            verificationId = verificationId,
            otp = otp
        ).toQevinUser()
    }

    // Logout
    override suspend fun logout(): Result<Unit> = safeResult {
        authDataSource.signOut()
    }

    // Convert FirebaseUser into domain model
    private fun FirebaseUser.toQevinUser(): QevinUser {
        return QevinUser(
            id = uid,
            displayName = displayName.orEmpty(),
            phoneNumber = phoneNumber,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }

    // Handle errors without swallowing coroutine cancellation
    private suspend fun <T> safeResult(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
