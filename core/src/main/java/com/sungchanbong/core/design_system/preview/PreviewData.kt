package com.sungchanbong.core.design_system.preview

import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail

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

fun previewPhotos(count: Int = 6): List<Photo> = List(count) { index ->
    previewPhoto(
        id = "p$index",
        description = if (index % 3 == 0) null else "사진 설명 $index",
        authorName = "작가 $index",
        isLike = index % 2 == 0,
    )
}

fun previewPhotoDetail(
    isLike: Boolean = true,
    isStale: Boolean = false,
) = PhotoDetail(
    photo = previewPhoto(isLike = isLike),
    views = 12_345,
    downloads = 678,
    location = "서울, 대한민국",
    exifModel = "Test",
    tags = listOf("nature", "forest", "sunrise"),
    isStale = isStale,
)