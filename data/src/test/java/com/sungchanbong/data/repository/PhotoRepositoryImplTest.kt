package com.sungchanbong.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.JsonDataException
import com.sungchanbong.data.FakeClock
import com.sungchanbong.data.likePhotoEntity
import com.sungchanbong.data.local.AppDatabase
import com.sungchanbong.data.paging.PhotoMediator
import com.sungchanbong.data.photo
import com.sungchanbong.data.photoEntity
import com.sungchanbong.data.remote.UnsplashAPI
import com.sungchanbong.data.remote.dto.DownloadDto
import com.sungchanbong.data.remote.dto.PhotoDetailDto
import com.sungchanbong.data.remote.dto.ProfileImageDto
import com.sungchanbong.data.remote.dto.UrlsDto
import com.sungchanbong.data.remote.dto.UserDto
import com.sungchanbong.data.repositories.PhotoRepositoryImpl
import com.sungchanbong.domain.models.PhotoError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class PhotoRepositoryImplTest {

    private val api: UnsplashAPI = mockk()
    private val clock = FakeClock(current = 5_000L)
    private lateinit var db: AppDatabase
    private lateinit var repository: PhotoRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()

        repository = PhotoRepositoryImpl(
            api = api,
            photoDao = db.photoDao(),
            likePhotoDao = db.likePhotoDao(),
            photoMediator = PhotoMediator(api, db, clock),
            clock = clock,
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `상세는 네트워크 성공 시 신선한 데이터를 준다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } returns detailDto()

        val result = repository.getPhotoDetail("p1")

        val detail = result.getOrThrow()
        assertEquals(120, detail.views)
        assertFalse(detail.isStale)
    }

    @Test
    fun `취소 예외는 삼키지 않고 그대로 던진다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getPhotoDetail("p1") }
        }
    }

    @Test
    fun `오프라인이면 좋아요 스냅샷으로 폴백하고 stale 로 표시한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws IOException("offline")

        val detail = repository.getPhotoDetail("p1").getOrThrow()

        assertTrue(detail.photo.isLike)
        assertTrue(detail.isStale)
        assertNull(detail.views)
    }


    @Test
    fun `좋아요하지 않아도 피드 캐시가 있으면 폴백한다`() = runTest {
        db.photoDao().upsertAll(listOf(photoEntity("p1")))
        coEvery { api.getPhotoDetail("p1") } throws IOException("offline")

        val detail = repository.getPhotoDetail("p1").getOrThrow()

        assertEquals("p1", detail.photo.id)
        assertFalse(detail.photo.isLike)
        assertTrue(detail.isStale)
    }

    @Test
    fun `캐시가 전혀 없으면 도메인 에러로 실패한다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } throws IOException("offline")

        val result = repository.getPhotoDetail("p1")

        assertEquals(PhotoError.Network, result.exceptionOrNull())
    }

    @Test
    fun `404 는 NotFound 로 매핑된다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } throws httpException(404)

        val result = repository.getPhotoDetail("p1")

        assertEquals(PhotoError.NotFound, result.exceptionOrNull())
    }


    @Test
    fun `캐시가 있어도 404 는 NotFound 로 실패한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws httpException(404)

        val result = repository.getPhotoDetail("p1")

        assertEquals(PhotoError.NotFound, result.exceptionOrNull())
    }

    @Test
    fun `캐시가 있어도 401 은 Unauthorized 로 실패한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws httpException(401)

        val result = repository.getPhotoDetail("p1")

        assertEquals(PhotoError.Unauthorized, result.exceptionOrNull())
    }


    @Test
    fun `RateLimited 는 캐시가 있으면 폴백한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws
                httpException(403, headers = mapOf("X-Ratelimit-Remaining" to "0"))

        val detail = repository.getPhotoDetail("p1").getOrThrow()

        assertTrue(detail.isStale)
    }

    @Test
    fun `스키마 위반은 크래시하지 않고 도메인 에러가 된다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } throws JsonDataException("'urls' missing")

        val result = repository.getPhotoDetail("p1")

        assertTrue(result.exceptionOrNull() is PhotoError.Unexpected)
    }

    @Test
    fun `스키마 위반이어도 캐시가 있으면 폴백한다`() = runTest {
        db.likePhotoDao().insert(likePhotoEntity("p1"))
        coEvery { api.getPhotoDetail("p1") } throws JsonDataException("'urls' missing")

        val detail = repository.getPhotoDetail("p1").getOrThrow()

        assertTrue(detail.isStale)
        assertEquals("p1", detail.photo.id)
    }

    @Test
    fun `403 은 남은 요청이 0 일 때만 RateLimited 다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } throws
                httpException(403, headers = mapOf("X-Ratelimit-Remaining" to "0"))

        assertEquals(PhotoError.RateLimited, repository.getPhotoDetail("p1").exceptionOrNull())
    }

    @Test
    fun `한도가 남았는데 403 이면 인증 문제로 본다`() = runTest {
        coEvery { api.getPhotoDetail("p1") } throws
                httpException(403, headers = mapOf("X-Ratelimit-Remaining" to "42"))

        assertEquals(PhotoError.Unauthorized, repository.getPhotoDetail("p1").exceptionOrNull())
    }

    @Test
    fun `좋아요 토글은 엔티티 내용을 그대로 저장한다`() = runTest {
        repository.togglePhotoLike(photo("p1")).getOrThrow()

        val saved = db.likePhotoDao().findById("p1")

        assertEquals("Alex", saved?.authorName)
        assertEquals(7, saved?.likes)
        assertEquals(5_000L, saved?.savedAt)
    }

    @Test
    fun `좋아요를 두 번 토글하면 해제된다`() = runTest {
        repository.togglePhotoLike(photo("p1")).getOrThrow()
        repository.togglePhotoLike(photo("p1")).getOrThrow()

        assertFalse(db.likePhotoDao().exists("p1"))
    }

    @Test
    fun `다운로드 트리거는 실제 다운로드 URL 을 반환한다`() = runTest {
        coEvery { api.downloadPhoto("p1") } returns DownloadDto(url = "https://dl/p1")

        val url = repository.photoDownload("p1").getOrThrow()

        assertEquals("https://dl/p1", url)
        coVerify(exactly = 1) { api.downloadPhoto("p1") }
    }

    @Test
    fun `200 인데 url 이 없으면 조용히 성공하지 않는다`() = runTest {
        coEvery { api.downloadPhoto("p1") } returns DownloadDto(url = null)

        val result = repository.photoDownload("p1")

        assertTrue(result.exceptionOrNull() is PhotoError.Unexpected)
    }

    private fun detailDto() = PhotoDetailDto(
        id = "p1",
        description = "d",
        altDescription = null,
        width = 400,
        height = 600,
        likes = 7,
        views = 120,
        downloads = 30,
        urls = UrlsDto("raw", "full", "regular", "small", "thumb"),
        user = UserDto("Alex", "alex", ProfileImageDto("profile")),
        exif = null,
        location = null,
        tags = null,
    )

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
