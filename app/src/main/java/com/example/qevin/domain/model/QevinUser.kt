
package com.example.qevin.domain.model

data class QevinUser(
    val id: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

