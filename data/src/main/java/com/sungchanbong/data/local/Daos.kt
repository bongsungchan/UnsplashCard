package com.sungchanbong.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sungchanbong.data.entities.LikePhotoEntity
import com.sungchanbong.data.entities.PhotoEntity
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.data.entities.RemoteKeyEntity
import kotlinx.coroutines.flow.Flow

private const val SQLITE_MAX_VARIABLES = 900

@Dao
interface PhotoDao {

    @Transaction
    @Query(
        """
        SELECT p.*, (f.id IS NOT NULL) AS isLike
        FROM photos AS p
        LEFT JOIN like_photos AS f ON p.id = f.id
        ORDER BY p.sortIndex ASC
        """,
    )
    fun pagingSource(): PagingSource<Int, PhotoWithLike>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    suspend fun clear()

    @Query("SELECT id FROM photos WHERE id IN (:ids)")
    suspend fun findExistingIdsChunk(ids: List<String>): List<String>
    suspend fun findExistingIds(ids: List<String>): List<String> =
        ids.chunked(SQLITE_MAX_VARIABLES).flatMap { chunk -> findExistingIdsChunk(chunk) }

    @Query(
        """
        UPDATE photos SET
            description = :description,
            thumbUrl = :thumbUrl,
            fullUrl = :fullUrl,
            width = :width,
            height = :height,
            authorName = :authorName,
            authorUsername = :authorUsername,
            authorProfileImageUrl = :authorProfileImageUrl,
            likes = :likes
        WHERE id = :id
        """,
    )
    suspend fun updateKeepingPosition(
        id: String,
        description: String?,
        thumbUrl: String,
        fullUrl: String,
        width: Int,
        height: Int,
        authorName: String,
        authorUsername: String,
        authorProfileImageUrl: String?,
        likes: Int,
    )

    suspend fun updateKeepingPosition(photos: List<PhotoEntity>) {
        photos.forEach { p ->
            updateKeepingPosition(
                id = p.id,
                description = p.description,
                thumbUrl = p.thumbUrl,
                fullUrl = p.fullUrl,
                width = p.width,
                height = p.height,
                authorName = p.authorName,
                authorUsername = p.authorUsername,
                authorProfileImageUrl = p.authorProfileImageUrl,
                likes = p.likes,
            )
        }
    }

    @Query("SELECT MAX(sortIndex) FROM photos")
    suspend fun maxSortIndex(): Int?

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): PhotoEntity?

}

@Dao
interface LikePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LikePhotoEntity)

    @Query("DELETE FROM like_photos WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM like_photos WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Transaction
    suspend fun toggle(entity: LikePhotoEntity): Boolean =
        if (exists(entity.id)) {
            delete(entity.id)
            false
        } else {
            insert(entity)
            true
        }

    @Query("SELECT * FROM like_photos ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<LikePhotoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM like_photos WHERE id = :id)")
    fun observeExists(id: String): Flow<Boolean>

    @Query("SELECT * FROM like_photos WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): LikePhotoEntity?
}


@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE id = ${RemoteKeyEntity.FEED_ID} LIMIT 1")
    suspend fun cursor(): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys")
    suspend fun clear()
}
