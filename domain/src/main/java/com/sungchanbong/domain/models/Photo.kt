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