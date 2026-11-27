package com.example.qevin.data.repository

import com.example.qevin.data.remote.FirestoreCallDataSource
import com.example.qevin.domain.model.CallHistorySnapshot
import com.example.qevin.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreCallDataSource
) : CallRepository {

    override fun observeCallHistory(uid: String): Flow<CallHistorySnapshot> =
        dataSource
            .observeCallHistory(uid)
            // Metadata-only events shouldn't recompose the list.
            .distinctUntilChanged()
}