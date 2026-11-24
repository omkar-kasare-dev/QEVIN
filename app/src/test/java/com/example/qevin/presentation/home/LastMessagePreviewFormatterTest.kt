package com.example.qevin.presentation.home

import com.example.qevin.domain.model.Conversation
import com.example.qevin.domain.model.ConversationType
import com.example.qevin.domain.model.LastMessage
import com.example.qevin.domain.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test

class LastMessagePreviewFormatterTest {

    private val me = "me"
    private val other = "other"

    private fun conversation(
        last: LastMessage?,
        type: ConversationType = ConversationType.DIRECT
    ) = Conversation(
        id = "c1",
        type = type,
        participantIds = listOf(me, other),
        lastMessage = last
    )

    private fun message(
        sender: String,
        text: String = "hi",
        type: MessageType = MessageType.TEXT,
        deleted: Boolean = false
    ) = LastMessage("m1", sender, text, type, 1L, deleted)

    private fun preview(
        c: Conversation,
        name: String? = null
    ) = LastMessagePreviewFormatter.format(c, me, name)

    @Test fun noMessages() =
        assertEquals("No messages yet", preview(conversation(null)))

    @Test fun myText() =
        assertEquals("You: Hello", preview(conversation(message(me, "Hello"))))

    @Test fun otherTextInDirectHasNoPrefix() =
        assertEquals("Call you later", preview(conversation(message(other, "Call you later"))))

    @Test fun otherTextInGroupUsesFirstName() =
        assertEquals(
            "Rahul: On it",
            preview(conversation(message(other, "On it"), ConversationType.GROUP), "Rahul Sharma")
        )

    @Test fun otherTextInGroupWithoutNameHasNoPrefix() =
        assertEquals(
            "On it",
            preview(conversation(message(other, "On it"), ConversationType.GROUP))
        )

    @Test fun multilineIsFlattened() =
        assertEquals("You: a b c", preview(conversation(message(me, " a\n\nb   c "))))

    @Test fun myPhoto() =
        assertEquals("You sent a photo", preview(conversation(message(me, type = MessageType.IMAGE))))

    @Test fun otherPhotoInDirect() =
        assertEquals("Photo", preview(conversation(message(other, type = MessageType.IMAGE))))

    @Test fun otherVoiceInGroup() =
        assertEquals(
            "Rahul sent a voice message",
            preview(
                conversation(message(other, type = MessageType.VOICE), ConversationType.GROUP),
                "Rahul Sharma"
            )
        )

    @Test fun unknownType() =
        assertEquals("You sent a message", preview(conversation(message(me, type = MessageType.UNKNOWN))))

    @Test fun deletedMine() =
        assertEquals("You deleted this message", preview(conversation(message(me, deleted = true))))

    @Test fun deletedOther() =
        assertEquals("This message was deleted", preview(conversation(message(other, deleted = true))))

    @Test fun blankTextIsUnavailable() =
        assertEquals("Message unavailable", preview(conversation(message(other, text = "   "))))
}