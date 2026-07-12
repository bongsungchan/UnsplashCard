package com.sungchanbong.domain.usecase

import com.sungchanbong.domain.download.PhotoFileDownloader
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.repositories.PhotoRepository
import javax.inject.Inject

class PhotoDownloadUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val fileDownloader: PhotoFileDownloader
) {
    suspend fun download(photo: Photo): Result<Unit> =
        photoRepository.photoDownload(photo.id).mapCatching { url ->
            fileDownloader.download(url, fileName(photo)).getOrThrow()
        }

    fun checkPermission(): Boolean = fileDownloader.isPermissionRequired()
    private fun fileName(photo: Photo): String = "unsplash-${photo.id}.jpg"
}