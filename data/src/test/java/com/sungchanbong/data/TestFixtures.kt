package com.sungchanbong.data

import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity
import com.sungchanbong.data.remote.dto.PhotoDto
import com.sungchanbong.data.remote.dto.ProfileImageDto
import com.sungchanbong.data.remote.dto.UrlsDto
import com.sungchanbong.data.remote.dto.UserDto
import com.sungchanbong.data.util.Clock
import com.sungchanbong.domain.models.Photo

fun photoDto(id: String = "p1"): PhotoDto = PhotoDto(
    id = id,
    description = "desc-$id",
    altDescription = null,
    width = 400,
    height = 600,
    likes = 7,
    urls = UrlsDto(
        raw = "raw",
        full = "full",
        regular = "regular",
        small = "small",
        thumb = "thumb",
    ),
    user = UserDto(
        name = "Alex",
        username = "alex",
        profileImage = ProfileImageDto(medium = "profile"),
    ),
)

fun photoEntity(id: String = "p1", sortIndex: Int = 0): PhotoEntity = PhotoEntity(
    id = id,
    description = "desc-$id",
    thumbUrl = "thumb",
    fullUrl = "full",
    width = 400,
    height = 600,
    authorName = "Alex",
    authorUsername = "alex",
    authorProfileImageUrl = "profile",
    likes = 7,
    sortIndex = sortIndex,
)

fun likePhotoEntity(id: String = "p1", savedAt: Long = 1_000L): LikePhotoEntity =
    LikePhotoEntity(
        id = id,
        description = "desc-$id",
        thumbUrl = "thumb",
        fullUrl = "full",
        width = 400,
        height = 600,
        authorName = "Alex",
        authorUsername = "alex",
        authorProfileImageUrl = "profile",
        likes = 7,
        savedAt = savedAt,
    )

fun photo(id: String = "p1", isLike: Boolean = false): Photo = Photo(
    id = id,
    description = "desc-$id",
    thumbUrl = "thumb",
    fullUrl = "full",
    width = 400,
    height = 600,
    authorName = "Alex",
    authorUsername = "alex",
    authorProfileImageUrl = "profile",
    likes = 7,
    isLike = isLike,
)

class FakeClock(var current: Long = 0L) : Clock {
    override fun now(): Long = current
}