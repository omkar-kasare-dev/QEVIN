package com.example.qevin.data.remote

import android.util.Log
import com.example.qevin.data.remote.model.ConversationDocument
import com.example.qevin.data.remote.model.MessageDocument
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.Message
import com.example.qevin.domain.model.MessageType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /**
     * Sets unreadCounts.<uid> to 0 using a field-path update, so other
     * participants' counts are never touched. A missing document is ignored.
     */
    suspend fun markConversationRead(
        conversationId: String,
        uid: String
    ) {
        require(conversationId.isNotBlank()) { "Conversation ID cannot be empty." }
        require(uid.isNotBlank()) { "User ID cannot be empty." }

        try {
            conversationsCollection
                .document(conversationId)
                .update(FieldPath.of(FIELD_UNREAD_COUNTS, uid), 0L)
                .await()
        } catch (exception: FirebaseFirestoreException) {
            if (exception.code == FirebaseFirestoreException.Code.NOT_FOUND) return
            throw exception
        }
    }

    /**
     * The newest [limit] messages, returned oldest first. Your own pending
     * writes appear immediately (latency compensation), with an estimated
     * server timestamp.
     */
    fun observeMessages(
        conversationId: String,
        limit: Long = MESSAGE_LIMIT
    ): Flow<List<Message>> = callbackFlow {

        val listener = conversationsCollection
            .document(conversationId)
            .collection(MessageDocument.COLLECTION)
            .orderBy(MessageDocument.FIELD_SENT_AT, Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                // Newest-first from the query, so reverse for display.
                trySend(snapshot.documents.mapNotNull(::parseMessage).reversed())
            }

        awaitClose { listener.remove() }
    }

    // Skips malformed documents instead of failing the whole list.
    private fun parseMessage(document: DocumentSnapshot): Message? {
        return try {
            document
                .toObject(
                    MessageDocument::class.java,
                    ServerTimestampBehavior.ESTIMATE
                )
                ?.toDomainOrNull(document.id)
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Skipping invalid message ${document.id}", exception)
            null
        }
    }

    private companion object {
        const val TAG = "QEVIN_MESSAGES"
        const val MESSAGES = "messages"
        const val STATUS_SENT = "sent"
        const val FIELD_UNREAD_COUNTS = "unreadCounts"
        const val MAX_TEXT_LENGTH = 4000
        const val PREVIEW_LENGTH = 200
        const val MESSAGE_LIMIT = 100L
    }
}