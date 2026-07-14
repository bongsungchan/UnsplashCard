package com.sungchanbong.core.design_system.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sungchanbong.core.R
import com.sungchanbong.core.design_system.preview.ThemePreviews
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.domain.models.PhotoError

@Composable
fun PhotoError.toMessage(): String = when (this) {
    PhotoError.Network -> stringResource(R.string.error_network)
    PhotoError.RateLimited -> stringResource(R.string.error_rate_limited)
    PhotoError.Unauthorized -> stringResource(R.string.error_unauthorized)
    PhotoError.NotFound -> stringResource(R.string.error_not_found)
    is PhotoError.Unexpected -> stringResource(R.string.error_unexpected)
}

@Composable
fun Throwable.toUserMessage(): String =
    (this as? PhotoError)?.toMessage() ?: stringResource(R.string.error_unexpected)

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@ThemePreviews
@Composable
private fun ErrorContentPreview() {
    UnsplashcardTheme() {
        ErrorContent(
            message = stringResource(
                R.string.error_network
            ), onRetry = {})
    }
}