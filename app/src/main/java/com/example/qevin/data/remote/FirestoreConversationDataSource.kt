package com.example.qevin.data.remote

import android.util.Log
import com.example.qevin.data.remote.model.ConversationDocument
import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationsSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreConversationDataSource @Inject constructor(
    firestore: FirebaseFirestore
) {

    private val conversationsCollection =
        firestore.collection(ConversationDocument.COLLECTION)

    fun observeConversations(
        uid: String,
        limit: Long = DEFAULT_LIMIT
    ): Flow<ConversationsSnapshot> = callbackFlow {

        val listener = conversationsCollection
            .whereArrayContains(ConversationDocument.FIELD_PARTICIPANTS, uid)
            .orderBy(ConversationDocument.FIELD_UPDATED_AT, Query.Direction.DESCENDING)
            .limit(limit)
            // INCLUDE so we are also notified when isFromCache flips to false.
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val conversations = snapshot.documents.mapNotNull(::parse)

                trySend(
                    ConversationsSnapshot(
                        currentUid = uid,
                        conversations = conversations,
                        isFromCache = snapshot.metadata.isFromCache
                    )
                )
            }

        awaitClose { listener.remove() }
    }

    // Skips malformed documents instead of failing the whole list.
    private fun parse(document: DocumentSnapshot): Conversation? {
        return try {
            document
                .toObject(
                    ConversationDocument::class.java,
                    ServerTimestampBehavior.ESTIMATE
                )
                ?.toDomainOrNull(document.id)
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Skipping invalid conversation ${document.id}", exception)
            null
        }
    }

    private companion object {
        const val TAG = "QEVIN_CONVERSATIONS"
        const val DEFAULT_LIMIT = 50L
    }
}