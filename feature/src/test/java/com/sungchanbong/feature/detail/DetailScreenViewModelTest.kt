package com.sungchanbong.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sungchanbong.core.R
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.GetPhotosUseCase
import com.sungchanbong.domain.usecase.PhotoDownloadUseCase
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import com.sungchanbong.feature.testutil.MainDispatcherRule
import com.sungchanbong.feature.testutil.photo
import com.sungchanbong.feature.testutil.photoDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class DetailScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPhotosUseCase: GetPhotosUseCase = mockk()
    private val photoLikeUseCase: PhotoLikeUseCase = mockk(relaxed = true)
    private val photoDownloadUseCase: PhotoDownloadUseCase = mockk()

    private fun viewModel() = DetailScreenViewModel(
        getPhotosUseCase = getPhotosUseCase,
        photoLikeUseCase = photoLikeUseCase,
        photoDownloadUseCase = photoDownloadUseCase,
        savedStateHandle = SavedStateHandle(mapOf("photoId" to "p1")),
    )

    @Test
    fun `로드 성공 시 상세가 State 에 담긴다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("p1", state.detail?.photo?.id)
        assertNull(state.error)
    }

    @Test
    fun `로드 실패 시 도메인 에러가 State 에 담긴다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.failure(PhotoError.RateLimited)
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(PhotoError.RateLimited, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `DB 의 좋아요 변화가 상세 하트에 반영된다`() = runTest {
        val favoriteFlow = MutableStateFlow(false)
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail(isLike = false))
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns favoriteFlow

        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.detail!!.photo.isLike)

        favoriteFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.state.value.detail!!.photo.isLike)
    }

    @Test
    fun `상세 로드 전에 도착한 좋아요 상태도 유실되지 않는다`() = runTest {
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(true)
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail(isLike = false))

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.detail!!.photo.isLike)
    }

    @Test
    fun `좋아요 토글은 정확한 사진을 UseCase 에 넘긴다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.TogglePhotoLike)
        advanceUntilIdle()

        coVerify(exactly = 1) { photoLikeUseCase.onToggle(photo("p1")) }
    }

    @Test
    fun `좋아요 저장 실패는 사용자에게 메시지로 알린다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        coEvery { photoLikeUseCase.onToggle(any()) } returns
                Result.failure(PhotoError.Unexpected(Exception()))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.TogglePhotoLike)
        advanceUntilIdle()

        assertEquals(R.string.favorite_save_failed, viewModel.state.value.message)

        viewModel.onIntent(DetailScreenIntent.MessageShown)
        assertNull(viewModel.state.value.message)
    }

    @Test
    fun `재시도 중에도 기존 상세는 유지된다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(DetailScreenIntent.Retry)
        assertEquals("p1", viewModel.state.value.detail?.photo?.id)
    }

    @Test
    fun `Retry 는 상세를 다시 불러온다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returnsMany listOf(
            Result.failure(PhotoError.Network),
            Result.success(photoDetail()),
        )
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(PhotoError.Network, viewModel.state.value.error)

        viewModel.onIntent(DetailScreenIntent.Retry)
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals("p1", viewModel.state.value.detail?.photo?.id)
    }

    @Test
    fun `권한이 필요 없으면 곧바로 저장한다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns false
        coEvery { photoDownloadUseCase.download(photo("p1")) } returns Result.success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        viewModel.effect.test {
            assertEquals(DetailScreenEffect.DownloadStarted, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { photoDownloadUseCase.download(photo("p1")) }
        assertFalse(viewModel.state.value.isDownloading)
    }

    @Test
    fun `권한이 필요하면 저장하지 않고 권한 요청 이펙트를 낸다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns true

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        viewModel.effect.test {
            assertEquals(DetailScreenEffect.RequestStoragePermission, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { photoDownloadUseCase.download(any()) }
    }

    @Test
    fun `권한 승인 후에 비로소 저장이 시작된다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns true
        coEvery { photoDownloadUseCase.download(photo("p1")) } returns Result.success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        viewModel.onIntent(DetailScreenIntent.PermissionResult(granted = true))
        advanceUntilIdle()

        coVerify(exactly = 1) { photoDownloadUseCase.download(photo("p1")) }
    }

    @Test
    fun `권한을 거부하면 저장도 집계도 일어나지 않는다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns true

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        viewModel.onIntent(DetailScreenIntent.PermissionResult(granted = false))
        advanceUntilIdle()

        viewModel.effect.test {
            assertEquals(DetailScreenEffect.RequestStoragePermission, awaitItem())
            assertEquals(DetailScreenEffect.DownloadPermissionDenied, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { photoDownloadUseCase.download(any()) }
    }

    @Test
    fun `저장이 실패하면 DownloadFailed 이펙트가 나온다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns false
        coEvery { photoDownloadUseCase.download(photo("p1")) } returns Result.failure(PhotoError.Network)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        viewModel.effect.test {
            assertEquals(DetailScreenEffect.DownloadFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.state.value.isDownloading)
    }

    @Test
    fun `뒤로가기는 NavigateBack 이펙트를 낸다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.BackClicked)

        viewModel.effect.test {
            assertEquals(DetailScreenEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `다운로드를 연타해도 저장은 한 번만 실행된다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns false
        coEvery { photoDownloadUseCase.download(any()) } returns Result.success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) { photoDownloadUseCase.download(any()) }
    }

    @Test
    fun `권한 요청 중에는 다운로드 진행 상태가 유지된다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns true
        coEvery { photoDownloadUseCase.download(any()) } returns Result.success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()

        assertTrue("권한 다이얼로그 중인데 버튼이 다시 활성화됐다", viewModel.state.value.isDownloading)
        coVerify(exactly = 0) { photoDownloadUseCase.download(any()) }

        viewModel.onIntent(DetailScreenIntent.PermissionResult(granted = true))
        advanceUntilIdle()

        coVerify(exactly = 1) { photoDownloadUseCase.download(photo("p1")) }
        assertFalse(viewModel.state.value.isDownloading)
    }

    @Test
    fun `권한이 거부되면 진행 상태가 풀린다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flowOf(false)
        every { photoDownloadUseCase.checkPermission() } returns true

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.DownloadClicked)
        advanceUntilIdle()
        viewModel.onIntent(DetailScreenIntent.PermissionResult(granted = false))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isDownloading)
        coVerify(exactly = 0) { photoDownloadUseCase.download(any()) }
    }

    @Test
    fun `좋아요 스트림이 실패해도 앱이 죽지 않고 사용자에게 알린다`() = runTest {
        coEvery { getPhotosUseCase.getDetail("p1") } returns Result.success(photoDetail())
        every { photoLikeUseCase.observeIsPhotoLike("p1") } returns flow {
            throw IllegalStateException(
                "DB 손상"
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(R.string.error_unexpected, viewModel.state.value.message)
        assertFalse(viewModel.state.value.isLoading)
    }
}