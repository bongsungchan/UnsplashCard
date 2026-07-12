package com.sungchanbong.domain.repositories

import androidx.paging.PagingData
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getPhotos(): Flow<PagingData<Photo>>

    suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail>

    suspend fun togglePhotoLike(photo: Photo): Result<Unit>

    fun getLikedPhoto(): Flow<List<Photo>>
    fun observeIsPhotoLike(photoId: String): Flow<Boolean>
    suspend fun photoDownload(id: String): Result<String>
}