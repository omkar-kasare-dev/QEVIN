package com.example.qevin.data.repository

import com.example.qevin.data.remote.FirestoreMessageDataSource
import com.example.qevin.domain.model.Message
import com.example.qevin.domain.repository.MessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreMessageDataSource
) : MessageRepository {

    override suspend fun sendTextMessage(
        conversationId: String,
        senderUid: String,
        text: String
    ): Result<Unit> = safeResult {
        dataSource.sendTextMessage(
            conversationId = conversationId,
            senderUid = senderUid,
            text = text
        )
    }

    override suspend fun markConversationRead(
        conversationId: String,
        uid: String
    ): Result<Unit> = safeResult {
        dataSource.markConversationRead(
            conversationId = conversationId,
            uid = uid
        )
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        dataSource
            .observeMessages(conversationId)
            .distinctUntilChanged()

    private suspend fun <T> safeResult(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}