package com.example.qevin.data.repository

import com.example.qevin.data.remote.FirestoreUserSearchDataSource
import com.example.qevin.domain.model.ParticipantProfile
import com.example.qevin.domain.repository.UserSearchRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSearchRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUserSearchDataSource
) : UserSearchRepository {

    override suspend fun searchUsers(
        query: String,
        currentUid: String
    ): Result<List<ParticipantProfile>> =
        try {
            Result.success(dataSource.searchUsers(query, currentUid))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
}