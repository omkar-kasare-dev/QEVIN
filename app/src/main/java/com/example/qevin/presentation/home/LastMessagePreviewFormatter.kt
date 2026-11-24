package com.example.qevin.presentation.home

import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.MessageType

object LastMessagePreviewFormatter {

    private const val NO_MESSAGES = "No messages yet"
    private const val UNAVAILABLE = "Message unavailable"

    /**
     * Builds the single-line preview shown under a conversation name.
     *
     * @param senderName display name of the last sender, used only for
     * other people's messages in group conversations.
     */
    fun format(
        conversation: Conversation,
        currentUid: String,
        senderName: String? = null
    ): String {
        val last = conversation.lastMessage ?: return NO_MESSAGES

        val isMine = last.senderId == currentUid

        if (last.isDeleted) {
            return if (isMine) "You deleted this message" else "This message was deleted"
        }

        val label: String? = when {
            isMine -> "You"
            conversation.type == ConversationType.GROUP ->
                senderName?.trim()?.substringBefore(' ')?.takeIf { it.isNotBlank() }
            else -> null
        }

        return when (last.type) {
            MessageType.TEXT -> {
                val body = last.text.normalizeWhitespace()
                    .ifEmpty { return UNAVAILABLE }
                if (label != null) "$label: $body" else body
            }
            MessageType.IMAGE -> media(label, "sent a photo", "Photo")
            MessageType.VOICE -> media(label, "sent a voice message", "Voice message")
            MessageType.UNKNOWN -> media(label, "sent a message", "Message")
        }
    }

    private fun media(label: String?, withLabel: String, withoutLabel: String): String =
        if (label != null) "$label $withLabel" else withoutLabel

    private fun String.normalizeWhitespace(): String =
        trim().replace(Regex("\\s+"), " ")
}