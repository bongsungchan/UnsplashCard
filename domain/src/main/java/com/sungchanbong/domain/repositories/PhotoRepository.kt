package com.sungchanbong.domain.repositories

import androidx.paging.PagingData
import com.sungchanbong.domain.models.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getPhotos(): Flow<PagingData<Photo>>
}