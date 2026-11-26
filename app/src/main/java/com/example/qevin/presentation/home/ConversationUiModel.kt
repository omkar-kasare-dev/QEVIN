package com.example.qevin.presentation.home

import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.ParticipantProfile

data class ConversationUiModel(
    val id: String,
    val title: String,
    val initials: String,
    val photoUrl: String?,
    val preview: String,
    val lastActivityAt: Long,
    val timeLabel: String,
    val unreadCount: Int,
    val unreadLabel: String,
    val isGroup: Boolean,
    val isOnline: Boolean = false,
    val otherUid: String? = null
)

object ConversationUiMapper {

    fun map(
        conversation: Conversation,
        currentUid: String,
        profiles: Map<String, ParticipantProfile>,
        // uid -> isOnline. A uid missing from the map is treated as offline.
        presence: Map<String, Boolean> = emptyMap()
    ): ConversationUiModel {

        val isGroup = conversation.type == ConversationType.GROUP
        val otherUid = conversation.otherParticipantIds(currentUid).firstOrNull()
        val other = otherUid?.let(profiles::get)

        val title = if (isGroup) {
            conversation.name ?: "Group"
        } else {
            other?.displayName?.takeIf { it.isNotBlank() }
                ?: other?.username?.takeIf { it.isNotBlank() }
                ?: "Unknown user"
        }

        val senderName = conversation.lastMessage
            ?.senderId
            ?.let(profiles::get)
            ?.displayName

        val unread = conversation.unreadCountFor(currentUid)

        return ConversationUiModel(
            id = conversation.id,
            title = title,
            initials = initialsOf(title),
            photoUrl = if (isGroup) conversation.photoUrl?.takeIf(::isRemote) else other?.photoUrl,
            preview = LastMessagePreviewFormatter.format(conversation, currentUid, senderName),
            lastActivityAt = conversation.lastActivityAt,
            timeLabel = ConversationTimeFormatter.format(conversation.lastActivityAt),
            unreadCount = unread,
            unreadLabel = ConversationTimeFormatter.unreadBadge(unread),
            isGroup = isGroup,
            isOnline = !isGroup && otherUid != null && presence[otherUid] == true,
            otherUid = if (isGroup) null else otherUid
        )
    }

    private fun isRemote(url: String) =
        url.startsWith("http://") || url.startsWith("https://")

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}