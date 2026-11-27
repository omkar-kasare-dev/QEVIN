package com.example.qevin.presentation.calls

import com.example.qevin.domain.model.CallHistory
import com.example.qevin.domain.model.CallStatus
import com.example.qevin.domain.model.CallType
import com.example.qevin.domain.model.CallDirection
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.model.ParticipantProfile

data class CallUiModel(
    val id: String,
    val title: String,
    val initials: String,
    val photoUrl: String?,
    val subtitle: String,
    val timeLabel: String,
    val startedAt: Long,
    val isMissed: Boolean,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isGroupCall: Boolean,
    // Seed for the avatar colour. Same rule as Chats: other user's UID.
    val colorSeed: String,
    // Chat to open when the row is tapped. Null when none can be determined.
    val conversationId: String?
)

object CallUiMapper {

    fun map(
        call: CallHistory,
        currentUid: String,
        profiles: Map<String, ParticipantProfile>
    ): CallUiModel {

        val others = call.otherParticipantIds(currentUid)
        val otherUid = others.firstOrNull()
        val isGroup = call.isGroupCall

        val title = if (isGroup) {
            groupTitle(others, profiles)
        } else {
            val other = otherUid?.let(profiles::get)
            other?.displayName?.takeIf { it.isNotBlank() }
                ?: other?.username?.takeIf { it.isNotBlank() }
                ?: "Unknown user"
        }

        return CallUiModel(
            id = call.id,
            title = title,
            initials = initialsOf(title),
            photoUrl = if (isGroup) null else otherUid?.let(profiles::get)?.photoUrl,
            subtitle = subtitleOf(call),
            timeLabel = CallTimeFormatter.format(call.startedAt),
            startedAt = call.startedAt,
            isMissed = call.isMissedIncoming,
            isVideo = call.callType == CallType.VIDEO,
            isIncoming = call.direction == CallDirection.INCOMING,
            isGroupCall = isGroup,
            colorSeed = otherUid ?: call.id,
            conversationId = conversationIdFor(call, currentUid, otherUid)
        )
    }

    private fun subtitleOf(call: CallHistory): String {
        val parts = mutableListOf<String>()

        parts += when {
            call.isMissedIncoming -> "Missed"
            call.direction == CallDirection.INCOMING -> "Incoming"
            else -> "Outgoing"
        }

        parts += if (call.callType == CallType.VIDEO) "Video" else "Audio"

        when {
            call.status == CallStatus.MISSED && !call.isMissedIncoming -> parts += "No answer"
            call.status == CallStatus.REJECTED -> parts += "Declined"
        }

        call.durationMillis
            ?.let(CallTimeFormatter::duration)
            ?.let { parts += it }

        return parts.joinToString(" · ")
    }

    private fun groupTitle(
        others: List<String>,
        profiles: Map<String, ParticipantProfile>
    ): String {
        val names = others.mapNotNull { uid ->
            profiles[uid]?.displayName
                ?.trim()
                ?.substringBefore(' ')
                ?.takeIf { it.isNotBlank() }
        }

        if (names.isEmpty()) return "Group call"

        val shown = names.take(2).joinToString(", ")
        val extra = others.size - 2
        return if (extra > 0) "$shown +$extra" else shown
    }

    private fun conversationIdFor(
        call: CallHistory,
        currentUid: String,
        otherUid: String?
    ): String? {
        call.conversationId?.let { return it }
        if (call.isGroupCall || otherUid == null) return null
        return runCatching { ConversationIds.direct(currentUid, otherUid) }.getOrNull()
    }

    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}