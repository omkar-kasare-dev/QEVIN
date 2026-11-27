package com.example.qevin.data.remote

import android.util.Log
import com.example.qevin.data.remote.model.CallHistoryDocument
import com.example.qevin.domain.model.CallHistory
import com.example.qevin.domain.model.CallHistorySnapshot
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
class FirestoreCallDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun observeCallHistory(
        uid: String,
        limit: Long = DEFAULT_LIMIT
    ): Flow<CallHistorySnapshot> = callbackFlow {

        val listener = firestore
            .collection(CallHistoryDocument.USERS_COLLECTION)
            .document(uid)
            .collection(CallHistoryDocument.COLLECTION)
            .orderBy(CallHistoryDocument.FIELD_STARTED_AT, Query.Direction.DESCENDING)
            .limit(limit)
            // INCLUDE so we are also notified when isFromCache flips to false.
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val calls = snapshot.documents.mapNotNull(::parse)

                trySend(
                    CallHistorySnapshot(
                        currentUid = uid,
                        calls = calls,
                        isFromCache = snapshot.metadata.isFromCache
                    )
                )
            }

        awaitClose { listener.remove() }
    }

    // Skips malformed documents instead of failing the whole list.
    private fun parse(document: DocumentSnapshot): CallHistory? {
        return try {
            val call = document
                .toObject(
                    CallHistoryDocument::class.java,
                    ServerTimestampBehavior.ESTIMATE
                )
                ?.toDomainOrNull(document.id)

            if (call == null) {
                Log.w(TAG, "Skipping invalid call record ${document.id}")
            }
            call
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Skipping unreadable call record ${document.id}", exception)
            null
        }
    }

    private companion object {
        const val TAG = "QEVIN_CALLS"
        const val DEFAULT_LIMIT = 50L
    }
}