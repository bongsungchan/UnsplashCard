package com.sungchanbong.domain.usecase

import androidx.paging.PagingData
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.repositories.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    operator fun invoke(): Flow<PagingData<Photo>> = repository.getPhotos()
    suspend fun getDetail(photoId : String) = repository.getPhotoDetail(photoId = photoId)
}