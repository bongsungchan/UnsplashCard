package com.sungchanbong.feature.like

import app.cash.turbine.test
import com.sungchanbong.core.R
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import com.sungchanbong.feature.testutil.MainDispatcherRule
import com.sungchanbong.feature.testutil.photo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LikeScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photoLikeUseCase: PhotoLikeUseCase = mockk(relaxed = true)

    private fun viewModel() =
        LikeScreenViewModel(photoLikeUseCase)

    @Test
    fun `좋아요 목록이 State 에 담긴다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns
                flowOf(listOf(photo("a", isLike = true), photo("b", isLike = true)))

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), viewModel.state.value.photos.map { it.id })
        assertFalse(viewModel.state.value.isEmpty)
    }

    @Test
    fun `로딩 중에는 비어있음으로 보지 않는다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns MutableStateFlow(emptyList())

        val viewModel = viewModel()

        assertFalse(viewModel.state.value.isEmpty)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `방출 후 목록이 비면 비어있음 상태가 된다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(emptyList())

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `좋아요 해제는 정확한 사진을 UseCase 에 넘긴다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(listOf(photo("a", isLike = true)))
        coEvery { photoLikeUseCase.onToggle(any()) } returns Result.success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(LikeScreenIntent.TogglePhotoLike(photo("a", isLike = true)))
        advanceUntilIdle()

        coVerify(exactly = 1) { photoLikeUseCase.onToggle(photo("a", isLike = true)) }
    }

    @Test
    fun `좋아요 저장 실패는 사용자에게 메시지로 알린다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(listOf(photo("a", isLike = true)))
        coEvery { photoLikeUseCase.onToggle(any()) } returns Result.failure(
            PhotoError.Unexpected(
                Exception()
            )
        )

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(LikeScreenIntent.TogglePhotoLike(photo("a", isLike = true)))
        advanceUntilIdle()

        assertEquals(R.string.favorite_save_failed, viewModel.state.value.message)

        viewModel.onIntent(LikeScreenIntent.MessageShown)
        assertEquals(null, viewModel.state.value.message)
    }

    @Test
    fun `좋아요 목록 진입 시 이미지 프리페치를 보충한다`() = runTest {
        val favorites = listOf(photo("a", isLike = true), photo("b", isLike = true))
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(favorites)

        viewModel()
        advanceUntilIdle()

        verify(exactly = 1) { photoLikeUseCase.prefetchLikedPhoto(favorites) }
    }

    @Test
    fun `사진 클릭은 상세 이동 이펙트를 낸다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(emptyList())

        val viewModel = viewModel()
        viewModel.onIntent(LikeScreenIntent.PhotoClicked("a"))

        viewModel.effect.test {
            assertEquals(LikeScreenEffect.NavigateToDetail("a"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `뒤로가기 이펙트를 낸다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flowOf(emptyList())

        val viewModel = viewModel()
        viewModel.onIntent(LikeScreenIntent.BackClicked)

        viewModel.effect.test {
            assertEquals(LikeScreenEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `좋아요 스트림이 실패해도 앱이 죽지 않고 사용자에게 알린다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flow { throw IllegalStateException("DB 손상") }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error is PhotoError.Unexpected)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `DB 실패는 빈 상태로 위장되지 않는다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returns flow { throw IllegalStateException("DB 손상") }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse("DB 실패를 '좋아요한 사진이 없음'으로 보여주면 안 된다", viewModel.state.value.isEmpty)
    }

    @Test
    fun `재시도하면 좋아요 스트림을 다시 구독한다`() = runTest {
        every { photoLikeUseCase.getLikedPhoto() } returnsMany listOf(
            flow { throw IllegalStateException("DB 손상") },
            flowOf(listOf(photo("a", isLike = true))),
        )

        val viewModel = viewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.error is PhotoError.Unexpected)

        viewModel.onIntent(LikeScreenIntent.RetryClicked)
        advanceUntilIdle()

        assertEquals(listOf("a"), viewModel.state.value.photos.map { it.id })
    }


    @Test
    fun `좋아요를 해제해도 남은 사진을 다시 프리페치하지 않는다`() = runTest {
        val p1 = photo("p1")
        val p2 = photo("p2")
        val likePhotoList = MutableStateFlow(listOf(p1, p2))
        every { photoLikeUseCase.getLikedPhoto() } returns likePhotoList

        viewModel()
        advanceUntilIdle()
        verify(exactly = 1) { photoLikeUseCase.prefetchLikedPhoto((listOf(p1, p2))) }

        likePhotoList.value = listOf(p1)
        advanceUntilIdle()

        verify(exactly = 0) { photoLikeUseCase.prefetchLikedPhoto((listOf(p1))) }
    }

    @Test
    fun `새로 추가된 사진만 프리페치한다`() = runTest {
        val p1 = photo("p1")
        val p2 = photo("p2")
        val likePhotoList = MutableStateFlow(listOf(p1))
        every { photoLikeUseCase.getLikedPhoto() } returns likePhotoList

        viewModel()
        advanceUntilIdle()

        likePhotoList.value = listOf(p2, p1)
        advanceUntilIdle()

        verify(exactly = 1) { photoLikeUseCase.prefetchLikedPhoto(listOf(p1)) }
        verify(exactly = 1) { photoLikeUseCase.prefetchLikedPhoto((listOf(p2))) }
    }
}