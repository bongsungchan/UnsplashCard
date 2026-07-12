package com.sungchanbong.feature.main

import androidx.annotation.StringRes
import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.Photo

data class MainScreenState(
    @StringRes val message: Int? = null,
) : UIState

sealed interface MainScreenIntent : UIIntent {
    data class PhotoClicked(val photoId: String) : MainScreenIntent
    data class TogglePhotoLike(val photo: Photo) : MainScreenIntent
    data object PhotoLikeClicked : MainScreenIntent
    data object RetryClicked : MainScreenIntent
    data object MessageShown : MainScreenIntent
}

sealed interface MainScreenEffect : UIEffect {
    data class NavigateToDetail(val photoId: String) : MainScreenEffect
    data object NavigateToPhotoLike : MainScreenEffect
    data object RetryPaging : MainScreenEffect

}