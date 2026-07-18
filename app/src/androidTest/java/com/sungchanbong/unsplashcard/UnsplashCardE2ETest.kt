package com.sungchanbong.unsplashcard

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.unsplashcard.navigation.NavigationGraph
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UnsplashCardE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.setContent {
            UnsplashcardTheme { NavigationGraph() }
        }
    }

    @Test
    fun 좋아요한_사진이_좋아요_화면과_상세까지_이어진다() {
        composeRule.onNodeWithText("Unsplash Explorer").assertIsDisplayed()
        composeRule.onNodeWithText("작가0").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("좋아요")[0].performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription("좋아요 취소")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("좋아요 목록 열기").performClick()
        composeRule.onNodeWithText("좋아요한 사진").assertIsDisplayed()

        composeRule.onNodeWithText("작가0").assertIsDisplayed()

        composeRule.onNodeWithText("작가0").performClick()
        composeRule.onNodeWithText("사진 상세").assertIsDisplayed()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasContentDescription("좋아요 취소"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun 좋아요를_해제하면_좋아요_화면에서_사라진다() {
        composeRule.onAllNodesWithContentDescription("좋아요")[0].performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription("좋아요 취소")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("좋아요 목록 열기").performClick()
        composeRule.onNodeWithText("작가0").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("좋아요 취소")[0].performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("아직 좋아요한 사진이 없습니다.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}