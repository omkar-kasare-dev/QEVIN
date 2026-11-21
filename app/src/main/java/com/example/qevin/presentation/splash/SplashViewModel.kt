package com.example.qevin.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SplashDestination {
    ONBOARDING,
    HOME
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "QEVIN_SPLASH"
    }

    private val _destination =
        MutableStateFlow<SplashDestination?>(null)

    val destination: StateFlow<SplashDestination?> =
        _destination.asStateFlow()

    init {
        checkAuthenticationSession()
    }

    private fun checkAuthenticationSession() {
        viewModelScope.launch {

            Log.d(TAG, "Checking Firebase authentication session")

            try {
                val currentUser = authRepository.currentUser.first()

                if (currentUser != null) {
                    Log.d(TAG, "Existing user session found")
                    _destination.value = SplashDestination.HOME
                } else {
                    Log.d(TAG, "No authenticated session found")
                    _destination.value = SplashDestination.ONBOARDING
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                Log.e(TAG, "Session check failed", exception)

                _destination.value = SplashDestination.ONBOARDING
            }
        }
    }
}
