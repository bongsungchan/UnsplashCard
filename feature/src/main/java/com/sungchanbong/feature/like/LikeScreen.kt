package com.sungchanbong.feature.like

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.feature.like.ui.LikePhotoContents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LikeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is LikeScreenEffect.NavigateBack -> {
                onNavigateBack()
            }

            is LikeScreenEffect.NavigateToDetail -> {
                onNavigateToDetail(effect.photoId)
            }
        }
    }

    val resources = LocalResources.current
    LaunchedEffect(state.message) {
        val messageRes = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(resources.getString(messageRes))
        viewModel.onIntent(LikeScreenIntent.MessageShown)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_favorites)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onIntent(LikeScreenIntent.BackClicked)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        LikePhotoContents(
            photos = state.photos,
            isLoading = state.isLoading,
            isEmpty = state.isEmpty,
            onPhotoLikeClick = {
                viewModel.onIntent(LikeScreenIntent.TogglePhotoLike(it))
            },
            onPhotoClick = {
                viewModel.onIntent(LikeScreenIntent.PhotoClicked(it))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}