package com.example.qevin.presentation.people

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.model.PersonUi
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.ConversationRepository
import com.example.qevin.domain.repository.PresenceRepository
import com.example.qevin.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val profileRepository: ProfileRepository,
    private val presenceRepository: PresenceRepository
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    // The UID the current state belongs to. A different user starts clean.
    private var activeUid: String? = null

    private val profileCache = MutableStateFlow<Map<String, ParticipantProfile>>(emptyMap())
    private val requestedUids = mutableSetOf<String>()
    private val watchedUids = MutableStateFlow<Set<String>>(emptySet())

    private val presence: Flow<Map<String, Boolean>> = watchedUids
        .flatMapLatest { uids -> presenceRepository.observeOnline(uids) }

    val uiState: StateFlow<PeopleUiState> = retryTrigger
        .flatMapLatest { observeForCurrentUser() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PeopleUiState()
        )

    fun retry() {
        activeUid = null // forces the loading state on restart
        reset()
        retryTrigger.update { it + 1 }
    }

    private fun observeForCurrentUser(): Flow<PeopleUiState> =
        authRepository.currentUser
            .map { it?.id }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) {
                    activeUid = null
                    reset()
                    flowOf(PeopleUiState(isLoading = false))
                } else {
                    val isNewUser = activeUid != uid
                    // Clear before collecting so a previous user's data never leaks.
                    if (isNewUser) reset()
                    activeUid = uid

                    observePeople(uid).onStart {
                        if (isNewUser) emit(PeopleUiState())
                    }
                }
            }

    private fun observePeople(uid: String): Flow<PeopleUiState> {
        val snapshots = conversationRepository.observeConversations(uid)
            .onEach { snapshot -> track(uid, snapshot.conversations) }

        return combine(snapshots, profileCache, presence) { snapshot, profiles, online ->
            PeopleUiState(
                isLoading = false,
                currentUid = uid,
                contacts = buildContacts(uid, snapshot.conversations, profiles, online)
            )
        }.catch { error ->
            Log.e(TAG, "People listener failed", error)
            emit(
                PeopleUiState(
                    isLoading = false,
                    currentUid = uid,
                    errorMessage = "Couldn't load your contacts."
                )
            )
        }
    }

    /** Fetches missing profiles and updates which users' presence is watched. */
    private fun track(uid: String, conversations: List<Conversation>) {
        val wanted = conversations
            .filter { it.type == ConversationType.DIRECT }
            .flatMap { it.otherParticipantIds(uid) }
            .toSet()

        watchedUids.value = wanted

        val missing = wanted - requestedUids
        if (missing.isEmpty()) return

        requestedUids += missing

        viewModelScope.launch {
            profileRepository.getParticipantProfiles(missing)
                .onSuccess { fetched ->
                    if (activeUid == uid) {
                        profileCache.update { it + fetched.associateBy { p -> p.uid } }
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "Profile fetch failed", error)
                    if (activeUid == uid) requestedUids -= missing
                }
        }
    }

    private fun buildContacts(
        uid: String,
        conversations: List<Conversation>,
        profiles: Map<String, ParticipantProfile>,
        online: Map<String, Boolean>
    ): List<PersonUi> =
        conversations
            .filter { it.type == ConversationType.DIRECT }
            .mapNotNull { conversation ->
                val otherUid = conversation.otherParticipantIds(uid).firstOrNull()
                    ?: return@mapNotNull null
                val profile = profiles[otherUid]

                val name = profile?.displayName?.takeIf { it.isNotBlank() }
                    ?: profile?.username?.takeIf { it.isNotBlank() }
                    ?: "Unknown user"

                PersonUi(
                    uid = otherUid,
                    conversationId = conversation.id,
                    name = name,
                    initials = initialsOf(name),
                    username = profile?.username?.takeIf { it.isNotBlank() },
                    photoUrl = profile?.photoUrl,
                    isOnline = online[otherUid] == true
                )
            }
            .distinctBy { it.uid }
            .sortedWith(compareBy({ it.name.lowercase() }, { it.uid }))

    private fun reset() {
        profileCache.value = emptyMap()
        requestedUids.clear()
        watchedUids.value = emptySet()
    }

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private companion object {
        const val TAG = "QEVIN_PEOPLE"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}