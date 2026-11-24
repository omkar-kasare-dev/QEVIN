package com.example.qevin.domain.model

enum class ConversationType {
        DIRECT,
    GROUP;

    val value: String
        get() = name.lowercase()

    companion object {
        fun fromValue(value: String?): ConversationType =
            if (value.equals("group", ignoreCase = true)) GROUP else DIRECT
    }
}

data class Conversation(
    val id: String,
    val type: ConversationType,
    val participantIds: List<String>,
    // Used by groups only. Direct chats take name and photo from the other participant.
    val name: String? = null,
    val photoUrl: String? = null,
    val lastMessage: LastMessage? = null,
    val unreadCounts: Map<String, Int> = emptyMap(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {

    val lastActivityAt: Long
        get() = lastMessage?.sentAt ?: updatedAt ?: createdAt ?: 0L

    fun unreadCountFor(uid: String): Int = unreadCounts[uid] ?: 0

    fun otherParticipantIds(currentUid: String): List<String> =
        participantIds.filter { it != currentUid }
}