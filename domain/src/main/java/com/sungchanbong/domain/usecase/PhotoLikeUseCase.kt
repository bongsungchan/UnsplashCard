package com.sungchanbong.domain.usecase

import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.repositories.PhotoRepository
import javax.inject.Inject

class PhotoLikeUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend fun onToggle(photo: Photo): Result<Unit> {
        return repository.togglePhotoLike(photo)
    }
}