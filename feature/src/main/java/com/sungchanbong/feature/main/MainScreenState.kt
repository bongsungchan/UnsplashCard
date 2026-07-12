package com.sungchanbong.feature.main

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo

data class MainScreenState(
    val isLoading: Boolean = false
) : UIState

sealed interface MainScreenIntent : UIIntent {
    data class PhotoClicked(val photoId: String) : MainScreenIntent
    data class TogglePhotoLike(val photo: Photo) : MainScreenIntent
}

sealed interface MainScreenEffect : UIEffect {
    data class NavigateToDetail(val photoId: String) : MainScreenEffect
}