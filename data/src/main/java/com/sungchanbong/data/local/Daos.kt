package com.sungchanbong.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    suspend fun clear()
}

@Dao
interface LikePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LikePhotoEntity)

    @Query("DELETE FROM like_photos WHERE id = :id")
    suspend fun delete(id: String)

}
