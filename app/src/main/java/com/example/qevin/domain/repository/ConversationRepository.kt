package com.example.qevin.domain.repository

import com.example.qevin.domain.model.ConversationsSnapshot
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    /**
     * Observes conversations the given user participates in.
     * The flow throws on Firestore errors; collectors should use .catch.
     * The listener is removed when collection stops.
     */
    fun observeConversations(uid: String): Flow<ConversationsSnapshot>
}