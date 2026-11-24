package com.example.qevin.di

import com.example.qevin.data.repository.ConversationRepositoryImpl
import com.example.qevin.domain.repository.ConversationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConversationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        implementation: ConversationRepositoryImpl
    ): ConversationRepository
}