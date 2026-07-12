package com.sungchanbong.domain.usecase

import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.repositories.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PhotoLikeUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend fun onToggle(photo: Photo): Result<Unit> {
        return repository.togglePhotoLike(photo)
    }

    suspend fun getLikedPhoto(): Flow<List<Photo>> = repository.getLikedPhoto()
}