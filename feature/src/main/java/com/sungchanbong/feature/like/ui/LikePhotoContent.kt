package com.sungchanbong.feature.like.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sungchanbong.core.R
import com.sungchanbong.core.design_system.component.CONTENT_TYPE_PHOTO
import com.sungchanbong.core.design_system.component.GRID_COLUMNS
import com.sungchanbong.core.design_system.component.PhotoGridItem
import com.sungchanbong.domain.models.Photo

@Composable
fun LikePhotoContents(
    photos: List<Photo>,
    isLoading: Boolean,
    isEmpty: Boolean,
    onPhotoClick: (String) -> Unit,
    onPhotoLikeClick: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {

        if (isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }
        if (isEmpty) {
            Text(
                text = stringResource(R.string.favorites_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = photos,
                key = { it.id },
                contentType = { CONTENT_TYPE_PHOTO },
            ) { photo ->
                PhotoGridItem(
                    photo = photo,
                    onClick = {
                        onPhotoClick(photo.id)
                    },
                    onClickLike = {
                        onPhotoLikeClick(photo)
                    }
                )
            }
        }
    }
}