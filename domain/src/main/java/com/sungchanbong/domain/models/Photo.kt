package com.sungchanbong.domain.models

data class Photo(
    val id: String,
    val description: String?,
    val thumbUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val authorName: String,
    val authorUsername: String,
    val authorProfileImageUrl: String?,
    val likes: Int = 0,
    val isLike: Boolean = false,
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) width.toFloat() / height.toFloat() else DEFAULT_ASPECT_RATIO

    private companion object {
        const val DEFAULT_ASPECT_RATIO = 0.75f
    }
}


data class PhotoDetail(
    val photo: Photo,
    val views: Int?,
    val downloads: Int?,
    val location: String?,
    val exifModel: String?,
    val tags: List<String>,
    val isStale: Boolean
)
