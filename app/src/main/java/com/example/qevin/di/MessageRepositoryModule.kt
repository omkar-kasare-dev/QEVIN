package com.example.qevin.di

import com.example.qevin.data.repository.MessageRepositoryImpl
import com.example.qevin.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        implementation: MessageRepositoryImpl
    ): MessageRepository
}