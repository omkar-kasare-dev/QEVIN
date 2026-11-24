package com.example.qevin.domain.model

/**
 * Deterministic IDs for one-to-one conversations: both UIDs, sorted, joined
 * by "_". The same pair always produces the same ID, so no query is needed
 * to find or create a chat and duplicates are impossible.
 *
 * Firebase Auth's generated UIDs are alphanumeric. If you ever use custom
 * UIDs containing "_", change SEPARATOR.
 */
object ConversationIds {

    private const val SEPARATOR = "_"

    fun direct(uidA: String, uidB: String): String {
        require(uidA.isNotBlank() && uidB.isNotBlank()) { "UIDs cannot be blank." }
        require(uidA != uidB) { "Cannot create a conversation with yourself." }
        return listOf(uidA, uidB).sorted().joinToString(SEPARATOR)
    }

    /**
     * Returns the other participant if [conversationId] is a valid direct
     * conversation ID containing [currentUid], otherwise null.
     */
    fun otherParticipant(conversationId: String, currentUid: String): String? {
        val parts = conversationId.split(SEPARATOR)
        if (parts.size != 2 || currentUid !in parts) return null

        val other = parts.first { it != currentUid }
        return other.takeIf { it.isNotBlank() && direct(currentUid, it) == conversationId }
    }
}