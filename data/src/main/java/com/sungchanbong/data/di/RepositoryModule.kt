package com.sungchanbong.data.di

import com.sungchanbong.data.repositories.PhotoRepositoryImpl
import com.sungchanbong.data.util.Clock
import com.sungchanbong.data.util.SystemClock
import com.sungchanbong.domain.repositories.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClock): Clock
}
