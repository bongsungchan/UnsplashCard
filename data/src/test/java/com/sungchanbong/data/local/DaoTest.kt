package com.sungchanbong.data.local

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.sungchanbong.data.likePhotoEntity
import com.sungchanbong.data.photoEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var photoDao: PhotoDao
    private lateinit var likePhotoDao: LikePhotoDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        photoDao = db.photoDao()
        likePhotoDao = db.likePhotoDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `pagingSource 는 sortIndex 순서를 보존한다`() = runTest {
        photoDao.upsertAll(
            listOf(
                photoEntity("c", sortIndex = 2),
                photoEntity("a", sortIndex = 0),
                photoEntity("b", sortIndex = 1),
            ),
        )

        val page = photoDao.pagingSource().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf("a", "b", "c"), page.data.map { it.photo.id })
    }

    @Test
    fun `pagingSource 의 isLike 는 LEFT JOIN 으로 채워진다`() = runTest {
        photoDao.upsertAll(listOf(photoEntity("a", 0), photoEntity("b", 1)))
        likePhotoDao.insert(likePhotoEntity("b"))

        val page = photoDao.pagingSource().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(false, page.data.first { it.photo.id == "a" }.isLike)
        assertEquals(true, page.data.first { it.photo.id == "b" }.isLike)
    }

    @Test
    fun `toggle 은 없으면 넣고 있으면 지운다`() = runTest {
        val entity = likePhotoEntity("p1")

        assertTrue(likePhotoDao.toggle(entity))
        assertTrue(likePhotoDao.exists("p1"))

        assertFalse(likePhotoDao.toggle(entity))
        assertFalse(likePhotoDao.exists("p1"))
    }


    @Test
    fun `toggle 을 실제 병렬로 짝수번 호출해도 최종 상태는 해제다`() = runTest {
        val entity = likePhotoEntity("p1")

        @OptIn(DelicateCoroutinesApi::class)
        val threads = newFixedThreadPoolContext(CONCURRENCY, "toggle-race")
        threads.use { threads ->
            withContext(threads) {
                List(TOGGLE_COUNT) { async { likePhotoDao.toggle(entity) } }.awaitAll()
            }
        }

        assertFalse("연타 후 최종 상태가 사용자 조작과 어긋납니다", likePhotoDao.exists("p1"))
    }

    @Test
    fun `피드 캐시를 비워도 좋아요는 살아남는다`() = runTest {
        photoDao.upsertAll(listOf(photoEntity("a", 0)))
        likePhotoDao.insert(likePhotoEntity("a"))
        photoDao.clear()

        assertTrue(likePhotoDao.exists("a"))
        assertEquals("a", likePhotoDao.findById("a")?.id)
    }


    @Test
    fun `observeExists 는 좋아요 변화를 흘려보낸다`() = runTest {
        withContext(Dispatchers.IO) {
            likePhotoDao.observeExists("p1").test {
                assertFalse("구독 시작 시점의 초기값", awaitItem())

                likePhotoDao.insert(likePhotoEntity("p1"))
                assertTrue("insert 가 흘러오지 않았습니다", awaitItem())

                likePhotoDao.delete("p1")
                assertFalse("delete 가 흘러오지 않았습니다", awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private companion object {
        const val CONCURRENCY = 4
        const val TOGGLE_COUNT = 20
    }
}