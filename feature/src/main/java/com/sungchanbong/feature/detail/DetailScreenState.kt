package com.sungchanbong.feature.detail

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail

data class DetailScreenState(
    val isLoading: Boolean = false,
    val detail: PhotoDetail? = null,
) : UIState

sealed interface DetailScreenIntent : UIIntent {
    data object BackClicked : DetailScreenIntent
    data class TogglePhotoLike(val photo: Photo) : DetailScreenIntent
}

sealed interface DetailScreenEffect : UIEffect {
    data object NavigateBack : DetailScreenEffect
}