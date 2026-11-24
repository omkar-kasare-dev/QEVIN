package com.example.qevin.domain.model

data class ParticipantProfile(
    val uid: String,
    val displayName: String,
    val username: String? = null,
    // Only remote (http/https) URLs. Local URIs are dropped.
    val photoUrl: String? = null
)