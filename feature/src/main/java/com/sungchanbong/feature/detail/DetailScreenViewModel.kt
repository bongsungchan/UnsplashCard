package com.sungchanbong.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.GetPhotosUseCase
import com.sungchanbong.domain.usecase.PhotoDownloadUseCase
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import com.sungchanbong.feature.PhotoDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val getPhotosUseCase: GetPhotosUseCase,
    private val photoLikeUseCase: PhotoLikeUseCase,
    private val photoDownloadUseCase: PhotoDownloadUseCase,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<DetailScreenState, DetailScreenIntent, DetailScreenEffect>(initialState = DetailScreenState()) {
    private val photoId: String = savedStateHandle.toRoute<PhotoDetailRoute>().photoId
    private val loadedDetail = MutableStateFlow<PhotoDetail?>(value = null)
    override fun onIntent(intent: DetailScreenIntent) {
        when (intent) {
            is DetailScreenIntent.Retry -> load()
            is DetailScreenIntent.TogglePhotoLike -> togglePhoto()
            is DetailScreenIntent.DownloadClicked -> download()
            is DetailScreenIntent.BackClicked -> postSideEffect(DetailScreenEffect.NavigateBack)
            is DetailScreenIntent.MessageShown -> reduce { copy(message = null) }
            is DetailScreenIntent.PermissionResult -> onPermissionResult(intent.granted)
        }
    }

    init {
        load()
        combine(
            loadedDetail.filterNotNull(),
            photoLikeUseCase.observeIsPhotoLike(photoId).onStart {
                emit(false)
            }
        ) { detail, isLike ->
            detail.copy(photo = detail.photo.copy(isLike = isLike))
        }.distinctUntilChanged()
            .onEach { detail ->
                reduce {
                    copy(isLoading = false, detail = detail)
                }
            }.catch { e ->
                reduce {
                    copy(
                        isLoading = false,
                        error = e as? PhotoError ?: PhotoError.Unexpected(e),
                        message = R.string.error_unexpected
                    )
                }
            }.launchIn(viewModelScope)

    }

    private var loadJob: Job? = null

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            reduce { copy(isLoading = true, error = null) }
            getPhotosUseCase.getDetail(photoId)
                .onSuccess { detail ->
                    loadedDetail.value = detail
                }
                .onFailure { e ->
                    val error = e as? PhotoError ?: PhotoError.Unexpected(e)
                    reduce { copy(isLoading = false, error = error) }
                }
        }
    }

    private fun togglePhoto() {
        val photo = currentState.detail?.photo ?: return
        viewModelScope.launch {
            photoLikeUseCase.onToggle(photo).onFailure {
                reduce { copy(message = R.string.favorite_save_failed) }
            }
        }
    }

    private fun download() {
        if (currentState.isDownloading) return
        if (currentState.detail == null) return
        reduce { copy(isDownloading = true) }
        if (photoDownloadUseCase.checkPermission()) {
            postSideEffect(DetailScreenEffect.RequestStoragePermission)
        } else {
            save()
        }
    }

    private fun save() {
        val photo = currentState.detail?.photo ?: run {
            finishDownload()
            postSideEffect(DetailScreenEffect.DownloadFailed)
            return
        }
        viewModelScope.launch {
            photoDownloadUseCase.download(photo)
                .onSuccess { postSideEffect(DetailScreenEffect.DownloadStarted) }
                .onFailure { postSideEffect(DetailScreenEffect.DownloadFailed) }
            finishDownload()
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        if (granted) {
            save()
        } else {
            finishDownload()
            postSideEffect(DetailScreenEffect.DownloadPermissionDenied)
        }
    }

    private fun finishDownload() = reduce { copy(isDownloading = false) }
}