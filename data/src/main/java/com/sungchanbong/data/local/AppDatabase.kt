package com.sungchanbong.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity

@Database(
    entities = [
        PhotoEntity::class,
        LikePhotoEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun likePhotoDao(): LikePhotoDao

    companion object {
        const val NAME = "unsplash_card.db"
    }
}
