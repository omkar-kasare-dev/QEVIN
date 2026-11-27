package com.example.qevin.di

import com.example.qevin.data.repository.CallRepositoryImpl
import com.example.qevin.domain.repository.CallRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCallRepository(
        implementation: CallRepositoryImpl
    ): CallRepository
}