package com.sungchanbong.domain.usecase

import com.sungchanbong.domain.image.ImagePrefetcher
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.repositories.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PhotoLikeUseCase @Inject constructor(
    private val repository: PhotoRepository,
    private val imagePrefetcher: ImagePrefetcher
) {
    suspend fun onToggle(photo: Photo): Result<Unit> = repository.togglePhotoLike(photo).onSuccess {
        if (!photo.isLike) {
            imagePrefetcher.prefetch(photo.offlineImageUrls())
        }
    }

    fun getLikedPhoto(): Flow<List<Photo>> = repository.getLikedPhoto()

    fun observeIsPhotoLike(photoId: String): Flow<Boolean> =
        repository.observeIsPhotoLike(photoId = photoId)

    fun prefetchLikedPhoto(photos: List<Photo>) =
        imagePrefetcher.prefetch(photos.flatMap { it.offlineImageUrls() })
}

private fun Photo.offlineImageUrls(): List<String> =
    listOfNotNull(fullUrl, thumbUrl, authorProfileImageUrl).filter { it.isNotBlank() }
