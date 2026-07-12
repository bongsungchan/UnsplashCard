package com.sungchanbong.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val id: Int = FEED_ID,
    val nextPage: Int?,
    val lastUpdatedAt: Long,
) {
    companion object {
        const val FEED_ID = 0
    }
}