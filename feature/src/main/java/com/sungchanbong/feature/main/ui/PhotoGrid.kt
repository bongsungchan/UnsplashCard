package com.sungchanbong.feature.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.sungchanbong.core.R
import com.sungchanbong.core.design_system.component.CONTENT_TYPE_PHOTO
import com.sungchanbong.core.design_system.component.EmptyContent
import com.sungchanbong.core.design_system.component.ErrorContent
import com.sungchanbong.core.design_system.component.GRID_COLUMNS
import com.sungchanbong.core.design_system.component.GRID_ITEM_ASPECT_RATIO
import com.sungchanbong.core.design_system.component.PhotoGridItem
import com.sungchanbong.core.design_system.component.toUserMessage
import com.sungchanbong.domain.models.Photo

@Composable
internal fun PhotoGrid(
    photos: LazyPagingItems<Photo>,
    onPhotoClick: (String) -> Unit,
    onPhotoLikeClick: (Photo) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(modifier = modifier) {
        val isEmpty = photos.itemCount == 0
        when (val refresh = photos.loadState.refresh) {
            is LoadState.Loading -> if (isEmpty) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }

            is LoadState.Error -> if (isEmpty) {
                ErrorContent(
                    message = refresh.error.toUserMessage(),
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                return@Box
            }

            is LoadState.NotLoading -> if (isEmpty) {
                EmptyContent(
                    title = stringResource(R.string.photos_empty_title),
                    description = stringResource(R.string.photos_empty_description),
                    modifier = Modifier.align(Alignment.Center),
                )
                return@Box
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                count = photos.itemCount,
                key = photos.itemKey { it.id },
                contentType = { CONTENT_TYPE_PHOTO },
            ) { index ->
                val photo = photos[index]
                if (photo != null) {
                    PhotoGridItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo.id) },
                        onClickLike = { onPhotoLikeClick(photo) },
                    )
                } else {

                    PhotoGridPlaceholder()
                }
            }

            // 다음 페이지 로딩 중.
            if (photos.loadState.append is LoadState.Loading) {
                item(span = { fullSpan() }, contentType = { CONTENT_TYPE_FOOTER }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            val append = photos.loadState.append
            if (append is LoadState.Error) {
                item(span = { fullSpan() }, contentType = { CONTENT_TYPE_FOOTER }) {
                    ErrorContent(
                        message = append.error.toUserMessage(),
                        onRetry = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

private fun LazyGridItemSpanScope.fullSpan(): GridItemSpan = GridItemSpan(maxLineSpan)

private const val CONTENT_TYPE_FOOTER = "footer"

@Composable
fun PhotoGridPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GRID_ITEM_ASPECT_RATIO)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}