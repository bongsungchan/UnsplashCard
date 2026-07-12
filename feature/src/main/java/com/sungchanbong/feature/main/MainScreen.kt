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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.feature.main.ui.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPhotoLike: () -> Unit,
    mainScreenViewModel: MainScreenViewModel = hiltViewModel()
) {
    val photos = mainScreenViewModel.photos.collectAsLazyPagingItems()
    mainScreenViewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is MainScreenEffect.NavigateToDetail -> {
                onNavigateToDetail(effect.photoId)
            }

            is MainScreenEffect.NavigateToPhotoLike -> {
                onNavigateToPhotoLike()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Title") },
                actions = {
                    IconButton(onClick = {
                        mainScreenViewModel.onIntent(intent = MainScreenIntent.PhotoLikeClicked)
                    }) {
                        Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        PhotoGrid(
            photos = photos,
            onPhotoClick = { photoId ->
                mainScreenViewModel.onIntent(intent = MainScreenIntent.PhotoClicked(photoId = photoId))
            },
            onPhotoLikeClick = {
                mainScreenViewModel.onIntent(intent = MainScreenIntent.TogglePhotoLike(it))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

    }
}

