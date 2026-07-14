package com.sungchanbong.data.remote

import com.sungchanbong.data.remote.dto.DownloadDto
import com.sungchanbong.data.remote.dto.PhotoDetailDto
import com.sungchanbong.data.remote.dto.PhotoDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashAPI {

    @GET("photos")
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("order_by") orderBy: String = "latest",
    ): List<PhotoDto>

    @GET("photos/{id}")
    suspend fun getPhotoDetail(
        @Path("id") id: String,
    ): PhotoDetailDto

    @GET("photos/{id}/download")
    suspend fun downloadPhoto(
        @Path("id") id: String,
    ): DownloadDto
}