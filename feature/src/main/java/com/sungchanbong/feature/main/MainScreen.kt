package com.sungchanbong.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.feature.main.ui.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPhotoLike: () -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val photos = viewModel.photos.collectAsLazyPagingItems()
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

