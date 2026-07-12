package com.sungchanbong.feature.like

import androidx.lifecycle.viewModelScope
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.usecase.PhotoLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikeScreenViewModel @Inject constructor(
    private val photoLikeUseCase: PhotoLikeUseCase
) :
    BaseViewModel<LikeScreenState, LikeScreenIntent, LikeScreenEffect>(initialState = LikeScreenState()) {
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
        }
    }

    init {
        viewModelScope.launch {
            photoLikeUseCase.getLikedPhoto().onEach { photos ->
                reduce { copy(photos = photos) }
            }.launchIn(viewModelScope)
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