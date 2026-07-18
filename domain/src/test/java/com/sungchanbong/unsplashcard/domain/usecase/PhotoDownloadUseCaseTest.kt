package com.sungchanbong.unsplashcard.domain.usecase

import androidx.paging.PagingData
import com.sungchanbong.domain.download.PhotoFileDownloader
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.repositories.PhotoRepository
import com.sungchanbong.domain.usecase.PhotoDownloadUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoDownloadUseCaseTest {
    private val downloader = RecordingFileDownloader()

    @Test
    fun `집계 엔드포인트가 준 URL 로 파일을 저장한다`() =
        runTest {
            val useCase = PhotoDownloadUseCase(SucceedingRepository, downloader)

            val result = useCase.download(photo())

            assertTrue(result.isSuccess)
            assertEquals("https://dl/p1", downloader.url)
            assertEquals("unsplash-p1.jpg", downloader.fileName)
        }

    @Test
    fun `집계 호출이 실패하면 파일을 저장하지 않는다`() =
        runTest {
            val useCase = PhotoDownloadUseCase(FailingRepository, downloader)

            val result = useCase.download(photo())

            assertTrue(result.isFailure)
            assertEquals(PhotoError.Network, result.exceptionOrNull())
            assertNull("집계도 안 됐는데 저장을 시도했다", downloader.url)
        }

    @Test
    fun `파일 저장이 실패하면 실패 Result 로 합쳐진다`() =
        runTest {
            val useCase = PhotoDownloadUseCase(SucceedingRepository, FailingFileDownloader)

            val result = useCase.download(photo())

            assertFalse(result.isSuccess)
        }

    private fun photo() =
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
            isLike = false,
        )

    private class RecordingFileDownloader : PhotoFileDownloader {
        var url: String? = null
        var fileName: String? = null

        override fun isPermissionRequired(): Boolean = false

        override suspend fun download(
            url: String,
            fileName: String,
        ): Result<Unit> {
            this.url = url
            this.fileName = fileName
            return Result.success(Unit)
        }
    }

    private object FailingFileDownloader : PhotoFileDownloader {
        override fun isPermissionRequired(): Boolean = false

        override suspend fun download(
            url: String,
            fileName: String,
        ): Result<Unit> = Result.failure(IllegalStateException("DownloadManager 없음"))
    }

    private object SucceedingRepository : FakePhotoRepository() {
        override suspend fun photoDownload(id: String): Result<String> =
            Result.success("https://dl/$id")
    }

    private object FailingRepository : FakePhotoRepository() {
        override suspend fun photoDownload(id: String): Result<String> =
            Result.failure(PhotoError.Network)
    }

    private abstract class FakePhotoRepository : PhotoRepository {
        override fun getPhotos(): Flow<PagingData<Photo>> = flowOf(PagingData.empty())

        override suspend fun getPhotoDetail(photoId: String): Result<PhotoDetail> =
            error("not used")

        override fun getLikedPhoto(): Flow<List<Photo>> = error("not used")

        override fun observeIsPhotoLike(photoId: String): Flow<Boolean> = error("not used")

        override suspend fun togglePhotoLike(photo: Photo): Result<Unit> = error("not used")
    }
}
