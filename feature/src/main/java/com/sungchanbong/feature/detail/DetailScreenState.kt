package com.sungchanbong.feature.detail

import androidx.annotation.StringRes
import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError

data class DetailScreenState(
    val isLoading: Boolean = true,
    val detail: PhotoDetail? = null,
    val error: PhotoError? = null,
    val isDownloading: Boolean = false,
    @StringRes val message: Int? = null,
) : UIState

sealed interface DetailScreenIntent : UIIntent {
    data object BackClicked : DetailScreenIntent
    data object TogglePhotoLike : DetailScreenIntent
    data object Retry : DetailScreenIntent
    data object DownloadClicked : DetailScreenIntent
    data class PermissionResult(val granted: Boolean) : DetailScreenIntent
    data object MessageShown : DetailScreenIntent


}

sealed interface DetailScreenEffect : UIEffect {
    data object NavigateBack : DetailScreenEffect
    data object RequestStoragePermission : DetailScreenEffect
    data object DownloadStarted : DetailScreenEffect
    data object DownloadFailed : DetailScreenEffect
    data object DownloadPermissionDenied : DetailScreenEffect
}