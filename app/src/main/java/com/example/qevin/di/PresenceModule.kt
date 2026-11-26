package com.example.qevin.di

import com.example.qevin.data.repository.RealtimePresenceRepository
import com.example.qevin.domain.repository.PresenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PresenceModule {

    @Binds
    @Singleton
    abstract fun bindPresenceRepository(
        impl: RealtimePresenceRepository
    ): PresenceRepository
}