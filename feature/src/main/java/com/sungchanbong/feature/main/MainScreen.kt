package com.sungchanbong.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.sungchanbong.feature.main.ui.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainScreenViewModel: MainScreenViewModel = hiltViewModel()
) {
    val photos = mainScreenViewModel.photos.collectAsLazyPagingItems()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Title") },
            )
        }
    ) { paddingValues ->
        PhotoGrid(
            photos = photos,
            onPhotoClick = {

            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )

    }
}

