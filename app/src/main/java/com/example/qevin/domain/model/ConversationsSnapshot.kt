package com.example.qevin.domain.model


/**
 * One emission of the signed-in user's conversation list.
 * [isFromCache] is true while data comes from the local cache
 * (offline or not yet confirmed by the server).
 */
data class ConversationsSnapshot(
    val currentUid: String,
    val conversations: List<Conversation>,
    val isFromCache: Boolean
)