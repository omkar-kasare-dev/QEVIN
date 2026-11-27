package com.example.qevin.domain.repository

import com.example.qevin.domain.model.ParticipantProfile

interface UserSearchRepository {

    /**
     * Finds users by username prefix, display-name prefix, or exact phone
     * number. The signed-in user is never included. Fails only when every
     * underlying query fails.
     */
    suspend fun searchUsers(
        query: String,
        currentUid: String
    ): Result<List<ParticipantProfile>>
}