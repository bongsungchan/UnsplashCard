package com.sungchanbong.feature.detail

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.core.design_system.component.ErrorContent
import com.sungchanbong.core.design_system.component.LikeButton
import com.sungchanbong.core.design_system.component.toMessage
import com.sungchanbong.feature.detail.ui.DetailContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    val downloadFailedMessage = stringResource(R.string.download_failed)
    val downloadStartedMessage = stringResource(R.string.download_started)
    val permissionDeniedMessage = stringResource(R.string.download_permission_denied)
    val snackbarScope = rememberCoroutineScope()

    fun showSnackbar(message: String) {
        snackbarScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(DetailScreenIntent.PermissionResult(granted))
    }

    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is DetailScreenEffect.NavigateBack -> {
                onNavigateBack()
            }

            is DetailScreenEffect.RequestStoragePermission ->
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            is DetailScreenEffect.DownloadStarted -> showSnackbar(downloadStartedMessage)
            is DetailScreenEffect.DownloadFailed -> showSnackbar(downloadFailedMessage)
            is DetailScreenEffect.DownloadPermissionDenied ->
                showSnackbar(permissionDeniedMessage)

        }
    }

    LaunchedEffect(state.message) {
        val messageRes = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(resources.getString(messageRes))
        viewModel.onIntent(DetailScreenIntent.MessageShown)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_photo_detail)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onIntent(DetailScreenIntent.BackClicked)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    state.detail?.let { detail ->
                        IconButton(
                            onClick = { viewModel.onIntent(DetailScreenIntent.DownloadClicked) },
                            enabled = !state.isDownloading && !detail.isStale,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = stringResource(R.string.action_download),
                            )
                        }
                        LikeButton(
                            onClick = {
                                viewModel.onIntent(DetailScreenIntent.TogglePhotoLike)
                            },
                            iconSize = 24,
                            isLike = detail.photo.isLike,
                            unfavoriteTint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val detail = state.detail
            val error = state.error
            when {
                detail != null -> DetailContent(
                    detail = detail,
                    onRetry = { viewModel.onIntent(DetailScreenIntent.Retry) },
                )

                state.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null -> ErrorContent(
                    message = error.toMessage(),
                    onRetry = { viewModel.onIntent(DetailScreenIntent.Retry) },
                )

                else -> ErrorContent(
                    message = stringResource(R.string.error_unexpected),
                    onRetry = { viewModel.onIntent(DetailScreenIntent.Retry) },
                )
            }
        }
    }
}