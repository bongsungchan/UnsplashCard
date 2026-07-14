package com.sungchanbong.feature.like

import androidx.annotation.StringRes
import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoError

data class LikeScreenState(
    val isLoading: Boolean = true,
    val photos: List<Photo> = emptyList(),
    @StringRes val message: Int? = null,
    val error: PhotoError? = null,
) : UIState {
    val isEmpty: Boolean get() = !isLoading && error == null && photos.isEmpty()
}

sealed interface LikeScreenIntent : UIIntent {
    data class TogglePhotoLike(val photo: Photo) : LikeScreenIntent
    data class PhotoClicked(val photoId: String) : LikeScreenIntent
    data object BackClicked : LikeScreenIntent
    data object MessageShown : LikeScreenIntent
    data object RetryClicked : LikeScreenIntent
}

sealed interface LikeScreenEffect : UIEffect {
    data class NavigateToDetail(val photoId: String) : LikeScreenEffect
    data object NavigateBack : LikeScreenEffect
}