package com.sungchanbong.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoDetailDto(
    @Json(name = "id") val id: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "alt_description") val altDescription: String?,
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0,
    @Json(name = "likes") val likes: Int = 0,
    @Json(name = "views") val views: Int?,
    @Json(name = "downloads") val downloads: Int?,
    @Json(name = "urls") val urls: UrlsDto?,
    @Json(name = "user") val user: UserDto?,
    @Json(name = "exif") val exif: ExifDto?,
    @Json(name = "location") val location: LocationDto?,
    @Json(name = "tags") val tags: List<TagDto>?,
)

@JsonClass(generateAdapter = true)
data class ExifDto(
    @Json(name = "make") val make: String?,
    @Json(name = "model") val model: String?,
)

@JsonClass(generateAdapter = true)
data class LocationDto(
    @Json(name = "name") val name: String?,
    @Json(name = "city") val city: String?,
    @Json(name = "country") val country: String?,
)

@JsonClass(generateAdapter = true)
data class TagDto(
    @Json(name = "title") val title: String?,
)

@JsonClass(generateAdapter = true)
data class DownloadDto(
    @Json(name = "url") val url: String?,
)
