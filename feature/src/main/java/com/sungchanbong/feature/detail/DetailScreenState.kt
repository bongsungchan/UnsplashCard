package com.sungchanbong.feature.detail

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError

data class DetailScreenState(
    val isLoading: Boolean = true,
    val detail: PhotoDetail? = null,
    val error: PhotoError? = null,
) : UIState

sealed interface DetailScreenIntent : UIIntent {
    data object BackClicked : DetailScreenIntent
    data class TogglePhotoLike(val photo: Photo) : DetailScreenIntent
    data object Retry : DetailScreenIntent
}

sealed interface DetailScreenEffect : UIEffect {
    data object NavigateBack : DetailScreenEffect
}