package com.sungchanbong.data.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.sungchanbong.data.local.LikePhotoDao
import com.sungchanbong.data.local.PhotoDao
import com.sungchanbong.data.mapper.toDomain
import com.sungchanbong.data.mapper.toFavoriteEntity
import com.sungchanbong.data.paging.PhotoMediator
import com.sungchanbong.data.remote.UnsplashAPI
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.repositories.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val api: UnsplashAPI,
    private val photoDao: PhotoDao,
    private val likePhotoDao: LikePhotoDao,
    private val photoMediator: PhotoMediator
) : PhotoRepository {

    override fun getPhotos(): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 10,
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

    override suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail> {
        return try {
            Result.success(api.getPhotoDetail(id = photoId).toDomain(false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePhotoLike(photo: Photo): Result<Unit> {
        return try {
            likePhotoDao.toggle(entity = photo.toFavoriteEntity(System.currentTimeMillis()))
            Result.success(Unit)
        } catch (
            e: Exception
        ) {
            Result.failure(e)
        }
    }

    override suspend fun getLikedPhoto(): Flow<List<Photo>> {
        return likePhotoDao.observeFavorites().map { entities ->
            entities.map {
                it.toDomain()
            }
        }
    }

    override fun observeIsPhotoLike(photoId: String): Flow<Boolean> {
        return likePhotoDao.observeExists(id = photoId).distinctUntilChanged()
    }


}