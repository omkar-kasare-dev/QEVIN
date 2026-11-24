package com.example.qevin.data.remote

import com.example.qevin.data.remote.model.ConversationDocument
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.MessageType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMessageDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val conversationsCollection =
        firestore.collection(ConversationDocument.COLLECTION)

    suspend fun sendTextMessage(
        conversationId: String,
        senderUid: String,
        text: String
    ) {
        val body = text.trim()

        require(conversationId.isNotBlank()) { "Conversation ID cannot be empty." }
        require(senderUid.isNotBlank()) { "Sender ID cannot be empty." }
        require(body.isNotEmpty()) { "Message cannot be empty." }
        require(body.length <= MAX_TEXT_LENGTH) { "Message is too long." }

        val conversationRef = conversationsCollection.document(conversationId)
        val messageRef = conversationRef.collection(MESSAGES).document()

        firestore.runTransaction { transaction ->

            // All reads before writes.
            val snapshot = transaction.get(conversationRef)
            val isNewConversation = !snapshot.exists()

            val participants: List<String> = if (isNewConversation) {
                val recipient = ConversationIds.otherParticipant(conversationId, senderUid)
                    ?: throw IllegalStateException("Conversation does not exist.")
                listOf(senderUid, recipient).sorted()
            } else {
                @Suppress("UNCHECKED_CAST")
                val existing = snapshot.get(ConversationDocument.FIELD_PARTICIPANTS)
                        as? List<String> ?: emptyList()

                check(senderUid in existing) {
                    "You are not a participant in this conversation."
                }
                existing
            }

            transaction.set(
                messageRef,
                mapOf(
                    "messageId" to messageRef.id,
                    "senderId" to senderUid,
                    "text" to body,
                    "type" to MessageType.TEXT.value,
                    "sentAt" to FieldValue.serverTimestamp(),
                    "status" to STATUS_SENT
                )
            )

            // Sender's unread resets to 0, everyone else's increments.
            val unreadCounts = buildMap<String, Any> {
                participants.forEach { uid ->
                    put(uid, if (uid == senderUid) 0L else FieldValue.increment(1))
                }
            }

            val summary = mutableMapOf<String, Any>(
                "lastMessage" to mapOf(
                    "messageId" to messageRef.id,
                    "senderId" to senderUid,
                    // Keep the conversation document small.
                    "text" to body.take(PREVIEW_LENGTH),
                    "type" to MessageType.TEXT.value,
                    "sentAt" to FieldValue.serverTimestamp(),
                    "deleted" to false
                ),
                "unreadCounts" to unreadCounts,
                ConversationDocument.FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            )

            if (isNewConversation) {
                summary["conversationId"] = conversationId
                summary["type"] = ConversationType.DIRECT.value
                summary[ConversationDocument.FIELD_PARTICIPANTS] = participants
                summary["createdAt"] = FieldValue.serverTimestamp()
            }

            // Merge keeps untouched fields and merges the nested unreadCounts map.
            transaction.set(conversationRef, summary, SetOptions.merge())

            Unit
        }.await()
    }

    private companion object {
        const val MESSAGES = "messages"
        const val STATUS_SENT = "sent"
        const val MAX_TEXT_LENGTH = 4000
        const val PREVIEW_LENGTH = 200
    }
}