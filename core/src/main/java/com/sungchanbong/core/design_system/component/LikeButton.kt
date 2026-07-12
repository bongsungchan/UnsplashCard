package com.sungchanbong.core.design_system.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sungchanbong.core.R

@Composable
fun LikeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 20,
    isLike: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.primary,
    unfavoriteTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val description = stringResource(
        if (isLike) R.string.cd_favorite_remove else R.string.cd_favorite_add,
    )
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isLike) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = description,
            tint = if (isLike) tint else unfavoriteTint,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview
@Composable
fun LikeButtonPreview() {
    LikeButton(onClick = {}, modifier = Modifier, isLike = false)
}