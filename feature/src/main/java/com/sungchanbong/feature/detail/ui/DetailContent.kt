package com.sungchanbong.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sungchanbong.core.R
import com.sungchanbong.domain.models.PhotoDetail

@Composable
fun DetailContent(detail: PhotoDetail, onRetry: () -> Unit) {
    val photo = detail.photo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val context = LocalContext.current
        val request = remember(photo.fullUrl, photo.thumbUrl) {
            ImageRequest.Builder(context)
                .data(photo.fullUrl)
                .placeholderMemoryCacheKey(photo.thumbUrl)
                .crossfade(true)
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = photo.description,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(photo.aspectRatio.coerceIn(MIN_RATIO, MAX_RATIO)),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            if (detail.isStale) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.detail_stale_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = photo.authorProfileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(photo.authorName, style = MaterialTheme.typography.titleMedium)
                    if (photo.authorUsername.isNotBlank()) {
                        Text(
                            text = "@${photo.authorUsername}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            photo.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MetaRow(
                    stringResource(R.string.detail_likes),
                    photo.likes.toString()
                )
                detail.views?.let {
                    MetaRow(
                        stringResource(R.string.detail_views),
                        it.toString()
                    )
                }
                detail.downloads?.let {
                    MetaRow(
                        stringResource(R.string.detail_downloads),
                        it.toString()
                    )
                }
                detail.location?.let {
                    MetaRow(
                        stringResource(R.string.detail_location),
                        it
                    )
                }
                detail.exifModel?.let {
                    MetaRow(
                        stringResource(R.string.detail_camera),
                        it
                    )
                }
                MetaRow(
                    stringResource(R.string.detail_dimensions),
                    stringResource(R.string.detail_dimensions_format, photo.width, photo.height),
                )
                if (detail.tags.isNotEmpty()) {
                    MetaRow(
                        stringResource(R.string.detail_tags),
                        detail.tags.take(MAX_TAGS).joinToString(", "),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(84.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private const val MAX_TAGS = 8
private const val MIN_RATIO = 0.5f
private const val MAX_RATIO = 1.5f