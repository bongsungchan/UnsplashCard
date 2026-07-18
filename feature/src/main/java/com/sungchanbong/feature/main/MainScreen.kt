package com.sungchanbong.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.core.design_system.preview.ThemePreviews
import com.sungchanbong.core.design_system.preview.previewPhotos
import com.sungchanbong.core.design_system.theme.UnsplashcardTheme
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.feature.main.ui.PhotoGrid
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPhotoLike: () -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is MainScreenEffect.NavigateToDetail -> {
                onNavigateToDetail(effect.photoId)
            }

            is MainScreenEffect.NavigateToPhotoLike -> {
                onNavigateToPhotoLike()
            }

            is MainScreenEffect.RetryPaging -> {
                photos.retry()
            }
        }
    }

    val resources = LocalResources.current
    LaunchedEffect(state.message) {
        val messageRes = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(resources.getString(messageRes))
        viewModel.onIntent(MainScreenIntent.MessageShown)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_explorer)) },
                actions = {
                    IconButton(onClick = {
                        viewModel.onIntent(intent = MainScreenIntent.PhotoLikeClicked)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.cd_open_favorites),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PhotoGrid(
            photos = photos,
            onPhotoClick = { photoId ->
                viewModel.onIntent(intent = MainScreenIntent.PhotoClicked(photoId = photoId))
            },
            onPhotoLikeClick = {
                viewModel.onIntent(intent = MainScreenIntent.TogglePhotoLike(it))
            },
            onRetry = {
                viewModel.onIntent(MainScreenIntent.RetryClicked)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

    }
}

@Composable
private fun previewGrid(
    photos: List<Photo>,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
): LazyPagingItems<Photo> = flowOf(
    PagingData.from(
        data = photos,
        sourceLoadStates = LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = append,
        ),
    ),
).collectAsLazyPagingItems()

@ThemePreviews
@Composable
private fun PhotoGridPreview() {
    UnsplashcardTheme {
        PhotoGrid(
            photos = previewGrid(photos = previewPhotos()),
            onPhotoClick = {},
            onPhotoLikeClick = {},
            onRetry = {})
    }
}

@ThemePreviews
@Composable
private fun PhotoGridEmptyPreview() {
    UnsplashcardTheme {
        PhotoGrid(
            photos = previewGrid(photos = emptyList()),
            onPhotoLikeClick = {},
            onPhotoClick = {},
            onRetry = {}
        )
    }
}

@ThemePreviews
@Composable
private fun PhotoGridErrorPreview() {
    UnsplashcardTheme {
        PhotoGrid(
            photos = previewGrid(emptyList(), refresh = LoadState.Error(PhotoError.Network)),
            onPhotoLikeClick = {},
            onPhotoClick = {},
            onRetry = {}
        )
    }
}

@ThemePreviews
@Composable
private fun PhotoGridAppendErrorPreview() {
    UnsplashcardTheme {
        PhotoGrid(
            photos = previewGrid(previewPhotos(2), append = LoadState.Error(PhotoError.Network)),
            onPhotoLikeClick = {},
            onPhotoClick = {},
            onRetry = {}
        )
    }
}
