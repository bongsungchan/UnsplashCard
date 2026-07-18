package com.sungchanbong.unsplashcard.core.design_system

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sungchanbong.core.design_system.component.PhotoGridItem
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.domain.models.Photo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PhotoGridItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun photo(isLike: Boolean) = Photo(
        id = "p1",
        description = "설명",
        thumbUrl = "",
        fullUrl = "",
        width = 400,
        height = 600,
        authorName = "Alex Smith",
        authorUsername = "alex",
        authorProfileImageUrl = null,
        likes = 7,
        isLike = isLike,
    )

    @Test
    fun 좋아요_상태에_따라_접근성_라벨이_달라진다() {
        composeRule.setContent {
            UnsplashcardTheme() {
                PhotoGridItem(photo(isLike = false), onClick = {}, onClickLike = {})
            }
        }

        composeRule.onNodeWithContentDescription("좋아요").assertIsDisplayed()
    }

    @Test
    fun 좋아요된_사진은_취소_라벨을_보여준다() {
        composeRule.setContent {
            UnsplashcardTheme {
                PhotoGridItem(photo(isLike = true), onClick = {}, onClickLike = {})
            }
        }

        composeRule.onNodeWithContentDescription("좋아요 취소").assertIsDisplayed()
    }

    @Test
    fun 하트를_누르면_토글_콜백이_정확히_한번_불린다() {
        var toggleCount = 0
        composeRule.setContent {
            UnsplashcardTheme {
                PhotoGridItem(
                    photo = photo(isLike = false),
                    onClick = {},
                    onClickLike = { toggleCount++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("좋아요").performClick()

        assertEquals(1, toggleCount)
    }

    @Test
    fun 카드를_누르면_상세_콜백이_불린다() {
        var clicked = false
        composeRule.setContent {
            UnsplashcardTheme {
                PhotoGridItem(
                    photo = photo(isLike = false),
                    onClick = { clicked = true },
                    onClickLike = {},
                )
            }
        }

        composeRule.onNodeWithText("Alex Smith").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("설명").assertHasClickAction()
        composeRule.onNodeWithContentDescription("설명").performClick()

        assertEquals(true, clicked)
    }
}