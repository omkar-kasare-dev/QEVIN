package com.example.qevin.data.remote

import com.example.qevin.data.remote.model.UserProfileDocument
import com.example.qevin.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProfileDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection =
        firestore.collection("users")

    private val usernamesCollection =
        firestore.collection("usernames")

    // Create a new profile without overwriting an existing one.
    suspend fun createProfile(profile: UserProfile) {

        require(profile.uid.isNotBlank()) {
            "User ID cannot be empty."
        }

        val normalizedUsername = normalizeUsername(profile.username)

        val userReference = usersCollection.document(profile.uid)

        firestore.runTransaction { transaction ->

            val existingProfile = transaction.get(userReference)

            check(!existingProfile.exists()) {
                "A profile already exists for this account."
            }

            val usernameReference = normalizedUsername?.let {
                usernamesCollection.document(it)
            }

            if (usernameReference != null) {
                val existingUsername =
                    transaction.get(usernameReference)

                check(!existingUsername.exists()) {
                    "This username is already taken."
                }
            }

            val now = Date()

            val document = UserProfileDocument.fromDomain(
                profile.copy(
                    createdAt = now.time,
                    updatedAt = now.time
                )
            )

            transaction.set(userReference, document)

            if (usernameReference != null) {
                transaction.set(
                    usernameReference,
                    mapOf(
                        "uid" to profile.uid,
                        "username" to normalizedUsername
                    )
                )
            }

            Unit
        }.await()
    }

    // Retrieve a profile using Firebase UID.
    suspend fun getProfile(uid: String): UserProfile? {

        val snapshot = usersCollection
            .document(uid)
            .get()
            .await()

        return snapshot
            .toObject(UserProfileDocument::class.java)
            ?.toDomain()
    }

    // Observe real-time profile changes.
    fun observeProfile(uid: String): Flow<UserProfile?> =
        callbackFlow {

            val listener = usersCollection
                .document(uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    val profile = snapshot
                        .toObject(UserProfileDocument::class.java)
                        ?.toDomain()

                    trySend(profile)
                }

            awaitClose {
                listener.remove()
            }
        }

    // Update profile and maintain username reservation.
    suspend fun updateProfile(profile: UserProfile) {

        require(profile.uid.isNotBlank()) {
            "User ID cannot be empty."
        }

        val userReference = usersCollection.document(profile.uid)

        val newUsername = normalizeUsername(profile.username)

        firestore.runTransaction { transaction ->

            val userSnapshot = transaction.get(userReference)

            check(userSnapshot.exists()) {
                "User profile does not exist."
            }

            val existingProfile = userSnapshot
                .toObject(UserProfileDocument::class.java)
                ?: throw IllegalStateException(
                    "Unable to read existing user profile."
                )

            val oldUsername = normalizeUsername(
                existingProfile.username
            )

            val newUsernameReference = newUsername
                ?.takeIf { it != oldUsername }
                ?.let { usernamesCollection.document(it) }

            val oldUsernameReference = oldUsername
                ?.takeIf { it != newUsername }
                ?.let { usernamesCollection.document(it) }

            // Complete all transaction reads before writes.
            val newUsernameSnapshot = newUsernameReference?.let {
                transaction.get(it)
            }

            val oldUsernameSnapshot = oldUsernameReference?.let {
                transaction.get(it)
            }

            if (newUsernameSnapshot?.exists() == true) {

                val reservedUid =
                    newUsernameSnapshot.getString("uid")

                check(reservedUid == profile.uid) {
                    "This username is already taken."
                }
            }

            val now = Date()

            val updatedDocument = UserProfileDocument.fromDomain(
                profile.copy(
                    createdAt = existingProfile.createdAt?.time
                        ?: now.time,
                    updatedAt = now.time
                )
            )

            transaction.set(userReference, updatedDocument)

            // Release the previous username if owned by this user.
            if (
                oldUsernameReference != null &&
                oldUsernameSnapshot?.getString("uid") == profile.uid
            ) {
                transaction.delete(oldUsernameReference)
            }

            // Reserve the new username.
            if (newUsernameReference != null) {
                transaction.set(
                    newUsernameReference,
                    mapOf(
                        "uid" to profile.uid,
                        "username" to newUsername
                    )
                )
            }

            Unit
        }.await()
    }

    // Check username availability.
    suspend fun isUsernameAvailable(
        username: String,
        currentUid: String
    ): Boolean {

        val normalizedUsername = normalizeUsername(username)
            ?: return false

        val snapshot = usernamesCollection
            .document(normalizedUsername)
            .get()
            .await()

        if (!snapshot.exists()) {
            return true
        }

        return snapshot.getString("uid") == currentUid
    }

    private fun normalizeUsername(
        username: String?
    ): String? {

        return username
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
    }
}
