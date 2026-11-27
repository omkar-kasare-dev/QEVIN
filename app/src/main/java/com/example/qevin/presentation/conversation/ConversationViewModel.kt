package com.example.qevin.presentation.conversation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.Message
import com.example.qevin.domain.model.MessageType
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.ConversationRepository
import com.example.qevin.domain.repository.MessageRepository
import com.example.qevin.domain.repository.PresenceRepository
import com.example.qevin.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

/**
 * What the conversation header needs. [title] is null while it is still
 * loading, or when the ID is a local sample (not a real conversation); the
 * screen then falls back to its sample-name lookup.
 */
data class ConversationUiState(
    val isLoading: Boolean = true,
    val title: String? = null,
    val initials: String = "",
    val photoUrl: String? = null,
    val isGroup: Boolean = false,
    val isOnline: Boolean = false,
    // "Online", "Offline", "3 members". Null hides the status line.
    val statusLabel: String? = null
)

data class MessageUiModel(
    val id: String,
    val text: String,
    val timeLabel: String,
    val isSent: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val profileRepository: ProfileRepository,
    private val presenceRepository: PresenceRepository
) : ViewModel() {

    /** A lookup that has either not finished yet ([done] = false) or finished with [value]. */
    private data class Lookup<T>(val done: Boolean, val value: T? = null)

    private val conversationId: String =
        savedStateHandle.get<String>("conversationId").orEmpty()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    // After a failed mark-as-read, stop retrying automatically. A rejected
    // write reverts the local count, which would otherwise trigger a retry loop.
    private var markReadFailed = false

    // True once the conversation document exists. The security rules deny
    // reading messages of a missing conversation, so the message listener
    // waits for this. Fed by the header's conversation listener (no extra
    // listener on the conversation document).
    private val conversationExists = MutableStateFlow(false)

    val uiState: StateFlow<ConversationUiState> = authRepository.currentUser
        .distinctUntilChangedBy { it?.id }
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(ConversationUiState(isLoading = false))
            } else {
                observeHeader(user.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            // Stops listening (and marking as read) shortly after the screen
            // leaves the foreground.
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ConversationUiState()
        )

    val messages: StateFlow<List<MessageUiModel>> = authRepository.currentUser
        .distinctUntilChangedBy { it?.id }
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList<MessageUiModel>())
            } else {
                observeMessages(user.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.first()?.id

            if (uid == null) {
                _errors.tryEmit("You are signed out.")
                return@launch
            }

            // Only direct conversations can be written for now. Sample IDs
            // such as "rahul" and group conversations are read-only.
            if (ConversationIds.otherParticipant(conversationId, uid) == null) {
                Log.d(TAG, "Sending not supported for '$conversationId'.")
                _errors.tryEmit("Sending messages isn't available in this conversation yet.")
                return@launch
            }

            messageRepository
                .sendTextMessage(conversationId, uid, text)
                .onSuccess { Log.d(TAG, "Message sent to $conversationId") }
                .onFailure { error ->
                    Log.e(TAG, "Send failed", error)
                    _errors.tryEmit(error.message ?: "Message could not be sent.")
                }
        }
    }

    // --------------------------------------------------
    // MESSAGES
    // --------------------------------------------------

    private fun observeMessages(uid: String): Flow<List<MessageUiModel>> =
        conversationExists
            .flatMapLatest { exists ->
                if (!exists) {
                    flowOf(emptyList<MessageUiModel>())
                } else {
                    messageRepository.observeMessages(conversationId)
                        .map { list -> list.mapNotNull { toUiModel(it, uid) } }
                        .catch { error ->
                            Log.w(TAG, "Message listener failed", error)
                            emit(emptyList())
                        }
                }
            }

    private fun toUiModel(message: Message, uid: String): MessageUiModel? {
        val body = when (message.type) {
            MessageType.TEXT -> message.text.trim().ifEmpty { return null }
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VOICE -> "🎤 Voice message"
            MessageType.UNKNOWN -> "Message unavailable"
        }

        return MessageUiModel(
            id = message.id,
            text = body,
            timeLabel = message.sentAt?.let(::formatTime).orEmpty(),
            isSent = message.senderId == uid
        )
    }

    private fun formatTime(epochMillis: Long): String =
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(epochMillis))

    // --------------------------------------------------
    // HEADER
    // --------------------------------------------------

    private fun observeHeader(uid: String): Flow<ConversationUiState> {
        // Direct IDs are deterministic, so the other person is known without
        // reading the document. Null for groups and for sample IDs.
        val otherUid = ConversationIds.otherParticipant(conversationId, uid)

        return combine(
            conversationFlow(uid),
            profileFlow(otherUid),
            presenceFlow(otherUid)
        ) { conversation, profile, online ->
            buildState(otherUid, conversation, profile, online)
        }
    }

    private fun conversationFlow(uid: String): Flow<Lookup<Conversation>> {
        if (conversationId.isBlank()) return flowOf(Lookup<Conversation>(done = true))

        return conversationRepository.observeConversation(conversationId)
            .onEach { conversation ->
                conversationExists.value = conversation != null
                markReadIfNeeded(uid, conversation)
            }
            .map { Lookup(done = true, value = it) }
            .catch { error ->
                Log.w(TAG, "Conversation listener failed", error)
                emit(Lookup<Conversation>(done = true))
            }
            .onStart { emit(Lookup<Conversation>(done = false)) }
    }

    private fun profileFlow(otherUid: String?): Flow<Lookup<ParticipantProfile>> {
        if (otherUid == null) return flowOf(Lookup<ParticipantProfile>(done = true))

        return flow {
            emit(Lookup<ParticipantProfile>(done = false))

            val result = profileRepository.getParticipantProfiles(setOf(otherUid))
            result.exceptionOrNull()?.let { Log.w(TAG, "Profile fetch failed", it) }

            emit(
                Lookup(
                    done = true,
                    value = result.getOrNull()?.firstOrNull()
                )
            )
        }
    }

    private fun presenceFlow(otherUid: String?): Flow<Map<String, Boolean>> =
        if (otherUid == null) {
            flowOf(emptyMap())
        } else {
            presenceRepository.observeOnline(setOf(otherUid))
        }

    private fun buildState(
        otherUid: String?,
        conversation: Lookup<Conversation>,
        profile: Lookup<ParticipantProfile>,
        online: Map<String, Boolean>
    ): ConversationUiState {

        val loaded = conversation.value

        if (loaded != null && loaded.type == ConversationType.GROUP) {
            val name = loaded.name ?: "Group"
            val count = loaded.participantIds.size

            return ConversationUiState(
                isLoading = false,
                title = name,
                initials = initialsOf(name),
                photoUrl = loaded.photoUrl?.takeIf(::isRemote),
                isGroup = true,
                statusLabel = if (count == 1) "1 member" else "$count members"
            )
        }

        if (otherUid != null) {
            val other = profile.value
            val title = when {
                other != null ->
                    other.displayName.takeIf { it.isNotBlank() }
                        ?: other.username?.takeIf { it.isNotBlank() }
                        ?: "Unknown user"
                profile.done -> "Unknown user"
                else -> null // still loading
            }
            val isOnline = online[otherUid] == true

            return ConversationUiState(
                isLoading = title == null,
                title = title,
                initials = title?.let(::initialsOf).orEmpty(),
                photoUrl = other?.photoUrl,
                isGroup = false,
                isOnline = isOnline,
                statusLabel = if (isOnline) "Online" else "Offline"
            )
        }

        // Not a real conversation (a local sample ID, or a group that has not
        // loaded yet). The screen decides what to show.
        return ConversationUiState(isLoading = !conversation.done)
    }

    // --------------------------------------------------
    // MARK AS READ
    // --------------------------------------------------

    /**
     * Runs on every conversation snapshot while the screen is visible.
     * The write updates the local cache immediately, so the next snapshot
     * shows 0 and this does not repeat. A message that arrives while the
     * chat is open raises the count again and is cleared the same way.
     */
    private fun markReadIfNeeded(uid: String, conversation: Conversation?) {
        if (conversation == null || markReadFailed) return
        if (uid !in conversation.participantIds) return
        if (conversation.unreadCountFor(uid) <= 0) return

        viewModelScope.launch {
            messageRepository.markConversationRead(conversationId, uid)
                .onFailure { error ->
                    Log.w(TAG, "Mark-as-read failed", error)
                    markReadFailed = true
                }
        }
    }

    private fun isRemote(url: String) =
        url.startsWith("http://") || url.startsWith("https://")

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private companion object {
        const val TAG = "QEVIN_SEND"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}