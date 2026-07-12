package com.sungchanbong.feature.like.ui

import android.print.PrintDocumentInfo.CONTENT_TYPE_PHOTO
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sungchanbong.core.design_system.component.PhotoGridItem
import com.sungchanbong.domain.models.Photo

@Composable
fun LikePhotoContents(
    photos: List<Photo>,
    onPhotoClick: (String) -> Unit,
    onPhotoLikeClick: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
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