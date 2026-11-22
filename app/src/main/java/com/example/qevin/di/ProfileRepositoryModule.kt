package com.example.qevin.di

import com.example.qevin.data.repository.ProfileRepositoryImpl
import com.example.qevin.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: ProfileRepositoryImpl
    ): ProfileRepository
}
