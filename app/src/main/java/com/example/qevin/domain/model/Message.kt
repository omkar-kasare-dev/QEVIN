package com.example.qevin.domain.model

data class Message(
    val id: String,
    val senderId: String,
    val text: String,
    val type: MessageType,
    // Null only briefly, while the server timestamp is still pending.
    val sentAt: Long?
)