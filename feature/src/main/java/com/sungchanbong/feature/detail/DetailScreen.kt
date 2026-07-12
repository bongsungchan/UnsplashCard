package com.sungchanbong.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sungchanbong.core.architecture.CollectAsEffect
import com.sungchanbong.core.design_system.component.LikeButton
import com.sungchanbong.feature.detail.ui.DetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effect.CollectAsEffect { effect ->
        when (effect) {
            is DetailScreenEffect.NavigateBack -> {
                onNavigateBack()
            }

        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onIntent(DetailScreenIntent.BackClicked)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    state.detail?.let { detail ->
                        LikeButton(onClick = {
                            viewModel.onIntent(DetailScreenIntent.TogglePhotoLike(detail.photo))
                        }, isLike = detail.photo.isLike)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val detail = state.detail
            if (detail != null) {
                DetailContent(detail = detail) {

                }
            }
        }
    }
}