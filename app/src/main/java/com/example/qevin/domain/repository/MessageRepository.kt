package com.example.qevin.domain.repository

import com.example.qevin.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    /**
     * Sends a text message and updates the conversation summary
     * (lastMessage, updatedAt, unreadCounts). Creates the direct
     * conversation if [conversationId] is a valid direct ID that
     * doesn't exist yet.
     */
    suspend fun sendTextMessage(
        conversationId: String,
        senderUid: String,
        text: String
    ): Result<Unit>

    /**
     * Sets the given user's unread count on the conversation to zero.
     * Works for direct and group conversations. Only call it for a
     * conversation known to exist.
     */
    suspend fun markConversationRead(
        conversationId: String,
        uid: String
    ): Result<Unit>

    /**
     * Observes the most recent messages, oldest first. Only call it for a
     * conversation known to exist: the security rules deny reads otherwise.
     * The flow throws on Firestore errors; collectors should use .catch.
     * The listener is removed when collection stops.
     */
    fun observeMessages(conversationId: String): Flow<List<Message>>
}