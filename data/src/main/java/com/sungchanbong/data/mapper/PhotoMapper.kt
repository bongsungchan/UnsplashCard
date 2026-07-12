package com.sungchanbong.data.mapper

import com.bccard.unsplashexplorer.data.remote.dto.PhotoDetailDto
import com.bccard.unsplashexplorer.data.remote.dto.PhotoDto
import com.bccard.unsplashexplorer.data.remote.dto.UrlsDto
import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError

private fun UrlsDto.thumbOrFallback(): String = thumb ?: small ?: regular ?: full ?: raw.orEmpty()
private fun UrlsDto.fullOrFallback(): String = full ?: raw ?: regular ?: small.orEmpty()

private const val UNKNOWN_AUTHOR = "Unknown"

private fun missingField(field: String): PhotoError =
    PhotoError.Unexpected(IllegalStateException("'$field' missing in 200 response"))

fun PhotoDto.toEntityOrNull(sortIndex: Int): PhotoEntity? {
    val id = id ?: return null
    val urls = urls ?: return null
    return PhotoEntity(
        id = id,
        description = description ?: altDescription,
        thumbUrl = urls.thumbOrFallback(),
        fullUrl = urls.fullOrFallback(),
        width = width,
        height = height,
        authorName = user?.name ?: UNKNOWN_AUTHOR,
        authorUsername = user?.username.orEmpty(),
        authorProfileImageUrl = user?.profileImage?.medium,
        likes = likes,
        sortIndex = sortIndex,
    )
}


fun PhotoWithLike.toDomain(): Photo = photo.toDomain(isLike = isLike)

fun PhotoEntity.toDomain(isLike: Boolean): Photo = Photo(
    id = id,
    description = description,
    thumbUrl = thumbUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    authorName = authorName,
    authorUsername = authorUsername,
    authorProfileImageUrl = authorProfileImageUrl,
    likes = likes,
    isLike = isLike,
)

fun LikePhotoEntity.toDomain(): Photo = Photo(
    id = id,
    description = description,
    thumbUrl = thumbUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    authorName = authorName,
    authorUsername = authorUsername,
    authorProfileImageUrl = authorProfileImageUrl,
    likes = likes,
    isLike = true,
)

fun Photo.toFavoriteEntity(savedAt: Long): LikePhotoEntity = LikePhotoEntity(
    id = id,
    description = description,
    thumbUrl = thumbUrl,
    fullUrl = fullUrl,
    width = width,
    height = height,
    authorName = authorName,
    authorUsername = authorUsername,
    authorProfileImageUrl = authorProfileImageUrl,
    likes = likes,
    savedAt = savedAt,
)

fun PhotoDetailDto.toDomain(isFavorite: Boolean): PhotoDetail = PhotoDetail(
    photo = Photo(
        id = id ?: throw missingField("id"),
        description = description ?: altDescription,
        thumbUrl = (urls ?: throw missingField("urls")).thumbOrFallback(),
        fullUrl = urls.fullOrFallback(),
        width = width,
        height = height,
        authorName = user?.name ?: UNKNOWN_AUTHOR,
        authorUsername = user?.username.orEmpty(),
        authorProfileImageUrl = user?.profileImage?.medium,
        likes = likes,
        isLike = isFavorite,
    ),
    views = views,
    downloads = downloads,
    location = location?.let { loc ->
        listOfNotNull(loc.name ?: loc.city, loc.country)
            .distinct()
            .joinToString(", ")
            .ifBlank { null }
    },
    exifModel = exif?.let { listOfNotNull(it.make, it.model).joinToString(" ").ifBlank { null } },
    tags = tags?.mapNotNull { it.title }.orEmpty(),
    isStale = false,
)

fun Photo.toStaleDetail(): PhotoDetail = PhotoDetail(
    photo = this,
    views = null,
    downloads = null,
    location = null,
    exifModel = null,
    tags = emptyList(),
    isStale = true,
)