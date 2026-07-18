package com.sungchanbong.data

import androidx.paging.PagingSource
import androidx.paging.testing.asSnapshot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.data.local.AppDatabase
import com.sungchanbong.data.local.PhotoDao
import com.sungchanbong.data.paging.PhotoMediator
import com.sungchanbong.data.remote.UnsplashAPI
import com.sungchanbong.data.remote.dto.DownloadDto
import com.sungchanbong.data.remote.dto.PhotoDetailDto
import com.sungchanbong.data.remote.dto.PhotoDto
import com.sungchanbong.data.repositories.PhotoRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class PhotoPagingIntegrationTest {

    private lateinit var db: AppDatabase
    private val clock = FakeClock(current = 1_000L)

    private class FakeApi(
        private val totalPhotos: Int,
        var offline: Boolean = false,
    ) : UnsplashAPI {
        val requestedPages = mutableListOf<Int>()

        override suspend fun getPhotos(page: Int, perPage: Int, orderBy: String): List<PhotoDto> {
            requestedPages += page
            if (offline) throw IOException("offline")
            val from = (page - 1) * perPage
            if (from >= totalPhotos) return emptyList()
            return (from until minOf(from + perPage, totalPhotos)).map { photoDto("p$it") }
        }

        override suspend fun getPhotoDetail(id: String): PhotoDetailDto = error("not used")
        override suspend fun downloadPhoto(id: String): DownloadDto = error("not used")
    }

    private fun repository(api: UnsplashAPI) = PhotoRepositoryImpl(
        api = api,
        photoDao = db.photoDao(),
        likePhotoDao = db.likePhotoDao(),
        photoMediator = PhotoMediator(api, db, clock),
        clock = clock,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `여러 페이지를 중복 누락 없이 이어 붙인다`() = runTest {
        val api = FakeApi(totalPhotos = 75)

        val items = repository(api).getPhotos()
            .asSnapshot { appendScrollWhile { it.id != "p74" } }

        assertEquals(75, items.size)
        assertEquals((0 until 75).map { "p$it" }, items.map { it.id })
        assertTrue(api.requestedPages.containsAll(listOf(1, 2, 3)))
    }

    @Test
    fun `REFRESH 가 실패해도 캐시된 피드는 지워지지 않는다`() = runTest {
        val api = FakeApi(totalPhotos = 30)
        repository(api).getPhotos().asSnapshot()
        assertEquals(30, db.photoDao().pagingSourceCountForTest())

        api.offline = true
        clock.current += PhotoMediator.CACHE_TIMEOUT_MS + 1

        runCatching { repository(api).getPhotos().asSnapshot() }

        assertEquals(30, db.photoDao().pagingSourceCountForTest())
    }

    @Test
    fun `캐시가 신선하면 오프라인에서도 네트워크 없이 피드가 나온다`() = runTest {
        val api = FakeApi(totalPhotos = 20)
        repository(api).getPhotos().asSnapshot()

        api.offline = true
        clock.current += 1
        api.requestedPages.clear()

        val items = repository(api).getPhotos().asSnapshot()

        assertEquals(20, items.size)
        assertEquals("p0", items.first().id)
        assertTrue("오프라인인데 네트워크를 탔다", api.requestedPages.isEmpty())
    }

    @Test
    fun `캐시가 신선하면 첫 페이지를 다시 받지 않는다`() = runTest {
        val api = FakeApi(totalPhotos = 30)
        repository(api).getPhotos().asSnapshot()

        clock.current += 1
        api.requestedPages.clear()
        repository(api).getPhotos().asSnapshot()

        assertTrue(
            "신선한 캐시인데 1페이지를 다시 요청했다: ${api.requestedPages}",
            1 !in api.requestedPages,
        )
    }

    @Test
    fun `좋아요 상태가 피드 아이템에 조인되어 나온다`() = runTest {
        val api = FakeApi(totalPhotos = 10)
        db.likePhotoDao().insert(likePhotoEntity("p3"))

        val items = repository(api).getPhotos().asSnapshot()

        assertEquals(true, items.first { it.id == "p3" }.isLike)
        assertEquals(false, items.first { it.id == "p0" }.isLike)
    }
}

private const val COUNT_ALL_PAGE_SIZE = 1000

private suspend fun PagingSource<Int, PhotoWithLike>.countOrZero(): Int {
    val params = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = COUNT_ALL_PAGE_SIZE,
        placeholdersEnabled = false,
    )
    return (load(params) as? PagingSource.LoadResult.Page)?.data?.size ?: 0
}

private suspend fun PhotoDao.pagingSourceCountForTest(): Int = pagingSource().countOrZero()
