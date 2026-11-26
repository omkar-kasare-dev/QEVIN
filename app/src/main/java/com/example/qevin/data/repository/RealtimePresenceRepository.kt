package com.example.qevin.data.repository

import android.util.Log
import com.example.qevin.domain.repository.PresenceRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class RealtimePresenceRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : PresenceRepository {

    private fun presenceRef(uid: String) =
        database.getReference(PRESENCE_PATH).child(uid)

    private fun onlineValue() = mapOf(
        "online" to true,
        "lastChanged" to ServerValue.TIMESTAMP
    )

    private fun offlineValue() = mapOf(
        "online" to false,
        "lastChanged" to ServerValue.TIMESTAMP
    )

    // --------------------------------------------------
    // OWN PRESENCE
    // --------------------------------------------------

    override suspend fun runOwnPresence(uid: String) {
        val myRef = presenceRef(uid)
        database.goOnline()
        Log.d(TAG, "Presence run started for ${uid.take(6)}")

        try {
            connectionState()
                .catch { Log.w(TAG, "Connection listener failed", it) }
                .collect { connected ->
                    if (!connected) return@collect
                    try {
                        // Order matters: register the server-side "offline"
                        // BEFORE announcing "online".
                        myRef.onDisconnect().setValue(offlineValue()).awaitCompletion()
                        myRef.setValue(onlineValue()).awaitCompletion()
                        Log.d(TAG, "Marked online: ${uid.take(6)}")
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mark online", e)
                    }
                }
        } finally {
            // Runs on background, sign-out and user switch.
            withContext(NonCancellable) {
                writeOfflineQuietly(uid)
                database.goOffline() // frees the Spark connection slot
                Log.d(TAG, "Presence run ended for ${uid.take(6)}; connection closed")
            }
        }
    }

    override suspend fun markOffline() {
        val uid = auth.currentUser?.uid ?: return
        writeOfflineQuietly(uid)
    }

    private suspend fun writeOfflineQuietly(uid: String) {
        // Bounded: when the device is offline the write never completes, and
        // logout must not hang. The server-side onDisconnect is the fallback.
        withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            try {
                presenceRef(uid).setValue(offlineValue()).awaitCompletion()
                Log.d(TAG, "Marked offline: ${uid.take(6)}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to mark offline", e)
            }
        } ?: Log.w(TAG, "Offline write timed out; relying on onDisconnect")
    }

    private fun connectionState(): Flow<Boolean> = callbackFlow {
        val ref = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // --------------------------------------------------
    // OTHER USERS' PRESENCE
    // --------------------------------------------------

    override fun observeOnline(uids: Set<String>): Flow<Map<String, Boolean>> {
        if (uids.isEmpty()) return flowOf(emptyMap())

        return combine(uids.map(::observeSingle)) { pairs -> pairs.toMap() }
            // Never make callers wait for the network.
            .onStart { emit(emptyMap()) }
            .catch {
                Log.w(TAG, "Presence observation failed", it)
                emit(emptyMap())
            }
    }

    private fun observeSingle(uid: String): Flow<Pair<String, Boolean>> = callbackFlow {
        val ref = presenceRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.child("online").getValue(Boolean::class.java) == true
                trySend(uid to online)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Presence read cancelled for ${uid.take(6)}: ${error.message}")
                trySend(uid to false)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // --------------------------------------------------

    private suspend fun Task<Void>.awaitCompletion() =
        suspendCancellableCoroutine<Unit> { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        task.exception ?: Exception("Realtime Database write failed")
                    )
                }
            }
        }

    private companion object {
        const val TAG = "QEVIN_PRESENCE"
        const val PRESENCE_PATH = "presence"
        const val WRITE_TIMEOUT_MS = 2_000L
    }
}