package com.example.qevin.domain.model


data class PersonUi(
    val uid: String,
    // The direct conversation with this person.
    val conversationId: String,
    val name: String,
    val initials: String,
    val username: String?,
    val photoUrl: String?,
    val isOnline: Boolean
)