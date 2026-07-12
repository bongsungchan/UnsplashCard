package com.sungchanbong.data.mapper

import com.bccard.unsplashexplorer.data.remote.dto.PhotoDetailDto
import com.bccard.unsplashexplorer.data.remote.dto.PhotoDto
import com.bccard.unsplashexplorer.data.remote.dto.UrlsDto
import com.sungchanbong.data.entities.PhotoEntity
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail

fun PhotoEntity.toDomain(isFavorite: Boolean): Photo = Photo(
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
    isLike = isFavorite,
)

fun PhotoWithLike.toDomain(): Photo = photo.toDomain(isFavorite = isLike)

private fun UrlsDto.thumbOrFallback(): String = thumb ?: small ?: regular ?: full ?: raw.orEmpty()
private fun UrlsDto.fullOrFallback(): String = full ?: raw ?: regular ?: small.orEmpty()
private const val UNKNOWN_AUTHOR = "Unknown"


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

fun PhotoDetailDto.toDomain(isLike: Boolean): PhotoDetail = PhotoDetail(
    photo = Photo(
        id = id ?: "",
        description = description ?: altDescription,
        thumbUrl = urls?.fullOrFallback() ?: "",
        fullUrl = urls?.fullOrFallback() ?: "",
        width = width,
        height = height,
        authorName = user?.name ?: UNKNOWN_AUTHOR,
        authorUsername = user?.username.orEmpty(),
        authorProfileImageUrl = user?.profileImage?.medium,
        likes = likes,
        isLike = isLike,
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
    tags = tags?.mapNotNull { it.title }.orEmpty()
)