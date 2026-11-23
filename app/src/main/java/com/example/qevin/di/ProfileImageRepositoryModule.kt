package com.example.qevin.di

import com.example.qevin.data.repository.InternalProfileImageRepository
import com.example.qevin.domain.repository.ProfileImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileImageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileImageRepository(
        implementation: InternalProfileImageRepository
    ): ProfileImageRepository
}
