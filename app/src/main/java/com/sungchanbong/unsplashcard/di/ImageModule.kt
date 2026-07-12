package com.sungchanbong.unsplashcard.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.sungchanbong.domain.download.PhotoFileDownloader
import com.sungchanbong.domain.image.ImagePrefetcher
import com.sungchanbong.unsplashcard.download.DownloadManagerFileDownloader
import com.sungchanbong.unsplashcard.image.CoilImagePrefetcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient.newBuilder().cache(null).build())
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(MEMORY_CACHE_PERCENT)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(IMAGE_CACHE_DIR))
                .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
        }
        .crossfade(true)
        .build()

    private const val MEMORY_CACHE_PERCENT = 0.25
    private const val IMAGE_CACHE_DIR = "image_cache"
    private const val IMAGE_DISK_CACHE_BYTES = 256L * 1024 * 1024
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageBindsModule {

    @Binds
    @Singleton
    abstract fun bindImagePrefetcher(impl: CoilImagePrefetcher): ImagePrefetcher

    @Binds
    @Singleton
    abstract fun bindPhotoFileDownloader(
        impl: DownloadManagerFileDownloader,
    ): PhotoFileDownloader
}
