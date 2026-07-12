package com.sungchanbong.data.di

import android.content.Context
import androidx.room.Room
import com.sungchanbong.data.local.AppDatabase
import com.sungchanbong.data.local.LikePhotoDao
import com.sungchanbong.data.local.PhotoDao
import com.sungchanbong.data.local.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Suppress("SpreadOperator")
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .build()

    @Provides
    fun providePhotoDao(db: AppDatabase): PhotoDao = db.photoDao()

    @Provides
    fun provideLikePhotoDao(db: AppDatabase): LikePhotoDao = db.likePhotoDao()

    @Provides
    fun provideRemoteKeyDao(db: AppDatabase): RemoteKeyDao = db.remoteKeyDao()
}