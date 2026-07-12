package com.sungchanbong.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity
import com.sungchanbong.data.entities.RemoteKeyEntity

@Database(
    entities = [
        PhotoEntity::class,
        LikePhotoEntity::class,
        RemoteKeyEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun likePhotoDao(): LikePhotoDao
    abstract fun remoteKeyDao(): RemoteKeyDao


    companion object {
        const val NAME = "unsplash_card.db"
    }
}
