package com.sungchanbong.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.GetPhotosUseCase
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
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<DetailScreenState, DetailScreenIntent, DetailScreenEffect>(initialState = DetailScreenState()) {
    private val photoId: String = savedStateHandle.toRoute<PhotoDetailRoute>().photoId
    private val loadedDetail = MutableStateFlow<PhotoDetail?>(value = null)
    override fun onIntent(intent: DetailScreenIntent) {
        when (intent) {
            is DetailScreenIntent.BackClicked -> {
                postSideEffect(DetailScreenEffect.NavigateBack)
            }

            is DetailScreenIntent.TogglePhotoLike -> {
                togglePhoto(intent.photo)
            }

            is DetailScreenIntent.Retry -> {
                load()
            }
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
                        error = e as? PhotoError ?: PhotoError.Unexpected(e)
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

    private fun togglePhoto(photo: Photo) {
        viewModelScope.launch {
            photoLikeUseCase.onToggle(photo).onSuccess {

            }.onFailure {

            }
        }
    }
}