package com.sungchanbong.core.design_system.preview

import com.sungchanbong.domain.models.Photo

fun previewPhoto(
    id: String = "p1",
    description: String? = "안개 낀 숲 위로 떠오르는 아침 해",
    authorName: String = "Alex Smith",
    isLike: Boolean = false,
) = Photo(
    id = id,
    description = description,
    thumbUrl = "",
    fullUrl = "",
    width = 400,
    height = 600,
    authorName = authorName,
    authorUsername = "alex",
    authorProfileImageUrl = null,
    likes = 128,
    isLike = isLike,
)