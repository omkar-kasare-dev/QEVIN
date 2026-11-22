package com.example.qevin.data.remote.model


import com.example.qevin.domain.model.UserProfile
import java.util.Date
import java.util.Locale

/**
 * Firestore representation of a QEVIN user profile.
 *
 * All properties have default values so Firestore
 * can deserialize this class.
 */
data class UserProfileDocument(

    var uid: String = "",

    var displayName: String = "",

    var username: String? = null,

    var usernameLowercase: String? = null,

    var email: String? = null,

    var phoneNumber: String? = null,

    var photoUrl: String? = null,

    var about: String = "",

    var createdAt: Date? = null,

    var updatedAt: Date? = null

) {

    fun toDomain(): UserProfile {
        return UserProfile(
            uid = uid,
            displayName = displayName,
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            photoUrl = photoUrl,
            about = about,
            createdAt = createdAt?.time,
            updatedAt = updatedAt?.time
        )
    }

    companion object {

        fun fromDomain(profile: UserProfile): UserProfileDocument {

            val normalizedUsername = profile.username
                ?.trim()
                ?.lowercase(Locale.ROOT)
                ?.takeIf { it.isNotBlank() }

            return UserProfileDocument(
                uid = profile.uid,
                displayName = profile.displayName,
                username = normalizedUsername?.let {
                    profile.username?.trim()
                },
                usernameLowercase = normalizedUsername,
                email = profile.email,
                phoneNumber = profile.phoneNumber,
                photoUrl = profile.photoUrl,
                about = profile.about,
                createdAt = profile.createdAt?.let(::Date),
                updatedAt = profile.updatedAt?.let(::Date)
            )
        }
    }
}
