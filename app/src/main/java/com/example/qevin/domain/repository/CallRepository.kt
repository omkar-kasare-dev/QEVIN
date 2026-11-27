package com.example.qevin.domain.repository

import com.example.qevin.domain.model.CallHistorySnapshot
import kotlinx.coroutines.flow.Flow

interface CallRepository {

    /**
     * Observes the given user's call history, newest first.
     * The flow throws on Firestore errors; collectors should use .catch.
     * The listener is removed when collection stops.
     */
    fun observeCallHistory(uid: String): Flow<CallHistorySnapshot>
}