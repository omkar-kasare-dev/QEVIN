package com.example.qevin.data.remote

import com.example.qevin.domain.model.ParticipantProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSearchDataSource @Inject constructor(
    firestore: FirebaseFirestore
) {

    private val usersCollection = firestore.collection("users")
    private val usernamesCollection = firestore.collection("usernames")

    suspend fun searchUsers(
        query: String,
        currentUid: String
    ): List<ParticipantProfile> = coroutineScope {

        val term = query.trim().removePrefix("@").trim()
        if (term.length < MIN_TERM_LENGTH) return@coroutineScope emptyList()

        // Priority order: username matches, then name matches, then phone.
        val branches = buildList {
            add(async { attempt { byUsername(term.lowercase(Locale.ROOT)) } })
            nameVariants(term).forEach { variant ->
                add(async { attempt { byDisplayName(variant) } })
            }
            phoneVariants(term).forEach { number ->
                add(async { attempt { byPhone(number) } })
            }
        }.awaitAll()

        val failures = branches.mapNotNull { it.exceptionOrNull() }
        if (failures.size == branches.size) throw failures.first()

        branches
            .flatMap { it.getOrNull().orEmpty() }
            .filter { it.uid != currentUid }
            .distinctBy { it.uid }
            .take(MAX_RESULTS)
    }

    // --------------------------------------------------
    // QUERIES
    // --------------------------------------------------

    // Usernames are reserved under their lowercase form as the document ID,
    // so a document-ID range is a case-correct prefix search.
    private suspend fun byUsername(prefix: String): List<ParticipantProfile> {
        val snapshot = usernamesCollection
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), prefix)
            .whereLessThanOrEqualTo(FieldPath.documentId(), prefix + HIGH_SURROGATE)
            .limit(PER_QUERY_LIMIT)
            .get()
            .await()

        val uids = snapshot.documents.mapNotNull { doc ->
            doc.getString("uid")?.takeIf { it.isNotBlank() }
        }

        return profilesFor(uids)
    }

    private suspend fun byDisplayName(prefix: String): List<ParticipantProfile> =
        usersCollection
            .orderBy("displayName")
            .startAt(prefix)
            .endAt(prefix + HIGH_SURROGATE)
            .limit(PER_QUERY_LIMIT)
            .get()
            .await()
            .documents
            .mapNotNull(::toProfile)

    private suspend fun byPhone(number: String): List<ParticipantProfile> =
        usersCollection
            .whereEqualTo("phoneNumber", number)
            .limit(PHONE_LIMIT)
            .get()
            .await()
            .documents
            .mapNotNull(::toProfile)

    // whereIn accepts at most 30 values.
    private suspend fun profilesFor(uids: List<String>): List<ParticipantProfile> =
        uids.distinct().chunked(30).flatMap { chunk ->
            usersCollection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()
                .documents
                .mapNotNull(::toProfile)
        }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

    private fun toProfile(document: DocumentSnapshot): ParticipantProfile? {
        if (!document.exists()) return null

        return ParticipantProfile(
            uid = document.id,
            displayName = document.getString("displayName").orEmpty(),
            username = document.getString("username")?.takeIf { it.isNotBlank() },
            photoUrl = document.getString("photoUrl")?.takeIf {
                it.startsWith("http://") || it.startsWith("https://")
            }
        )
    }

    private fun nameVariants(term: String): Set<String> = linkedSetOf(
        term,
        term.replaceFirstChar { it.uppercase() },
        term.split(' ').joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    )

    // Stored numbers are international ("+" followed by digits).
    private fun phoneVariants(term: String): Set<String> {
        if (!term.matches(PHONE_INPUT)) return emptySet()

        val digits = term.filter { it.isDigit() }
        if (digits.length < MIN_PHONE_DIGITS) return emptySet()

        return setOf("+$digits")
    }

    private suspend fun <T> attempt(block: suspend () -> List<T>): Result<List<T>> =
        try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private companion object {
        const val MIN_TERM_LENGTH = 2
        const val MIN_PHONE_DIGITS = 7
        const val PER_QUERY_LIMIT = 20L
        const val PHONE_LIMIT = 5L
        const val MAX_RESULTS = 30
        const val HIGH_SURROGATE = "\uf8ff"
        val PHONE_INPUT = Regex("^[+\\d\\s()-]+$")
    }
}