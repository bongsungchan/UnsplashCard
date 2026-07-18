package com.sungchanbong.unsplashcard.domain.usecase

import androidx.paging.PagingData
import com.sungchanbong.domain.image.ImagePrefetcher
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.repositories.PhotoRepository
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoLikeUseCaseTest {
    private val prefetcher = RecordingImagePrefetcher()

    @Test
    fun `좋아요를 켜면 오프라인 상세에 필요한 이미지들을 프리페치한다`() =
        runTest {
            val useCase = PhotoLikeUseCase(SucceedingRepository, prefetcher)

            useCase.onToggle(photo(isLike = false))

            assertTrue("full", prefetcher.requested.contains("https://img/full"))
            assertTrue("thumb", prefetcher.requested.contains("https://img/thumb"))
            assertTrue("profile", prefetcher.requested.contains("https://img/profile"))
        }

    @Test
    fun `좋아요를 끄면 프리페치하지 않는다`() =
        runTest {
            val useCase = PhotoLikeUseCase(SucceedingRepository, prefetcher)

            useCase.onToggle(photo(isLike = true))

            assertEquals(emptyList<String>(), prefetcher.requested)
        }

    @Test
    fun `저장이 실패하면 프리페치하지 않는다`() =
        runTest {
            val useCase = PhotoLikeUseCase(FailingRepository, prefetcher)

            val result = useCase.onToggle(photo(isLike = false))

            assertTrue(result.isFailure)
            assertEquals(emptyList<String>(), prefetcher.requested)
        }

    @Test
    fun `빈 URL 은 프리페치 대상에서 제외한다`() =
        runTest {
            val useCase = PhotoLikeUseCase(SucceedingRepository, prefetcher)

            useCase.onToggle(
                photo(isLike = false).copy(
                    authorProfileImageUrl = null,
                    thumbUrl = ""
                )
            )

            assertEquals(listOf("https://img/full"), prefetcher.requested)
        }

    private fun photo(isLike: Boolean) =
        Photo(
            id = "p1",
            description = null,
            thumbUrl = "https://img/thumb",
            fullUrl = "https://img/full",
            width = 100,
            height = 200,
            authorName = "Alex",
            authorUsername = "alex",
            authorProfileImageUrl = "https://img/profile",
            isLike = isLike,
        )

    private class RecordingImagePrefetcher : ImagePrefetcher {
        val requested = mutableListOf<String>()

        override fun prefetch(urls: List<String>) {
            requested += urls
        }
    }

    private object SucceedingRepository : FakePhotoRepository() {
        override suspend fun togglePhotoLike(photo: Photo): Result<Unit> = Result.success(Unit)
    }

    private object FailingRepository : FakePhotoRepository() {
        override suspend fun togglePhotoLike(photo: Photo): Result<Unit> =
            Result.failure(PhotoError.Unexpected(null))
    }

    private abstract class FakePhotoRepository : PhotoRepository {
        override fun getPhotos(): Flow<PagingData<Photo>> = flowOf(PagingData.empty())

        override suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail> =
            error("not used")

        override fun getLikedPhoto(): Flow<List<Photo>> = error("not used")

        override fun observeIsPhotoLike(photoId: String): Flow<Boolean> = error("not used")

        override suspend fun photoDownload(id: String): Result<String> = error("not used")
    }
}
