package com.sungchanbong.data.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.sungchanbong.data.local.LikePhotoDao
import com.sungchanbong.data.local.PhotoDao
import com.sungchanbong.data.mapper.toDomain
import com.sungchanbong.data.mapper.toFavoriteEntity
import com.sungchanbong.data.mapper.toPhotoError
import com.sungchanbong.data.mapper.toStaleDetail
import com.sungchanbong.data.paging.PhotoMediator
import com.sungchanbong.data.remote.UnsplashAPI
import com.sungchanbong.data.util.Clock
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.repositories.PhotoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val api: UnsplashAPI,
    private val photoDao: PhotoDao,
    private val likePhotoDao: LikePhotoDao,
    private val photoMediator: PhotoMediator,
    private val clock: Clock,
) : PhotoRepository {

    override fun getPhotos(): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE / 2,
                enablePlaceholders = true
            ),
            remoteMediator = photoMediator,
            pagingSourceFactory = {
                photoDao.pagingSource()
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail> = try {
        val isFavorite = likePhotoDao.exists(photoId)
        Result.success(api.getPhotoDetail(photoId).toDomain(isFavorite))
    } catch (e: CancellationException) {
        throw e
    } catch (e: JsonEncodingException) {
        fallbackToCache(photoId) ?: Result.failure(PhotoError.Unexpected(e))
    } catch (e: JsonDataException) {
        fallbackToCache(photoId) ?: Result.failure(PhotoError.Unexpected(e))
    } catch (e: PhotoError) {
        fallbackToCache(photoId) ?: Result.failure(e)
    } catch (e: IOException) {
        fallbackToCache(photoId) ?: Result.failure(e.toPhotoError())
    } catch (e: HttpException) {
        fallbackToCache(photoId) ?: Result.failure(e.toPhotoError())
    }

    override suspend fun togglePhotoLike(photo: Photo): Result<Unit> = runCatchingDomain {
        likePhotoDao.toggle(photo.toFavoriteEntity(savedAt = clock.now()))
    }

    override fun getLikedPhoto(): Flow<List<Photo>> {
        return likePhotoDao.observeFavorites().map { entities ->
            entities.map {
                it.toDomain()
            }
        }
    }

    override fun observeIsPhotoLike(photoId: String): Flow<Boolean> {
        return likePhotoDao.observeExists(id = photoId).distinctUntilChanged()
    }

    private suspend fun fallbackToCache(id: String): Result<PhotoDetail>? {
        val cached = likePhotoDao.findById(id)?.toDomain()
            ?: photoDao.findById(id)?.toDomain(isLike = false)
            ?: return null
        return Result.success(cached.toStaleDetail())
    }

    override suspend fun photoDownload(id: String): Result<String> = runCatchingDomain {
        api.downloadPhoto(id).url ?: throw PhotoError.Unexpected(
            IllegalStateException("download url missing in 200 response"),
        )
    }

    private inline fun <T> runCatchingDomain(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: JsonEncodingException) {
            Result.failure(PhotoError.Unexpected(e))
        } catch (e: JsonDataException) {
            Result.failure(PhotoError.Unexpected(e))
        } catch (e: PhotoError) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e.toPhotoError())
        } catch (e: HttpException) {
            Result.failure(e.toPhotoError())
        }

    private companion object {
        const val PAGE_SIZE = 20
    }

}