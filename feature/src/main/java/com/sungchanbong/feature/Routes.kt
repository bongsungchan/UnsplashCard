package com.sungchanbong.feature

import kotlinx.serialization.Serializable

@Serializable
data object MainListRoute

@Serializable
data object LikePhotoRoute

@Serializable
data class PhotoDetailRoute(val photoId: String)

