package com.sungchanbong.unsplashcard

import androidx.paging.PagingData
import com.sungchanbong.data.di.RepositoryModule
import com.sungchanbong.data.util.Clock
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.repositories.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePhotoRepository @Inject constructor() : PhotoRepository {

    private val likePhotoList = MutableStateFlow<Map<String, Photo>>(emptyMap())

    private val feed = List(6) { index ->
        Photo(
            id = "p$index",
            description = "사진 $index",
            thumbUrl = "",
            fullUrl = "",
            width = 400,
            height = 600,
            authorName = "작가$index",
            authorUsername = "author$index",
            authorProfileImageUrl = null,
            likes = index,
            isLike = false,
        )
    }

    override fun getPhotos(): Flow<PagingData<Photo>> =
        likePhotoList.map { favs ->
            PagingData.from(feed.map { it.copy(isLike = it.id in favs) })
        }

    override suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail> {
        val photo = feed.first { it.id == photoId }
        return Result.success(
            PhotoDetail(
                photo = photo.copy(isLike = photoId in likePhotoList.value),
                views = 100,
                downloads = 10,
                location = null,
                exifModel = null,
                tags = emptyList(),
                isStale = false,
            ),
        )
    }

    override fun getLikedPhoto(): Flow<List<Photo>> =
        likePhotoList.map { it.values.toList() }

    override fun observeIsPhotoLike(photoId: String): Flow<Boolean> =
        likePhotoList.map { photoId in it }

    override suspend fun togglePhotoLike(photo: Photo): Result<Unit> {
        likePhotoList.value = likePhotoList.value.toMutableMap().apply {
            if (photo.id in this) remove(photo.id) else put(photo.id, photo.copy(isLike = true))
        }
        return Result.success(Unit)
    }

    override suspend fun photoDownload(id: String): Result<String> =
        Result.success("https://example.com/$id.jpg")
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
abstract class FakeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: FakePhotoRepository): PhotoRepository

    @Binds
    abstract fun bindClock(impl: FakeClock): Clock
}

@Singleton
class FakeClock @Inject constructor() : Clock {
    override fun now(): Long = 1_000L
}
