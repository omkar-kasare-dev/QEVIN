package com.example.qevin.data.repository

import com.example.qevin.data.remote.FirestoreProfileDataSource
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.model.UserProfile
import com.example.qevin.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreProfileDataSource
) : ProfileRepository {

    override suspend fun createProfile(
        profile: UserProfile
    ): Result<Unit> = safeResult {
        dataSource.createProfile(profile)
    }

    override suspend fun getProfile(
        uid: String
    ): Result<UserProfile?> = safeResult {
        dataSource.getProfile(uid)
    }

    override fun observeProfile(
        uid: String
    ): Flow<UserProfile?> {
        return dataSource.observeProfile(uid)
    }

    override suspend fun updateProfile(
        profile: UserProfile
    ): Result<Unit> = safeResult {
        dataSource.updateProfile(profile)
    }

    override suspend fun isUsernameAvailable(
        username: String,
        currentUid: String
    ): Result<Boolean> = safeResult {
        dataSource.isUsernameAvailable(
            username = username,
            currentUid = currentUid
        )
    }

    private suspend fun <T> safeResult(
        block: suspend () -> T
    ): Result<T> {

        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    //-----------------
    override suspend fun getParticipantProfiles(
        uids: Collection<String>
    ): Result<List<ParticipantProfile>> = safeResult {
        dataSource.getParticipantProfiles(uids)
    }
}
