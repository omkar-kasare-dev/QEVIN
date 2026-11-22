package com.example.qevin.domain.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val username: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val about: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

