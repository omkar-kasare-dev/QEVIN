package com.example.qevin.feature.auth.register

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.usecase.ValidateRegistrationInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateRegistrationInput: ValidateRegistrationInputUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // STEP 1: Create account using email and password
    /*
    fun onCreateAccountClicked(
        activity: Activity,
        displayName: String,
        email: String,
        phoneNumber: String,
        password: String
    ) {
        val error = validateRegistrationInput(
            displayName = displayName,
            email=email,
            phoneNumber = phoneNumber,
            password = password
        )

        if (error != null) {
            _uiState.update {
                it.copy(errorMessage = error)
            }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a valid email address.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                displayName = displayName.trim(),
                email = email.trim(),
                phoneNumber = phoneNumber.trim(),
                isOtpSent = false,
                isPhoneVerified = false,
                isRegistrationCompleted = false,
                verificationId = null
            )
        }

        viewModelScope.launch {
            try {
                val result = authRepository.registerWithEmail(
                    displayName = displayName.trim(),
                    email = email.trim(),
                    password = password
                )

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }

                        // Email account created.
                        // Now request phone OTP.
                        requestPhoneOtp(
                            activity = activity,
                            phoneNumber = phoneNumber.trim()
                        )
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message
                                    ?: "Account registration failed."
                            )
                        }
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Something went wrong."
                    )
                }
            }
        }
    }
*/
    //-------------------------------------

    fun onCreateAccountClicked(
        displayName: String,
        email: String,
        phoneNumber: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = authRepository.registerWithEmail(
                displayName = displayName.trim(),
                email = email.trim(),
                password = password
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailAccountCreated = true,
                            displayName = displayName.trim(),
                            email = email.trim(),
                            phoneNumber = phoneNumber.trim()
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailAccountCreated = false,
                            errorMessage = exception.message
                                ?: "Unable to create email account."
                        )
                    }
                }
            )
        }
    }

    //-----------------------------------------------------------------------
    // STEP 2: Request phone OTP
    fun requestPhoneOtp(
        activity: Activity,
        phoneNumber: String
    ) {

        if (!_uiState.value.isEmailAccountCreated) {
            _uiState.update {
                it.copy(
                    errorMessage = "Please create your email account first."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        authRepository.sendPhoneOtp(
            activity = activity,
            phoneNumber = phoneNumber,
            onOtpCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        verificationId = verificationId,
                        errorMessage = null
                    )
                }
            },
            onAutoVerified = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPhoneVerified = true,
                        isRegistrationCompleted = true,
                        isOtpSent = false,
                        errorMessage = null
                    )
                }
            },
            onOtpRequestFailed = { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to send OTP. Please try again."
                    )
                }
            }
        )
    }

    // STEP 3: Store OTP input
    fun onOtpChanged(otp: String) {
        _uiState.update {
            it.copy(
                otp = otp.filter(Char::isDigit).take(6),
                errorMessage = null
            )
        }
    }

    // STEP 4: Verify manually entered OTP
    fun onVerifyOtpClicked() {
        val currentState = _uiState.value
        val verificationId = currentState.verificationId

        if (verificationId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please request an OTP first.")
            }
            return
        }

        if (currentState.otp.length != 6) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a valid 6-digit OTP.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val result = authRepository.verifyPhoneOtp(
                    verificationId = verificationId,
                    otp = currentState.otp
                )

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPhoneVerified = true,
                                isRegistrationCompleted = true,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message
                                    ?: "OTP verification failed."
                            )
                        }
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Something went wrong."
                    )
                }
            }
        }
    }

    // STEP 5: Clear error
    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}
