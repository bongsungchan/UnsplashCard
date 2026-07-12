package com.sungchanbong.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
        )

        Column(modifier = Modifier.padding(16.dp)) {
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
                MetaRow("좋아요", photo.likes.toString())
                detail.views?.let { MetaRow("조회", it.toString()) }
                detail.downloads?.let {
                    MetaRow("다운로드", it.toString())
                }
                detail.location?.let { MetaRow("위치", it) }
                detail.exifModel?.let { MetaRow("카메라", it) }
                if (detail.tags.isNotEmpty()) {
                    MetaRow(
                        "태그",
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