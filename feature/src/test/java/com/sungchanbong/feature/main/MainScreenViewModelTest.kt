package com.sungchanbong.feature.main

import androidx.paging.PagingData
import app.cash.turbine.test
import com.sungchanbong.core.R
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.GetPhotosUseCase
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import com.sungchanbong.feature.testutil.MainDispatcherRule
import com.sungchanbong.feature.testutil.photo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPhotosUseCase: GetPhotosUseCase = mockk()
    private val photoLikeUseCase: PhotoLikeUseCase = mockk()

    private fun viewModel() = MainScreenViewModel(getPhotosUseCase, photoLikeUseCase)


    @Test
    fun `사진 클릭은 상세로 이동하는 이펙트를 낸다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())

        val viewModel = viewModel()
        viewModel.onIntent(MainScreenIntent.PhotoClicked("p1"))

        viewModel.effect.test {
            assertEquals(MainScreenEffect.NavigateToDetail("p1"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `좋아요 목록 진입 이펙트를 낸다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())

        val viewModel = viewModel()
        viewModel.onIntent(MainScreenIntent.PhotoLikeClicked)

        viewModel.effect.test {
            assertEquals(MainScreenEffect.NavigateToPhotoLike, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `좋아요 토글은 정확한 사진을 UseCase 에 넘긴다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())
        coEvery { photoLikeUseCase.onToggle(any()) } returns Result.success(Unit)

        val viewModel = viewModel()
        viewModel.onIntent(MainScreenIntent.TogglePhotoLike(photo("p1")))
        advanceUntilIdle()

        coVerify(exactly = 1) { photoLikeUseCase.onToggle(photo("p1")) }
    }

    @Test
    fun `좋아요 저장이 실패하면 사용자에게 알린다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())
        coEvery { photoLikeUseCase.onToggle(any()) } returns Result.failure(
            PhotoError.Unexpected(
                null
            )
        )

        val viewModel = viewModel()
        viewModel.onIntent(MainScreenIntent.TogglePhotoLike(photo("p1")))
        advanceUntilIdle()

        assertEquals(R.string.favorite_save_failed, viewModel.state.value.message)
    }

    @Test
    fun `메시지를 보여준 뒤에는 상태에서 지운다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())
        coEvery { photoLikeUseCase.onToggle(any()) } returns Result.failure(
            PhotoError.Unexpected(
                null
            )
        )

        val viewModel = viewModel()
        viewModel.onIntent(MainScreenIntent.TogglePhotoLike(photo("p1")))
        advanceUntilIdle()

        viewModel.onIntent(MainScreenIntent.MessageShown)

        assertNull(viewModel.state.value.message)
    }

    @Test
    fun `재시도는 Intent 로 들어와 Paging 재시도 이펙트를 낸다`() = runTest {
        every { getPhotosUseCase() } returns flowOf(PagingData.empty())

        val viewModel = viewModel()
        viewModel.effect.test {
            viewModel.onIntent(MainScreenIntent.RetryClicked)
            assertEquals(MainScreenEffect.RetryPaging, awaitItem())
        }
    }
}
