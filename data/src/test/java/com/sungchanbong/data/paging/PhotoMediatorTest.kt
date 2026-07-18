package com.sungchanbong.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.JsonDataException
import com.sungchanbong.data.FakeClock
import com.sungchanbong.data.entities.PhotoWithLike
import com.sungchanbong.data.likePhotoEntity
import com.sungchanbong.data.local.AppDatabase
import com.sungchanbong.data.photoDto
import com.sungchanbong.data.remote.UnsplashAPI
import com.sungchanbong.domain.models.PhotoError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class PhotoMediatorTest {

    private val api: UnsplashAPI = mockk()
    private val clock = FakeClock(current = 1_000L)
    private lateinit var db: AppDatabase
    private lateinit var mediator: PhotoMediator

    private val pageSize = PhotoMediator.NETWORK_PAGE_SIZE

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        mediator = PhotoMediator(api, db, clock)
    }

    @After
    fun tearDown() = db.close()

    private fun emptyState() = PagingState<Int, PhotoWithLike>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    private suspend fun loadedItems(): List<PhotoWithLike> {
        val result = db.photoDao().pagingSource().load(
            PagingSource.LoadParams.Refresh(null, 100, false),
        ) as PagingSource.LoadResult.Page
        return result.data
    }
    private suspend fun stateWithLoadedItems(): PagingState<Int, PhotoWithLike> {
        val items = loadedItems()
        return PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = items,
                    prevKey = null,
                    nextKey = null,
                ),
            ),
            anchorPosition = items.lastIndex,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )
    }

    @Test
    fun `REFRESH 는 첫 페이지를 적재하고 sortIndex 를 부여한다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val items = loadedItems()
        assertEquals(pageSize, items.size)
        assertEquals(listOf(0, 1, 2), items.take(3).map { it.photo.sortIndex })
    }

    @Test
    fun `마지막 페이지면 endOfPaginationReached 가 true 다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize - 1) { photoDto("p$it") }

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH 는 피드를 비우지만 좋아요는 보존한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("keep-me"))
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(db.likePhotoDao().exists("keep-me"))
        assertEquals(pageSize, loadedItems().size)
    }

    @Test
    fun `APPEND 는 다음 페이지를 이어서 적재한다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                List(pageSize) { photoDto("p2-$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        val result = mediator.load(LoadType.APPEND, stateWithLoadedItems())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val items = loadedItems()
        assertEquals(pageSize * 2, items.size)
        assertEquals("p1-0", items.first().photo.id)
        assertEquals("p2-${pageSize - 1}", items.last().photo.id)
    }

    @Test
    fun `APPEND 의 sortIndex 는 이전 페이지에 이어서 증가한다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                List(pageSize) { photoDto("p2-$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, stateWithLoadedItems())

        val sortIndexes = loadedItems().map { it.photo.sortIndex }
        assertEquals((0 until pageSize * 2).toList(), sortIndexes)
    }

    @Test
    fun `빈 PagingState 로 APPEND 가 불려도 페이지네이션을 끝내지 않는다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                List(pageSize) { photoDto("p2-$it") }

        mediator.load(LoadType.REFRESH, emptyState())

        val result = mediator.load(LoadType.APPEND, emptyState())

        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(pageSize * 2, loadedItems().size)
    }

    @Test
    fun `페이지가 전부 중복이어도 커서는 전진해 다음 페이지를 받는다`() = runTest {
        val page1 = List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns page1
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns page1
        coEvery { api.getPhotos(page = 3, perPage = pageSize) } returns
                List(pageSize) { photoDto("p3-$it") }

        mediator.load(LoadType.REFRESH, emptyState())

        val dup = mediator.load(LoadType.APPEND, stateWithLoadedItems())
        assertFalse((dup as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(pageSize, loadedItems().size)

        mediator.load(LoadType.APPEND, stateWithLoadedItems())

        coVerify(exactly = 1) { api.getPhotos(page = 2, perPage = pageSize) }
        coVerify(exactly = 1) { api.getPhotos(page = 3, perPage = pageSize) }
        assertEquals(pageSize * 2, loadedItems().size)
        assertEquals("p3-${pageSize - 1}", loadedItems().last().photo.id)
    }

    @Test
    fun `끝사진을 포함하지 않는 중복 페이지에서도 커서는 전진한다`() = runTest {
        val page1 = List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns page1
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-${it % (pageSize - 1)}") }
        coEvery { api.getPhotos(page = 3, perPage = pageSize) } returns
                List(pageSize) { photoDto("p3-$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, stateWithLoadedItems())
        mediator.load(LoadType.APPEND, stateWithLoadedItems())

        coVerify(exactly = 1) { api.getPhotos(page = 2, perPage = pageSize) }
        coVerify(exactly = 1) { api.getPhotos(page = 3, perPage = pageSize) }
    }

    @Test
    fun `캐시가 비었는데 APPEND 가 불리면 끝났다고 단정하지 않는다`() = runTest {
        val result = mediator.load(LoadType.APPEND, emptyState())

        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `APPEND 는 마지막 페이지 이후 종료한다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                List(pageSize - 1) { photoDto("p2-$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        val last = mediator.load(LoadType.APPEND, stateWithLoadedItems())
        assertTrue((last as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val afterEnd = mediator.load(LoadType.APPEND, stateWithLoadedItems())
        assertTrue((afterEnd as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 1) { api.getPhotos(page = 2, perPage = pageSize) }
    }

    @Test
    fun `다음 페이지에 중복이 섞여 와도 사진이 자리를 옮기지 않는다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                listOf(photoDto("p${pageSize - 2}"), photoDto("p${pageSize - 1}")) +
                List(pageSize - 2) { photoDto("new$it") }

        mediator.load(LoadType.REFRESH, emptyState())
        val before = loadedItems().associate { it.photo.id to it.photo.sortIndex }

        mediator.load(LoadType.APPEND, stateWithLoadedItems())

        val after = loadedItems()
        val duplicated = listOf("p${pageSize - 2}", "p${pageSize - 1}")
        duplicated.forEach { id ->
            assertEquals(before[id], after.first { it.photo.id == id }.photo.sortIndex)
        }
        assertEquals(pageSize + (pageSize - 2), after.size)
        assertEquals("new${pageSize - 3}", after.last().photo.id)
    }

    @Test
    fun `중복 항목의 내용은 최신으로 갱신된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } returns
                listOf(photoDto("p0").copy(likes = 999))

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, stateWithLoadedItems())

        val p0 = loadedItems().first { it.photo.id == "p0" }
        assertEquals(999, p0.photo.likes)
        assertEquals(0, p0.photo.sortIndex)
    }

    @Test
    fun `APPEND 실패해도 기존 캐시는 살아남는다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p1-$it") }
        coEvery { api.getPhotos(page = 2, perPage = pageSize) } throws IOException("offline")

        mediator.load(LoadType.REFRESH, emptyState())
        val result = mediator.load(LoadType.APPEND, stateWithLoadedItems())

        assertEquals(PhotoError.Network, (result as RemoteMediator.MediatorResult.Error).throwable)
        assertEquals(pageSize, loadedItems().size)
    }

    @Test
    fun `PREPEND 는 즉시 종료한다`() = runTest {
        val result = mediator.load(LoadType.PREPEND, emptyState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `네트워크 실패는 도메인 에러로 변환된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } throws IOException("offline")

        val result = mediator.load(LoadType.REFRESH, emptyState())

        val error = (result as RemoteMediator.MediatorResult.Error).throwable
        assertEquals(PhotoError.Network, error)
    }

    @Test
    fun `한도 소진된 403 은 RateLimited 로 매핑된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } throws
                httpException(403, headers = mapOf("X-Ratelimit-Remaining" to "0"))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertEquals(
            PhotoError.RateLimited,
            (result as RemoteMediator.MediatorResult.Error).throwable,
        )
    }

    @Test
    fun `한도가 남은 403 은 Unauthorized 로 매핑된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } throws
                httpException(403, headers = mapOf("X-Ratelimit-Remaining" to "50"))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertEquals(
            PhotoError.Unauthorized,
            (result as RemoteMediator.MediatorResult.Error).throwable,
        )
    }

    @Test
    fun `401 은 Unauthorized 로 매핑된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } throws httpException(401)

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertEquals(
            PhotoError.Unauthorized,
            (result as RemoteMediator.MediatorResult.Error).throwable,
        )
    }

    @Test
    fun `표시 불가능한 항목은 건너뛰고 나머지 페이지는 살린다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns listOf(
            photoDto("ok-1"),
            photoDto("broken").copy(urls = null),
            photoDto("no-id").copy(id = null),
            photoDto("ok-2"),
        )

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertEquals(listOf("ok-1", "ok-2"), loadedItems().map { it.photo.id })
    }

    @Test
    fun `스키마 위반은 도메인 에러로 변환된다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } throws
                JsonDataException("Required value 'urls' missing")

        val result = mediator.load(LoadType.REFRESH, emptyState())

        val error = (result as RemoteMediator.MediatorResult.Error).throwable
        assertTrue(error is PhotoError.Unexpected)
    }

    @Test
    fun `캐시가 신선하면 초기 새로고침을 건너뛴다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }
        mediator.load(LoadType.REFRESH, emptyState())

        clock.current += PhotoMediator.CACHE_TIMEOUT_MS - 1

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `캐시가 만료되면 초기 새로고침을 실행한다`() = runTest {
        coEvery { api.getPhotos(page = 1, perPage = pageSize) } returns
                List(pageSize) { photoDto("p$it") }
        mediator.load(LoadType.REFRESH, emptyState())

        clock.current += PhotoMediator.CACHE_TIMEOUT_MS + 1

        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `캐시가 비어 있으면 초기 새로고침을 실행한다`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }
    private fun httpException(code: Int, headers: Map<String, String> = emptyMap()) =
        HttpException(
            Response.error<Unit>(
                "".toResponseBody("application/json".toMediaType()),
                okhttp3.Response.Builder()
                    .code(code)
                    .message("error")
                    .protocol(Protocol.HTTP_1_1)
                    .request(Request.Builder().url("https://api.unsplash.com/").build())
                    .apply { headers.forEach { (name, value) -> header(name, value) } }
                    .build(),
            ),
        )
}
