package com.example.qevin.presentation.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.UserSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Shortest query that is searched. Shared with the screen. */
internal const val MIN_QUERY_LENGTH = 2

enum class SearchStatus { IDLE, SEARCHING, RESULTS, EMPTY, ERROR }

data class UserSearchResultUi(
    val uid: String,
    // Deterministic direct conversation with this user.
    val conversationId: String,
    val name: String,
    val initials: String,
    val username: String?,
    val photoUrl: String?
)

data class NewConversationUiState(
    val status: SearchStatus = SearchStatus.IDLE,
    val results: List<UserSearchResultUi> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewConversationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSearchRepository: UserSearchRepository
) : ViewModel() {

    private data class SearchInput(val query: String, val uid: String?)

    private val query = MutableStateFlow("")
    private val retryTrigger = MutableStateFlow(0)

    // Kept while a newer search runs, so the list doesn't flash empty
    // between keystrokes.
    private var lastResults: List<UserSearchResultUi> = emptyList()

    val uiState: StateFlow<NewConversationUiState> = combine(
        query
            .map(::normalize)
            .distinctUntilChanged()
            // Too-short input clears immediately; real searches wait for a pause.
            .debounce { text -> if (text.length < MIN_QUERY_LENGTH) 0L else DEBOUNCE_MS },
        authRepository.currentUser
            .map { it?.id }
            .distinctUntilChanged(),
        retryTrigger
    ) { text, uid, _ -> SearchInput(text, uid) }
        .flatMapLatest(::search)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = NewConversationUiState()
        )

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    private fun search(input: SearchInput): Flow<NewConversationUiState> = flow {
        val uid = input.uid

        if (uid == null || input.query.length < MIN_QUERY_LENGTH) {
            lastResults = emptyList()
            emit(NewConversationUiState())
            return@flow
        }

        emit(NewConversationUiState(status = SearchStatus.SEARCHING, results = lastResults))

        val outcome = userSearchRepository.searchUsers(input.query, uid)
        val profiles = outcome.getOrNull()

        if (profiles == null) {
            Log.w(TAG, "User search failed", outcome.exceptionOrNull())
            lastResults = emptyList()
            emit(
                NewConversationUiState(
                    status = SearchStatus.ERROR,
                    errorMessage = "Couldn't search right now. Check your connection."
                )
            )
            return@flow
        }

        val results = profiles.mapNotNull { toUi(it, uid) }
        lastResults = results

        emit(
            NewConversationUiState(
                status = if (results.isEmpty()) SearchStatus.EMPTY else SearchStatus.RESULTS,
                results = results
            )
        )
    }

    private fun toUi(profile: ParticipantProfile, myUid: String): UserSearchResultUi? {
        // Fails only for yourself or a blank UID, which are never valid chats.
        val conversationId = runCatching {
            ConversationIds.direct(myUid, profile.uid)
        }.getOrNull() ?: return null

        val name = profile.displayName.takeIf { it.isNotBlank() }
            ?: profile.username?.takeIf { it.isNotBlank() }
            ?: "Unknown user"

        return UserSearchResultUi(
            uid = profile.uid,
            conversationId = conversationId,
            name = name,
            initials = initialsOf(name),
            username = profile.username?.takeIf { it.isNotBlank() },
            photoUrl = profile.photoUrl
        )
    }

    private fun normalize(text: String): String =
        text.trim().removePrefix("@").trim()

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private companion object {
        const val TAG = "QEVIN_USER_SEARCH"
        const val DEBOUNCE_MS = 300L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}