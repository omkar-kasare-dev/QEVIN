package com.example.qevin.domain.repository

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
}