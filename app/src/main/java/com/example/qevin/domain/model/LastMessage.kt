package com.example.qevin.domain.model



enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    UNKNOWN;

    val value: String
        get() = name.lowercase()

    companion object {
        fun fromValue(value: String?): MessageType =
            entries.firstOrNull { it.value == value?.lowercase() } ?: UNKNOWN
    }
}

data class LastMessage(
    val messageId: String,
    val senderId: String,
    val text: String,
    val type: MessageType,
    val sentAt: Long?,
    val isDeleted: Boolean = false
)