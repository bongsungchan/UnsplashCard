package com.sungchanbong.core.design_system.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LikeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 20,
    isLike: Boolean = false,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isLike) Icons.Filled.Favorite else Icons.Outlined.Favorite,
            contentDescription = null,
            tint = if (isLike) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview
@Composable
fun LikeButtonPreview() {
    LikeButton(onClick = {}, modifier = Modifier, isLike = false)
}