package com.example.qevin.data.remote

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.userProfileChangeRequest

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    val authState: Flow<FirebaseUser?> = callbackFlow {

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    val currentFirebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Email and password login
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): FirebaseUser {
        return firebaseAuth
            .signInWithEmailAndPassword(email.trim(), password)
            .await()
            .user ?: throw IllegalStateException(
            "Firebase user not found."
        )
    }

    // Email and password registration
    suspend fun createUserWithEmail(
        email: String,
        password: String,
        displayName: String
    ): FirebaseUser {
        val user = firebaseAuth
            .createUserWithEmailAndPassword(email.trim(), password)
            .await()
            .user ?: throw IllegalStateException(
            "Firebase user not found."
        )

        val profile = userProfileChangeRequest {
            this.displayName = displayName.trim()
        }
        /*
        val profile = com.google.firebase.auth.userProfileChangeRequest {
            this.displayName = displayName.trim()
        }

         */

        user.updateProfile(profile).await()

        return user
    }

    // Send phone OTP

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onOtpCodeSent: (String) -> Unit,
        onAutoVerified: (FirebaseUser) -> Unit,
        onOtpRequestFailed: (Exception) -> Unit
    ) {
        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {
                    val currentUser = firebaseAuth.currentUser

                    if (currentUser == null) {
                        onOtpRequestFailed(
                            IllegalStateException(
                                "No email account is currently authenticated."
                            )
                        )
                        return
                    }

                    currentUser.linkWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            val linkedUser = authResult.user

                            if (linkedUser != null) {
                                onAutoVerified(linkedUser)
                            } else {
                                onOtpRequestFailed(
                                    IllegalStateException(
                                        "Phone verification succeeded but user was not returned."
                                    )
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            onOtpRequestFailed(exception)
                        }
                }


                override fun onVerificationFailed(
                    exception: FirebaseException
                ) {
                    onOtpRequestFailed(exception)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onOtpCodeSent(verificationId)
                }
            }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    // Verify manually entered OTP

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otp: String
    ): FirebaseUser {

        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException(
                "No authenticated email account found."
            )

        val credential = PhoneAuthProvider.getCredential(
            verificationId,
            otp
        )

        val authResult = currentUser
            .linkWithCredential(credential)
            .await()

        return authResult.user
            ?: throw IllegalStateException(
                "Phone account linking failed."
            )
    }




    // Logout
    fun signOut() {
        firebaseAuth.signOut()
    }
}
