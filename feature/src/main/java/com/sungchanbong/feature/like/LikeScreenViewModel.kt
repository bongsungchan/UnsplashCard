package com.sungchanbong.feature.like

import androidx.lifecycle.viewModelScope
import com.sungchanbong.core.R
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.models.PhotoError
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LikeScreenViewModel @Inject constructor(
    private val photoLikeUseCase: PhotoLikeUseCase,
) :
    BaseViewModel<LikeScreenState, LikeScreenIntent, LikeScreenEffect>(initialState = LikeScreenState()) {

    private val prefetched = mutableSetOf<String>()
    private val retryTrigger = MutableStateFlow(0)
    override fun onIntent(intent: LikeScreenIntent) {
        when (intent) {
            is LikeScreenIntent.TogglePhotoLike -> {
                togglePhoto(intent.photo)
            }

            is LikeScreenIntent.PhotoClicked -> {
                postSideEffect(LikeScreenEffect.NavigateToDetail(intent.photoId))
            }

            is LikeScreenIntent.BackClicked -> {
                postSideEffect(LikeScreenEffect.NavigateBack)
            }

            is LikeScreenIntent.RetryClicked -> retry()
            is LikeScreenIntent.MessageShown -> {
                reduce {
                    copy(message = null)
                }
            }
        }
    }

    init {
        retryTrigger
            .flatMapLatest {
                photoLikeUseCase.getLikedPhoto()
                    .onEach { photos ->
                        reduce { copy(isLoading = false, error = null, photos = photos) }
                        val newcomers = photos.filter { prefetched.add(it.id) }
                        if (newcomers.isNotEmpty()) {
                            photoLikeUseCase.prefetchLikedPhoto(newcomers)
                        }
                    }
                    .catch { cause ->
                        reduce {
                            copy(
                                isLoading = false,
                                error = cause as? PhotoError ?: PhotoError.Unexpected(cause),
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }

    private fun retry() {
        reduce { copy(isLoading = true, error = null) }
        retryTrigger.update { it + 1 }
    }

    private fun togglePhoto(photo: Photo) {
        viewModelScope.launch {
            photoLikeUseCase.onToggle(photo).onFailure {
                reduce { copy(message = R.string.favorite_save_failed) }
            }
        }
    }

}