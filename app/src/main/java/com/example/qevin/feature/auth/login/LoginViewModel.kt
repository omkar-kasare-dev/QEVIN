package com.example.qevin.feature.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.usecase.ValidateLoginInputUseCase
import com.example.qevin.feature.auth.common.AuthUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateLoginInput: ValidateLoginInputUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "QEVIN_LOGIN_DEBUG"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<AuthUiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val navigationEventChannel =
        Channel<LoginNavigationEvent>(Channel.BUFFERED)

    val navigationEvents = navigationEventChannel.receiveAsFlow()

    // STEP 1: Handle login button click
    fun onLoginClicked(
        identifier: String,
        password: String
    ) {
        Log.d(TAG, "Login button clicked")

        val error = validateLoginInput(identifier, password)

        if (error != null) {
            Log.w(TAG, "Input validation failed: $error")

            _uiState.update {
                it.copy(errorMessage = error)
            }
            return
        }

        Log.d(TAG, "Input validation successful")

        _uiState.update {
            it.copy(errorMessage = null)
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending LoginRequested event")

                eventChannel.send(
                    AuthUiEvent.LoginRequested(
                        identifier = identifier.trim(),
                        password = password
                    )
                )

                Log.d(TAG, "LoginRequested event sent successfully")

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to send login event", exception)
            }
        }
    }

    // STEP 2: Collect login events
    init {
        Log.d(TAG, "LoginViewModel initialized")

        viewModelScope.launch {
            events.collect { event ->

                Log.d(TAG, "Authentication event received: ${event::class.simpleName}")

                when (event) {

                    is AuthUiEvent.LoginRequested -> {
                        val identifier = event.identifier.trim()

                        Log.d(TAG, "Login request received")

                        if (!identifier.contains("@")) {

                            Log.w(TAG, "Phone login is not supported in LoginViewModel")

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Please use your email address to log in."
                                )
                            }

                        } else {
                            loginWithEmail(
                                email = identifier,
                                password = event.password
                            )
                        }
                    }

                    else -> {
                        Log.w(TAG, "Unhandled authentication event")
                    }
                }
            }
        }
    }

    // STEP 3: Clear error
    fun clearError() {
        Log.d(TAG, "Clearing login error")

        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    // STEP 4: Authenticate using Firebase
    private fun loginWithEmail(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            Log.d(TAG, "Starting email authentication")

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                Log.d(TAG, "Calling AuthRepository.signInWithEmail()")

                val result = authRepository.signInWithEmail(
                    email = email.trim(),
                    password = password
                )

                Log.d(TAG, "Repository returned authentication result")

                result.fold(

                    onSuccess = {
                        Log.d(TAG, "Firebase authentication SUCCESS")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null
                            )
                        }

                        Log.d(TAG, "Login UI state updated: loading=false, error=null")

                        Log.d(TAG, "Emitting LoginSuccess navigation event")

                        navigationEventChannel.send(
                            LoginNavigationEvent.LoginSuccess
                        )
                    },

                    onFailure = { exception ->
                        Log.e(
                            TAG,
                            "Firebase authentication FAILED",
                            exception
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message
                                    ?: "Login failed. Please try again."
                            )
                        }

                        Log.d(TAG, "Login UI state updated with authentication error")
                    }
                )

            } catch (exception: CancellationException) {
                Log.w(TAG, "Login coroutine cancelled")
                throw exception

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unexpected exception during login",
                    exception
                )

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
}
