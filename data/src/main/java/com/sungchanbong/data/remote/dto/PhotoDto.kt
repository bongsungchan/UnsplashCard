package com.sungchanbong.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoDto(
    @Json(name = "id") val id: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "alt_description") val altDescription: String?,
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0,
    @Json(name = "likes") val likes: Int = 0,
    @Json(name = "urls") val urls: UrlsDto?,
    @Json(name = "user") val user: UserDto?,
)

@JsonClass(generateAdapter = true)
data class UrlsDto(
    @Json(name = "raw") val raw: String?,
    @Json(name = "full") val full: String?,
    @Json(name = "regular") val regular: String?,
    @Json(name = "small") val small: String?,
    @Json(name = "thumb") val thumb: String?,
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "name") val name: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "profile_image") val profileImage: ProfileImageDto?,
)

@JsonClass(generateAdapter = true)
data class ProfileImageDto(
    @Json(name = "medium") val medium: String?,
)
