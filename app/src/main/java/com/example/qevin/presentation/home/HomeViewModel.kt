package com.example.qevin.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.model.QevinUser
import com.example.qevin.domain.model.UserProfile
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.ConversationRepository
import com.example.qevin.domain.repository.PresenceRepository
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
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val profileRepository: ProfileRepository,
    private val presenceRepository: PresenceRepository
) : ViewModel() {

    /** Internal list state. Holds domain conversations; mapped to UI models at the end. */
    private data class ListState(
        val isLoading: Boolean,
        val conversations: List<Conversation> = emptyList(),
        val isOffline: Boolean = false,
        val errorMessage: String? = null
    )

    private val retryTrigger = MutableStateFlow(0)

    // The UID the current state belongs to. Resubscribing for the same user
    // keeps the existing list and profiles; a different user starts clean.
    private var lastUid: String? = null

    // Ensure-profile: the UID whose profile check has already run (or is running).
    private var profileEnsuredFor: String? = null

    private val profileCache = MutableStateFlow<Map<String, ParticipantProfile>>(emptyMap())
    private val requestedUids = mutableSetOf<String>()

    // Other users in DIRECT conversations whose presence we watch. Updated from
    // the same Firestore snapshot that feeds the list, so no second
    // conversation listener is opened.
    private val visibleDirectUids = MutableStateFlow<Set<String>>(emptySet())

    private val presence: Flow<Map<String, Boolean>> = visibleDirectUids
        .flatMapLatest { uids -> presenceRepository.observeOnline(uids) }

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest { observeForCurrentUser() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeUiState()
        )

    fun retry() {
        lastUid = null // forces the loading state on restart
        profileEnsuredFor = null // Ensure-profile: try again too
        resetProfiles()
        retryTrigger.update { it + 1 }
    }

    private fun observeForCurrentUser(): Flow<HomeUiState> =
        authRepository.currentUser
            .distinctUntilChangedBy { it?.id } // Ensure-profile: keep the user, not just the id
            .flatMapLatest { user ->
                if (user == null) {
                    lastUid = null
                    profileEnsuredFor = null
                    resetProfiles()
                    flowOf(HomeUiState(isLoading = false, currentUid = null))
                } else {
                    // Clear before collecting so a previous user's profiles never leak.
                    if (lastUid != user.id) resetProfiles()
                    ensureOwnProfile(user)
                    observeConversations(user.id)
                }
            }

    /**
     * Ensure-profile: accounts created before Phase 1, or abandoned between
     * email sign-up and phone verification, have no users/{uid} document and
     * would show as "Unknown user" to everyone. Creates it once per signed-in
     * user if missing; never overwrites an existing profile.
     */
    private fun ensureOwnProfile(user: QevinUser) {
        if (profileEnsuredFor == user.id) return
        profileEnsuredFor = user.id

        viewModelScope.launch {
            val profile = UserProfile(
                uid = user.id,
                displayName = user.displayName.ifBlank {
                    user.email?.substringBefore('@').orEmpty()
                },
                email = user.email?.takeIf { it.isNotBlank() },
                phoneNumber = user.phoneNumber?.takeIf { it.isNotBlank() },
                photoUrl = user.photoUrl
            )

            profileRepository.ensureProfile(profile)
                .onSuccess { created ->
                    if (created) {
                        Log.i(TAG, "Created missing profile for ${user.id.take(6)}")
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Ensure-profile failed", error)
                    // Allow another attempt on retry() or the next Home start.
                    if (profileEnsuredFor == user.id) profileEnsuredFor = null
                }
        }
    }

    private fun observeConversations(uid: String): Flow<HomeUiState> =
        combine(listStates(uid), profileCache, presence) { state, profiles, online ->
            HomeUiState(
                isLoading = state.isLoading,
                currentUid = uid,
                conversations = state.conversations
                    .sortedWith(
                        compareByDescending<Conversation> { it.lastActivityAt }
                            .thenBy { it.id }
                    )
                    .map { ConversationUiMapper.map(it, uid, profiles, online) },
                isOffline = state.isOffline,
                errorMessage = state.errorMessage
            )
        }

    private fun listStates(uid: String): Flow<ListState> =
        conversationRepository.observeConversations(uid)
            .transformLatest { snapshot ->

                ensureProfiles(uid, snapshot.conversations)
                updateWatchedPresence(uid, snapshot.conversations)

                // Cache-only and empty: we can't yet tell "no conversations"
                // from "not loaded", so keep loading until the server answers
                // or the offline delay passes.
                val awaitingServer =
                    snapshot.isFromCache && snapshot.conversations.isEmpty()

                emit(
                    ListState(
                        isLoading = awaitingServer,
                        conversations = snapshot.conversations
                    )
                )

                if (snapshot.isFromCache) {
                    // transformLatest cancels this if the server snapshot arrives.
                    delay(OFFLINE_DELAY_MS)
                    emit(
                        ListState(
                            isLoading = false,
                            conversations = snapshot.conversations,
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
                // A missing index logs a FAILED_PRECONDITION message with a
                // link to create it.
                Log.e(TAG, "Conversation listener failed", error)
                emit(
                    ListState(
                        isLoading = false,
                        errorMessage = error.toUserMessage()
                    )
                )
            }

    /**
     * Fetches profiles for participants not requested yet. Runs in the
     * background so the list renders immediately (with "Unknown user"
     * placeholders) and fills in names as profiles arrive.
     */
    private fun ensureProfiles(uid: String, conversations: List<Conversation>) {
        val wanted = conversations
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

    /**
     * Only direct chats show an online dot, so only the other person in each
     * DIRECT conversation is watched. Groups are skipped.
     */
    private fun updateWatchedPresence(uid: String, conversations: List<Conversation>) {
        visibleDirectUids.value = conversations
            .filter { it.type == ConversationType.DIRECT }
            .flatMap { it.otherParticipantIds(uid) }
            .toSet()
    }

    private fun resetProfiles() {
        profileCache.value = emptyMap()
        requestedUids.clear()
        visibleDirectUids.value = emptySet()
    }

    private fun Throwable.toUserMessage(): String =
        when ((this as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "You don't have permission to view conversations."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Can't reach the server. Check your connection."
            else -> "Couldn't load conversations."
        }

    private companion object {
        const val TAG = "QEVIN_HOME"
        const val STOP_TIMEOUT_MS = 5_000L
        const val OFFLINE_DELAY_MS = 2_000L
    }
}