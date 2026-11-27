package com.example.qevin.domain.model

enum class CallType {
    AUDIO,
    VIDEO;

    val value: String
        get() = name.lowercase()

    companion object {
        fun fromValue(value: String?): CallType =
            if (value.equals("video", ignoreCase = true)) VIDEO else AUDIO
    }
}

enum class CallDirection {
    INCOMING,
    OUTGOING;

    val value: String
        get() = name.lowercase()

    companion object {
        /** Null when missing or unrecognised: a call without a direction cannot be shown correctly. */
        fun fromValue(value: String?): CallDirection? =
            entries.firstOrNull { it.value == value?.lowercase() }
    }
}

enum class CallStatus {
    COMPLETED,
    MISSED,
    REJECTED,
    UNKNOWN;

    val value: String
        get() = name.lowercase()

    companion object {
        fun fromValue(value: String?): CallStatus =
            entries.firstOrNull { it.value == value?.lowercase() } ?: UNKNOWN
    }
}

/**
 * One call record from the signed-in user's own point of view.
 * Each participant has their own copy, so [direction] and [status] are
 * already correct for the owner.
 */
data class CallHistory(
    val id: String,
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val participantIds: List<String>,
    // Lets a row open the related chat. Null when there is no conversation.
    val conversationId: String? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val createdAt: Long? = null
) {

    /** Only an incoming call that went unanswered is shown as "missed". */
    val isMissedIncoming: Boolean
        get() = direction == CallDirection.INCOMING && status == CallStatus.MISSED

    /** Length of a completed call, or null when unknown or invalid. */
    val durationMillis: Long?
        get() = if (status == CallStatus.COMPLETED) {
            endedAt?.minus(startedAt)?.takeIf { it >= 0L }
        } else {
            null
        }

    /** More than two participants means a group call. */
    val isGroupCall: Boolean
        get() = participantIds.size > 2

    fun otherParticipantIds(currentUid: String): List<String> =
        participantIds.filter { it != currentUid }
}

/** One emission of the call-history listener. Mirrors [ConversationsSnapshot]. */
data class CallHistorySnapshot(
    val currentUid: String,
    val calls: List<CallHistory>,
    val isFromCache: Boolean
)