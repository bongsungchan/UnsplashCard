package com.sungchanbong.feature.main

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.sungchanbong.core.R
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.feature.main.ui.PhotoGrid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PhotoGridLoadStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun 갱신중이어도_보여줄_사진이_있으면_목록을_지우지_않는다() {
        setGrid(photos = listOf(photo("p1", "Alex Smith")), refresh = LoadState.Loading)

        composeRule.onNodeWithText("Alex Smith").assertIsDisplayed()
    }

    @Test
    fun 갱신실패해도_캐시가_있으면_전면_에러_대신_목록을_보여준다() {
        setGrid(
            photos = listOf(photo("p1", "Alex Smith")),
            refresh = LoadState.Error(PhotoError.Network),
        )

        composeRule.onNodeWithText("Alex Smith").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.error_network)).assertDoesNotExist()
    }

    @Test
    fun 보여줄_사진이_없을_때만_전면_에러를_그린다() {
        setGrid(photos = emptyList(), refresh = LoadState.Error(PhotoError.Network))

        composeRule.onNodeWithText(string(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).assertIsDisplayed()
    }

    @Test
    fun 로딩도_에러도_아닌데_사진이_없으면_빈_상태를_그린다() {
        setGrid(photos = emptyList(), refresh = LoadState.NotLoading(endOfPaginationReached = true))

        composeRule.onNodeWithText(string(R.string.photos_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.photos_empty_description)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).assertDoesNotExist()
    }

    @Test
    fun 다음_페이지_실패는_재시도_가능한_푸터로_알린다() {
        setGrid(
            photos = listOf(photo("p1", "Alex Smith")),
            append = LoadState.Error(PhotoError.Network),
        )


        composeRule.onNodeWithText(string(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.action_retry)).assertIsDisplayed()
        composeRule.onNodeWithText("Alex Smith").assertIsDisplayed()
    }

    @Test
    fun 사진을_누르면_해당_id_로_상세_콜백이_불린다() {
        val clicked = mutableListOf<String>()
        setGrid(photos = listOf(photo("p1", "Alex Smith")), onPhotoClick = { clicked += it })

        composeRule.onNodeWithText("Alex Smith").performClick()

        assertEquals(listOf("p1"), clicked)
    }

    private fun setGrid(
        photos: List<Photo>,
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        onPhotoClick: (String) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        val flow: Flow<PagingData<Photo>> = flowOf(
            PagingData.from(
                data = photos,
                sourceLoadStates = LoadStates(
                    refresh = refresh,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = append,
                ),
            ),
        )

        composeRule.setContent {
            UnsplashcardTheme {
                PhotoGrid(
                    photos = flow.collectAsLazyPagingItems(),
                    onPhotoClick = onPhotoClick,
                    onPhotoLikeClick = {},
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun `전면 에러의 재시도 버튼은 onRetry 를 부른다`() {
        var retried = 0
        setGrid(
            photos = emptyList(),
            refresh = LoadState.Error(PhotoError.Network),
            onRetry = { retried++ },
        )

        composeRule.onNodeWithText(string(R.string.action_retry)).performClick()

        assertEquals(1, retried)
    }

    private fun photo(id: String, authorName: String) = Photo(
        id = id,
        description = "설명",
        thumbUrl = "",
        fullUrl = "",
        width = 400,
        height = 600,
        authorName = authorName,
        authorUsername = "alex",
        authorProfileImageUrl = null,
        likes = 7,
        isLike = false,
    )

    private fun string(@StringRes id: Int): String =
        RuntimeEnvironment.getApplication().getString(id)
}
