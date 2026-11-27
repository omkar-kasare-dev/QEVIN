package com.example.qevin.presentation.calls

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.CallHistory
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.CallRepository
import com.example.qevin.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CallsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val callRepository: CallRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /** Internal list state. Holds domain calls; mapped to UI models at the end. */
    private data class ListState(
        val isLoading: Boolean,
        val calls: List<CallHistory> = emptyList(),
        val isOffline: Boolean = false,
        val errorMessage: String? = null
    )

    private val retryTrigger = MutableStateFlow(0)

    // The UID the current state belongs to. Resubscribing for the same user
    // keeps the existing list and profiles; a different user starts clean.
    private var lastUid: String? = null

    private val profileCache = MutableStateFlow<Map<String, ParticipantProfile>>(emptyMap())
    private val requestedUids = mutableSetOf<String>()

    val uiState: StateFlow<CallsUiState> = retryTrigger
        .flatMapLatest { observeForCurrentUser() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = CallsUiState()
        )

    fun retry() {
        lastUid = null // forces the loading state on restart
        resetProfiles()
        retryTrigger.update { it + 1 }
    }

    private fun observeForCurrentUser(): Flow<CallsUiState> =
        authRepository.currentUser
            .distinctUntilChangedBy { it?.id }
            .flatMapLatest { user ->
                if (user == null) {
                    lastUid = null
                    resetProfiles()
                    flowOf(CallsUiState(isLoading = false, currentUid = null))
                } else {
                    // Clear before collecting so a previous user's profiles never leak.
                    if (lastUid != user.id) resetProfiles()
                    observeCalls(user.id)
                }
            }

    private fun observeCalls(uid: String): Flow<CallsUiState> =
        combine(listStates(uid), profileCache) { state, profiles ->
            CallsUiState(
                isLoading = state.isLoading,
                currentUid = uid,
                calls = state.calls
                    .sortedWith(
                        compareByDescending<CallHistory> { it.startedAt }
                            .thenBy { it.id }
                    )
                    .map { CallUiMapper.map(it, uid, profiles) },
                isOffline = state.isOffline,
                errorMessage = state.errorMessage
            )
        }

    private fun listStates(uid: String): Flow<ListState> =
        callRepository.observeCallHistory(uid)
            .transformLatest { snapshot ->

                ensureProfiles(uid, snapshot.calls)

                // Cache-only and empty: we can't yet tell "no calls" from
                // "not loaded", so keep loading until the server answers
                // or the offline delay passes.
                val awaitingServer = snapshot.isFromCache && snapshot.calls.isEmpty()

                emit(
                    ListState(
                        isLoading = awaitingServer,
                        calls = snapshot.calls
                    )
                )

                if (snapshot.isFromCache) {
                    // transformLatest cancels this if the server snapshot arrives.
                    delay(OFFLINE_DELAY_MS)
                    emit(
                        ListState(
                            isLoading = false,
                            calls = snapshot.calls,
                            isOffline = true
                        )
                    )
                }
            }
            .onStart {
                if (lastUid != uid) {
                    emit(ListState(isLoading = true))
                }
                lastUid = uid
            }
            .catch { error ->
                Log.e(TAG, "Call history listener failed", error)
                emit(
                    ListState(
                        isLoading = false,
                        errorMessage = error.toUserMessage()
                    )
                )
            }

    /**
     * Fetches profiles for participants not requested yet, in the background,
     * so rows render immediately with "Unknown user" and fill in as profiles arrive.
     */
    private fun ensureProfiles(uid: String, calls: List<CallHistory>) {
        val wanted = calls
            .flatMap { it.participantIds }
            .filter { it != uid }
            .toSet()

        val missing = wanted - requestedUids
        if (missing.isEmpty()) return

        requestedUids += missing

        viewModelScope.launch {
            profileRepository.getParticipantProfiles(missing)
                .onSuccess { fetched ->
                    if (lastUid == uid) {
                        profileCache.update { it + fetched.associateBy { p -> p.uid } }
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Profile fetch failed", error)
                    if (lastUid == uid) requestedUids -= missing
                }
        }
    }

    private fun resetProfiles() {
        profileCache.value = emptyMap()
        requestedUids.clear()
    }

    private fun Throwable.toUserMessage(): String =
        when ((this as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "You don't have permission to view call history."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Can't reach the server. Check your connection."
            else -> "Couldn't load call history."
        }

    private companion object {
        const val TAG = "QEVIN_CALLS_VM"
        const val STOP_TIMEOUT_MS = 5_000L
        const val OFFLINE_DELAY_MS = 2_000L
    }
}