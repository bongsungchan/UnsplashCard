package com.sungchanbong.data.remote

import com.squareup.moshi.Moshi
import com.sungchanbong.data.mapper.toEntityOrNull
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class UnsplashAPITest {

    private lateinit var server: MockWebServer
    private lateinit var api: UnsplashAPI

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
            .create(UnsplashAPI::class.java)
    }

    @After
    fun tearDown() = server.close()

    private val authInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID test_key")
                .build(),
        )
    }

    private fun enqueue(body: String, code: Int = 200) {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    @Test
    fun `사진 목록의 snake_case 필드가 DTO 로 매핑된다`() = runTest {
        enqueue(
            """
            [
              {
                "id": "p1",
                "description": null,
                "alt_description": "A lone tree",
                "width": 4000,
                "height": 3000,
                "likes": 42,
                "urls": { "raw": "r", "full": "f", "regular": "reg", "small": "s", "thumb": "t" },
                "user": {
                  "name": "Alex Smith",
                  "username": "alex",
                  "profile_image": { "medium": "http://profile/medium.jpg" }
                }
              }
            ]
            """.trimIndent(),
        )

        val photos = api.getPhotos(page = 1, perPage = 30)

        assertEquals(1, photos.size)
        val p = photos.first()
        assertEquals("p1", p.id)
        assertEquals("A lone tree", p.altDescription)
        assertEquals("http://profile/medium.jpg", p.user?.profileImage?.medium)
        assertEquals("f", p.urls?.full)
        assertEquals("t", p.urls?.thumb)
        assertEquals(42, p.likes)
        assertEquals(4000, p.width)
    }

    @Test
    fun `요청에 페이지 파라미터와 인증 헤더가 실린다`() = runTest {
        enqueue("[]")

        api.getPhotos(page = 3, perPage = 30)

        val request = server.takeRequest()
        val url = request.url
        assertEquals("3", url.queryParameter("page"))
        assertEquals("30", url.queryParameter("per_page"))
        assertEquals("latest", url.queryParameter("order_by"))
        assertEquals("Client-ID test_key", request.headers["Authorization"])
    }

    @Test
    fun `표시 불가능한 항목이 섞여 있어도 나머지 항목은 살아남는다`() = runTest {
        enqueue(
            """
            [
              { "id": "broken" },
              {
                "id": "ok",
                "alt_description": "fine",
                "width": 100, "height": 200, "likes": 1,
                "urls": { "raw": "r", "full": "f", "regular": "reg", "small": "s", "thumb": "t" },
                "user": { "name": "A", "username": "a", "profile_image": { "medium": "m" } }
              }
            ]
            """.trimIndent(),
        )

        val photos = api.getPhotos(page = 1, perPage = 30)

        assertEquals(2, photos.size)
        assertNull(photos[0].urls)

        assertNull(photos[0].toEntityOrNull(sortIndex = 0))
        assertEquals("ok", photos[1].toEntityOrNull(sortIndex = 1)?.id)
    }

    @Test
    fun `상세 응답의 exif·location·tags 가 파싱된다`() = runTest {
        enqueue(
            """
            {
              "id": "p1",
              "description": "desc",
              "alt_description": "alt",
              "width": 4000, "height": 3000, "likes": 42,
              "views": 12345,
              "downloads": 678,
              "urls": { "raw": "r", "full": "f", "regular": "reg", "small": "s", "thumb": "t" },
              "user": { "name": "Alex", "username": "alex", "profile_image": { "medium": "m" } },
              "exif": { "make": "Canon", "model": "EOS R5" },
              "location": { "name": "Seoul", "city": "Seoul", "country": "KR" },
              "tags": [ { "title": "nature" }, { "title": "tree" } ]
            }
            """.trimIndent(),
        )

        val detail = api.getPhotoDetail("p1")

        assertEquals(12345, detail.views)
        assertEquals(678, detail.downloads)
        assertEquals("EOS R5", detail.exif?.model)
        assertEquals("Seoul", detail.location?.city)
        assertEquals(listOf("nature", "tree"), detail.tags?.map { it.title })
    }

    @Test
    fun `상세의 선택 필드가 없어도 파싱된다`() = runTest {
        enqueue(
            """
            {
              "id": "p1",
              "description": null,
              "alt_description": null,
              "width": 100, "height": 200,
              "urls": { "raw": "r", "full": "f", "regular": "reg", "small": "s", "thumb": "t" },
              "user": { "name": "A", "username": "a", "profile_image": null }
            }
            """.trimIndent(),
        )

        val detail = api.getPhotoDetail("p1")

        assertNull(detail.views)
        assertNull(detail.exif)
        assertNull(detail.location)
        assertNull(detail.tags)
        assertEquals(0, detail.likes)
    }

    @Test
    fun `다운로드 트리거 응답의 url 이 파싱된다`() = runTest {
        enqueue("""{ "url": "http://images.unsplash.com/p1?ixid=abc" }""")

        val dto = api.downloadPhoto("p1")

        assertEquals("http://images.unsplash.com/p1?ixid=abc", dto.url)
        assertTrue(server.takeRequest().url.encodedPath.endsWith("/photos/p1/download"))
    }
}