package com.sungchanbong.data

import com.sungchanbong.data.mapper.toDomain
import com.sungchanbong.data.mapper.toEntityOrNull
import com.sungchanbong.data.remote.dto.ExifDto
import com.sungchanbong.data.remote.dto.LocationDto
import com.sungchanbong.data.remote.dto.PhotoDetailDto
import com.sungchanbong.data.remote.dto.ProfileImageDto
import com.sungchanbong.data.remote.dto.TagDto
import com.sungchanbong.data.remote.dto.UrlsDto
import com.sungchanbong.data.remote.dto.UserDto
import com.sungchanbong.domain.models.PhotoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private fun urls(
        raw: String? = "raw",
        full: String? = "full",
        regular: String? = "regular",
        small: String? = "small",
        thumb: String? = "thumb",
    ) = UrlsDto(raw, full, regular, small, thumb)

    private fun detailDto(
        location: LocationDto? = null,
        exif: ExifDto? = null,
        tags: List<TagDto>? = null,
        urls: UrlsDto = urls(),
    ) = PhotoDetailDto(
        id = "p1",
        description = null,
        altDescription = "alt",
        width = 400,
        height = 600,
        likes = 7,
        views = 1,
        downloads = 2,
        urls = urls,
        user = UserDto(null, null, ProfileImageDto("profile")),
        exif = exif,
        location = location,
        tags = tags,
    )


    @Test
    fun `urls 가 없는 항목은 건너뛴다`() {
        assertNull(photoDto().copy(urls = null).toEntityOrNull(0))
    }

    @Test
    fun `id 가 없는 항목은 건너뛴다`() {
        assertNull(photoDto().copy(id = null).toEntityOrNull(0))
    }

    @Test
    fun `user 가 없어도 항목을 버리지 않는다`() {
        val photo = photoDto().copy(user = null).toEntityOrNull(0)!!

        assertEquals("Unknown", photo.authorName)
        assertEquals("", photo.authorUsername)
        assertNull(photo.authorProfileImageUrl)
    }

    @Test
    fun `thumb 가 없으면 small 로 폴백한다`() {
        val photo = photoDto().copy(urls = urls(thumb = null)).toEntityOrNull(0)!!
        assertEquals("small", photo.thumbUrl)
    }

    @Test
    fun `thumb 와 small 이 없으면 regular 로 폴백한다`() {
        val photo = photoDto().copy(urls = urls(thumb = null, small = null)).toEntityOrNull(0)!!
        assertEquals("regular", photo.thumbUrl)
    }

    @Test
    fun `full 이 없으면 raw 로 폴백한다`() {
        val photo = photoDto().copy(urls = urls(full = null)).toEntityOrNull(0)!!
        assertEquals("raw", photo.fullUrl)
    }

    @Test
    fun `모든 url 이 없으면 빈 문자열이 된다`() {
        val empty = urls(raw = null, full = null, regular = null, small = null, thumb = null)
        val photo = photoDto().copy(urls = empty).toEntityOrNull(0)!!
        assertEquals("", photo.thumbUrl)
        assertEquals("", photo.fullUrl)
    }


    @Test
    fun `상세는 urls 가 없으면 도메인 에러로 실패한다`() {
        val error = assertThrows(PhotoError.Unexpected::class.java) {
            detailDto().copy(urls = null).toDomain(isFavorite = false)
        }
        assertTrue(error.cause is IllegalStateException)
    }

    @Test
    fun `상세는 id 가 없으면 도메인 에러로 실패한다`() {
        assertThrows(PhotoError.Unexpected::class.java) {
            detailDto().copy(id = null).toDomain(isFavorite = false)
        }
    }

    @Test
    fun `상세는 user 가 없어도 Unknown 으로 그린다`() {
        val detail = detailDto().copy(user = null).toDomain(isFavorite = false)
        assertEquals("Unknown", detail.photo.authorName)
        assertEquals("", detail.photo.authorUsername)
    }

    @Test
    fun `작성자 이름이 없으면 Unknown 으로 대체된다`() {
        val detail = detailDto().toDomain(isFavorite = false)
        assertEquals("Unknown", detail.photo.authorName)
        assertEquals("", detail.photo.authorUsername)
    }

    @Test
    fun `description 이 없으면 altDescription 을 쓴다`() {
        val detail = detailDto().toDomain(isFavorite = false)
        assertEquals("alt", detail.photo.description)
    }

    @Test
    fun `location 은 name 과 country 를 합친다`() {
        val detail = detailDto(location = LocationDto("Seoul", null, "Korea"))
            .toDomain(isFavorite = false)
        assertEquals("Seoul, Korea", detail.location)
    }

    @Test
    fun `name 이 없으면 city 를 쓴다`() {
        val detail = detailDto(location = LocationDto(null, "Busan", "Korea"))
            .toDomain(isFavorite = false)
        assertEquals("Busan, Korea", detail.location)
    }

    @Test
    fun `중복된 지명은 한 번만 표시한다`() {
        val detail = detailDto(location = LocationDto("Korea", null, "Korea"))
            .toDomain(isFavorite = false)
        assertEquals("Korea", detail.location)
    }

    @Test
    fun `location 필드가 전부 비면 null 이다`() {
        val detail = detailDto(location = LocationDto(null, null, null))
            .toDomain(isFavorite = false)
        assertNull(detail.location)
    }

    @Test
    fun `exif 는 make 와 model 을 합친다`() {
        val detail = detailDto(exif = ExifDto("Canon", "EOS R5")).toDomain(isFavorite = false)
        assertEquals("Canon EOS R5", detail.exifModel)
    }

    @Test
    fun `exif 가 전부 비면 null 이다`() {
        val detail = detailDto(exif = ExifDto(null, null)).toDomain(isFavorite = false)
        assertNull(detail.exifModel)
    }

    @Test
    fun `title 이 없는 태그는 걸러낸다`() {
        val detail = detailDto(tags = listOf(TagDto("nature"), TagDto(null), TagDto("sky")))
            .toDomain(isFavorite = false)
        assertEquals(listOf("nature", "sky"), detail.tags)
    }

    @Test
    fun `width 나 height 가 0 이면 기본 종횡비를 쓴다`() {
        val broken = photo().copy(width = 0, height = 0)
        assertEquals(0.75f, broken.aspectRatio, 0.001f)
    }
}