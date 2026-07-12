package com.sungchanbong.feature.like

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo

data class LikeScreenState(
    val isLoading: Boolean = true,
    val photos: List<Photo> = emptyList(),
) : UIState {
    val isEmpty: Boolean get() = !isLoading && photos.isEmpty()
}

sealed interface LikeScreenIntent : UIIntent {
    data class TogglePhotoLike(val photo: Photo) : LikeScreenIntent
    data class PhotoClicked(val photoId: String) : LikeScreenIntent
    data object BackClicked : LikeScreenIntent
}

sealed interface LikeScreenEffect : UIEffect {
    data class NavigateToDetail(val photoId: String) : LikeScreenEffect
    data object NavigateBack : LikeScreenEffect
}