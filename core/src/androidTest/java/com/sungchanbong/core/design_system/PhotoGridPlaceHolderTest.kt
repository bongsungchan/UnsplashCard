package com.sungchanbong.unsplashcard.core.design_system

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.sungchanbong.core.design_system.component.PhotoGridItem
import com.sungchanbong.core.design_system.component.PhotoGridPlaceholder
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.domain.models.Photo
import org.junit.Rule
import org.junit.Test

class PhotoGridPlaceHolderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun photo() = Photo(
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
        isLike = false,
    )

    private companion object {
        val CELL_WIDTH = 180.dp
        const val TAG_ITEM = "grid_item"
        const val TAG_PLACEHOLDER = "grid_placeholder"
    }

    @Test
    fun placeholder_는_실제_셀과_같은_높이를_점유한다() {
        composeRule.setContent {
            UnsplashcardTheme {
                Row {
                    PhotoGridItem(
                        photo = photo(),
                        onClick = {},
                        onClickLike = {},
                        modifier = Modifier
                            .width(CELL_WIDTH)
                            .testTag(TAG_ITEM),
                    )
                    PhotoGridPlaceholder(
                        modifier = Modifier
                            .width(CELL_WIDTH)
                            .testTag(TAG_PLACEHOLDER),
                    )
                }
            }
        }

        val itemHeight = composeRule.onNodeWithTag(TAG_ITEM)
            .getUnclippedBoundsInRoot().height

        composeRule.onNodeWithTag(TAG_PLACEHOLDER).assertHeightIsEqualTo(itemHeight)
    }
}
