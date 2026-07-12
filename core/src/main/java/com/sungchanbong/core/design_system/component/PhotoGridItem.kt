package com.sungchanbong.core.design_system.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private const val GRID_ITEM_ASPECT_RATIO = 0.7f
private val SCRIM_COLOR = Color(0x99000000)

@Composable
fun PhotoGridItem(
    photoUrl: String,
    name: String,
    isLike: Boolean = false,
    onClick: () -> Unit,
    onClickLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val request = remember(photoUrl) {
        ImageRequest.Builder(context)
            .data(photoUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GRID_ITEM_ASPECT_RATIO)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(SCRIM_COLOR)
                .padding(start = 8.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Text(
                text = name,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
            )
            LikeButton(
                isLike = isLike,
                onClick = onClickLike,
                iconSize = 18,
            )
        }
    }
}

@Preview
@Composable
fun PhotoGridPreview() {
    PhotoGridItem(
        photoUrl = "https://example.com/photo.jpg",
        name = "test",
        isLike = true,
        onClick = {},
        onClickLike = {}
    )
}