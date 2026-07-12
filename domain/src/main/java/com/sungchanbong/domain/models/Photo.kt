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
)

data class PhotoDetail(
    val photo: Photo,
    val views: Int?,
    val downloads: Int?,
    val location: String?,
    val exifModel: String?,
    val tags: List<String>,
    val isStale: Boolean
)
