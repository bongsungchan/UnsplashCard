package com.sungchanbong.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.data.entities.RemoteKeyEntity
import com.sungchanbong.data.local.AppDatabase
import com.sungchanbong.data.mapper.toEntityOrNull
import com.sungchanbong.data.remote.UnsplashAPI
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PhotoMediator @Inject constructor(
    private val api: UnsplashAPI,
    private val db: AppDatabase,
) : RemoteMediator<Int, PhotoWithLike>() {

    private val photoDao = db.photoDao()
    private val remoteKeyDao = db.remoteKeyDao()
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PhotoWithLike>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> STARTING_PAGE

            // 최신순 피드는 위로 더 불러올 것이 없다. 새 사진은 REFRESH 로만 들어온다.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {

                val cursor = remoteKeyDao.cursor()
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                cursor.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val photos = api.getPhotos(page = page, perPage = NETWORK_PAGE_SIZE)
            val endOfPagination = photos.size < NETWORK_PAGE_SIZE
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    photoDao.clear()
                    remoteKeyDao.clear()
                }

                val fetched =
                    photos.mapIndexedNotNull { i, dto -> dto.toEntityOrNull(sortIndex = i) }

                val existingIds = photoDao.findExistingIds(fetched.map { it.id }).toSet()
                val (duplicates, newcomers) = fetched.partition { it.id in existingIds }

                photoDao.updateKeepingPosition(duplicates)

                val baseIndex = (photoDao.maxSortIndex() ?: -1) + 1
                photoDao.upsertAll(
                    newcomers.mapIndexed { i, entity -> entity.copy(sortIndex = baseIndex + i) },
                )
                remoteKeyDao.upsert(
                    RemoteKeyEntity(
                        nextPage = if (endOfPagination) null else page + 1,
                        lastUpdatedAt = System.currentTimeMillis(),
                    ),
                )
            }
            MediatorResult.Success(endOfPaginationReached = endOfPagination)
        } catch (e: CancellationException) {
            throw e
        }
    }

    companion object {
        const val STARTING_PAGE = 1
        const val NETWORK_PAGE_SIZE = 30

    }
}
