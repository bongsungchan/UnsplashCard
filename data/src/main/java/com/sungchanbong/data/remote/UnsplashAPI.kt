package com.sungchanbong.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashAPI {

    @GET("photos")
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("order_by") orderBy: String = "latest",
    )

    @GET("photos/{id}")
    suspend fun getPhotoDetail(
        @Path("id") id: String,
    )

    @GET("photos/{id}/download")
    suspend fun downloadPhoto(
        @Path("id") id: String,
    )
}