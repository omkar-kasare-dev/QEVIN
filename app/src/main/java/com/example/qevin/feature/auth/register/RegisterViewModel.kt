package com.example.qevin.feature.auth.register

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.QevinUser
import com.example.qevin.domain.model.UserProfile
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.ProfileRepository
import com.example.qevin.domain.usecase.ValidateRegistrationInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateRegistrationInput: ValidateRegistrationInputUseCase,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var pendingVerifiedUser: QevinUser? = null
    private var isProfileCreationInProgress = false
    private var isProfileCreationCompleted = false

    // ---------------------------------------------------------
    // STEP 1: EMAIL REGISTRATION
    // ---------------------------------------------------------

    fun onCreateAccountClicked(
        displayName: String,
        email: String,
        phoneNumber: String,
        password: String
    ) {
        val normalizedName = displayName.trim()
        val normalizedEmail = email.trim()
        val normalizedPhone = phoneNumber.trim()

        if (_uiState.value.isLoading) return

        if (_uiState.value.isEmailAccountCreated) {
            _uiState.update {
                it.copy(
                    errorMessage = "Email account is already created. Continue with phone verification."
                )
            }
            return
        }

        // Phone number is not required during email registration.
        val validationError = validateRegistrationInput(
            displayName = normalizedName,
            phoneNumber = normalizedPhone,
            password = password,
            requirePhoneNumber = false
        )

        if (validationError != null) {
            _uiState.update {
                it.copy(errorMessage = validationError)
            }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            _uiState.update {
                it.copy(errorMessage = "Enter a valid email address.")
            }
            return
        }

        _uiState.update {
            it.copy(
                displayName = normalizedName,
                email = normalizedEmail,
                phoneNumber = normalizedPhone,
                errorMessage = null,
                isLoading = true,
                isOtpSent = false,
                isPhoneVerified = false,
                isRegistrationCompleted = false,
                verificationId = null,
                otp = ""
            )
        }

        viewModelScope.launch {
            try {
                authRepository.registerWithEmail(
                    displayName = normalizedName,
                    email = normalizedEmail,
                    password = password
                ).onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailAccountCreated = true
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "Unable to create your account."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: "Something went wrong during registration."
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------
    // STEP 2: PHONE NUMBER VALIDATION
    // ---------------------------------------------------------

    private fun normalizePhoneNumber(phoneNumber: String): String? {
        val input = phoneNumber.trim()
        val digits = input.filter { it.isDigit() }

        return when {
            // Indian 10-digit mobile number.
            digits.length == 10 && !input.startsWith("+") ->
                "+91$digits"

            // Number entered as 91XXXXXXXXXX.
            digits.length == 12 && digits.startsWith("91") ->
                "+$digits"

            // Already entered in international format.
            input.startsWith("+") &&
                    digits.length in 11..15 ->
                "+$digits"

            else -> null
        }
    }

    // ---------------------------------------------------------
    // STEP 3: SEND PHONE OTP
    // ---------------------------------------------------------

    fun requestPhoneOtp(
        activity: Activity,
        phoneNumber: String
    ) {
        val currentState = _uiState.value

        if (currentState.isLoading) return

        if (!currentState.isEmailAccountCreated) {
            _uiState.update {
                it.copy(
                    errorMessage = "Please create your email account first."
                )
            }
            return
        }

        if (currentState.isPhoneVerified) return

        val normalizedPhone = normalizePhoneNumber(phoneNumber)

        if (normalizedPhone == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Enter a valid mobile number with country code, or a 10-digit Indian mobile number."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                phoneNumber = normalizedPhone,
                errorMessage = null,
                isLoading = true
            )
        }

        authRepository.sendPhoneOtp(
            activity = activity,
            phoneNumber = normalizedPhone,

            onOtpCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(
                        verificationId = verificationId,
                        isOtpSent = true,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            },

            onAutoVerified = { user ->
                completeRegistration(user)
            },

            onOtpRequestFailed = { errorMessage ->
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        )
    }

    // ---------------------------------------------------------
    // STEP 4: MANUAL OTP VERIFICATION
    // ---------------------------------------------------------

    fun onOtpChanged(otp: String) {
        _uiState.update {
            it.copy(
                otp = otp.filter { character -> character.isDigit() }
                    .take(6),
                errorMessage = null
            )
        }
    }

    fun onVerifyOtpClicked() {
        val currentState = _uiState.value

        if (currentState.isLoading) return

        val verificationId = currentState.verificationId

        if (verificationId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please request an OTP first.")
            }
            return
        }

        if (currentState.otp.length != 6) {
            _uiState.update {
                it.copy(errorMessage = "Enter the 6-digit OTP.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                authRepository.verifyPhoneOtp(
                    verificationId = verificationId,
                    otp = currentState.otp
                ).onSuccess { user ->
                    completeRegistration(user)
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "OTP verification failed."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: "Unable to verify OTP."
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------
    // STEP 5: CREATE USER PROFILE
    // ---------------------------------------------------------

    private fun completeRegistration(user: QevinUser) {
        if (isProfileCreationInProgress || isProfileCreationCompleted) {
            return
        }

        pendingVerifiedUser = user
        isProfileCreationInProgress = true

        _uiState.update {
            it.copy(
                isLoading = true,
                isPhoneVerified = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                val profile = UserProfile(
                    uid = user.id,
                    displayName = user.displayName.ifBlank {
                        currentState.displayName
                    },
                    username = null,
                    email = user.email?.ifBlank {
                        currentState.email
                    },
                    phoneNumber = user.phoneNumber?.ifBlank {
                        currentState.phoneNumber
                    },
                    photoUrl = user.photoUrl,
                    about = "",
                    createdAt = null,
                    updatedAt = null
                )

                // Avoid creating duplicate profiles on retry.
                val existingProfileResult =
                    profileRepository.getProfile(user.id)

                existingProfileResult.onFailure { exception ->
                    throw exception
                }

                val existingProfile = existingProfileResult.getOrNull()

                if (existingProfile == null) {
                    val createResult =
                        profileRepository.createProfile(profile)

                    createResult.onFailure { exception ->
                        throw exception
                    }
                }

                isProfileCreationCompleted = true
                pendingVerifiedUser = null

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPhoneVerified = true,
                        isRegistrationCompleted = true,
                        errorMessage = null
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPhoneVerified = true,
                        isRegistrationCompleted = false,
                        errorMessage = e.message
                            ?: "Profile creation failed. Please try again."
                    )
                }
            } finally {
                isProfileCreationInProgress = false
            }
        }
    }

    // ---------------------------------------------------------
    // STEP 6: RETRY PROFILE CREATION
    // ---------------------------------------------------------

    fun retryProfileCreation() {
        val user = pendingVerifiedUser ?: return

        if (isProfileCreationInProgress || isProfileCreationCompleted) {
            return
        }

        completeRegistration(user)
    }

    // ---------------------------------------------------------
    // UI STATE HELPERS
    // ---------------------------------------------------------

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null
            )
        }
    }
}
