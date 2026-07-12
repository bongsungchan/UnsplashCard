package com.sungchanbong.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "like_photos",
    indices = [Index("savedAt")],
)
data class LikePhotoEntity(
    @PrimaryKey val id: String,
    val description: String?,
    val thumbUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val authorName: String,
    val authorUsername: String,
    val authorProfileImageUrl: String?,
    val likes: Int,
    val savedAt: Long,
)