package com.example.qevin.domain.repository

import kotlinx.coroutines.flow.Flow

interface PresenceRepository {

    /**
     * Keeps the signed-in user marked online until this coroutine is cancelled.
     * On cancellation it writes "offline" and closes the database connection.
     */
    suspend fun runOwnPresence(uid: String)

    /**
     * Best-effort "offline" write for the current user. Call before signing out,
     * while the session is still valid. Never throws.
     */
    suspend fun markOffline()

    /**
     * Emits a map of uid -> isOnline. Emits an empty map immediately so callers
     * never wait on the network. A uid missing from the map means offline.
     */
    fun observeOnline(uids: Set<String>): Flow<Map<String, Boolean>>
}