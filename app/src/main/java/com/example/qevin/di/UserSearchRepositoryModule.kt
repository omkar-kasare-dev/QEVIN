package com.example.qevin.di


import com.example.qevin.data.repository.UserSearchRepositoryImpl
import com.example.qevin.domain.repository.UserSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSearchRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserSearchRepository(
        implementation: UserSearchRepositoryImpl
    ): UserSearchRepository
}