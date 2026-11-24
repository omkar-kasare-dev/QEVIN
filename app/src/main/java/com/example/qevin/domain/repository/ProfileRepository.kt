package com.example.qevin.domain.repository

import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    suspend fun createProfile(
        profile: UserProfile
    ): Result<Unit>

    suspend fun getProfile(
        uid: String
    ): Result<UserProfile?>

    fun observeProfile(
        uid: String
    ): Flow<UserProfile?>

    suspend fun updateProfile(
        profile: UserProfile
    ): Result<Unit>

    suspend fun isUsernameAvailable(
        username: String,
        currentUid: String
    ): Result<Boolean>

    //----------
    suspend fun getParticipantProfiles(uids: Collection<String>): Result<List<ParticipantProfile>>
}
