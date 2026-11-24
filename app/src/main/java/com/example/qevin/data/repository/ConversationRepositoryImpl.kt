package com.example.qevin.data.repository


import com.example.qevin.data.remote.FirestoreConversationDataSource
import com.example.qevin.domain.model.ConversationsSnapshot
import com.example.qevin.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreConversationDataSource
) : ConversationRepository {

    override fun observeConversations(uid: String): Flow<ConversationsSnapshot> =
        dataSource
            .observeConversations(uid)
            // Metadata-only events (e.g. pending-write flags) shouldn't recompose the list.
            .distinctUntilChanged()
}