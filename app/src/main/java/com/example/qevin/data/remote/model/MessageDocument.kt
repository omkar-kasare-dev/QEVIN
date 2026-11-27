package com.example.qevin.data.remote.model

import com.example.qevin.domain.model.Message
import com.example.qevin.domain.model.MessageType
import java.util.Date

/**
 * Firestore representation of conversations/{id}/messages/{messageId}.
 * All properties have defaults so Firestore can deserialize the class.
 */
data class MessageDocument(

    var messageId: String = "",

    var senderId: String = "",

    var text: String = "",

    var type: String = MessageType.TEXT.value,

    var sentAt: Date? = null,

    var status: String = ""

) {

    /** Null for messages with no sender, which cannot be shown correctly. */
    fun toDomainOrNull(documentId: String): Message? {
        if (senderId.isBlank()) return null

        return Message(
            id = documentId,
            senderId = senderId,
            text = text,
            type = MessageType.fromValue(type),
            sentAt = sentAt?.time
        )
    }

    companion object {
        const val COLLECTION = "messages"
        const val FIELD_SENT_AT = "sentAt"
    }
}