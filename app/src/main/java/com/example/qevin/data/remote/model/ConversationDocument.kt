package com.example.qevin.data.remote.model

import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.LastMessage
import com.example.qevin.domain.model.MessageType
import java.util.Date

/**
 * Firestore representation of conversations/{conversationId}.
 * All properties have defaults so Firestore can deserialize the class.
 */
data class ConversationDocument(

    var conversationId: String = "",

    var type: String = ConversationType.DIRECT.value,

    var participants: List<String> = emptyList(),

    var name: String? = null,

    var photoUrl: String? = null,

    var lastMessage: LastMessageDocument? = null,

    var unreadCounts: Map<String, Long> = emptyMap(),

    var createdAt: Date? = null,

    var updatedAt: Date? = null

) {

    /**
     * Returns null for malformed documents (no participants) so the
     * repository can skip them instead of crashing the whole list.
     * The Firestore document ID is the source of truth for the ID.
     */
    fun toDomainOrNull(documentId: String): Conversation? {

        val validParticipants = participants
            .filter { it.isNotBlank() }
            .distinct()

        if (validParticipants.isEmpty()) return null

        return Conversation(
            id = documentId,
            type = ConversationType.fromValue(type),
            participantIds = validParticipants,
            name = name?.takeIf { it.isNotBlank() },
            photoUrl = photoUrl?.takeIf { it.isNotBlank() },
            lastMessage = lastMessage?.toDomain(),
            unreadCounts = unreadCounts.mapValues {
                it.value.toInt().coerceAtLeast(0)
            },
            createdAt = createdAt?.time,
            updatedAt = updatedAt?.time
        )
    }

    companion object {
        const val COLLECTION = "conversations"
        const val FIELD_PARTICIPANTS = "participants"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}

data class LastMessageDocument(

    var messageId: String = "",

    var senderId: String = "",

    var text: String = "",

    var type: String = MessageType.TEXT.value,

    var sentAt: Date? = null,

    var deleted: Boolean = false

) {

    fun toDomain(): LastMessage? {
        // A last message with no sender is unusable for previews.
        if (senderId.isBlank()) return null

        return LastMessage(
            messageId = messageId,
            senderId = senderId,
            text = text,
            type = MessageType.fromValue(type),
            sentAt = sentAt?.time,
            isDeleted = deleted
        )
    }
}