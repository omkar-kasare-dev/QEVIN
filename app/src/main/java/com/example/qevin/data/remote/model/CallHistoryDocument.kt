package com.example.qevin.data.remote.model

import com.example.qevin.domain.model.CallDirection
import com.example.qevin.domain.model.CallHistory
import com.example.qevin.domain.model.CallStatus
import com.example.qevin.domain.model.CallType
import java.util.Date

/**
 * Firestore representation of users/{uid}/callHistory/{callId}.
 * All properties have defaults so Firestore can deserialize the class.
 */
data class CallHistoryDocument(

    var callType: String = CallType.AUDIO.value,

    var direction: String? = null,

    var status: String = CallStatus.UNKNOWN.value,

    var participants: List<String> = emptyList(),

    var conversationId: String? = null,

    var startedAt: Date? = null,

    var endedAt: Date? = null,

    var createdAt: Date? = null

) {

    /**
     * Returns null for documents that cannot be shown correctly:
     * no start time (cannot be sorted) or no valid direction.
     * The Firestore document ID is the source of truth for the ID.
     */
    fun toDomainOrNull(documentId: String): CallHistory? {

        val started = startedAt?.time ?: return null
        val callDirection = CallDirection.fromValue(direction) ?: return null

        val validParticipants = participants
            .filter { it.isNotBlank() }
            .distinct()

        if (validParticipants.isEmpty()) return null

        return CallHistory(
            id = documentId,
            callType = CallType.fromValue(callType),
            direction = callDirection,
            status = CallStatus.fromValue(status),
            participantIds = validParticipants,
            conversationId = conversationId?.takeIf { it.isNotBlank() },
            startedAt = started,
            endedAt = endedAt?.time,
            createdAt = createdAt?.time
        )
    }

    companion object {
        const val USERS_COLLECTION = "users"
        const val COLLECTION = "callHistory"
        const val FIELD_STARTED_AT = "startedAt"
    }
}