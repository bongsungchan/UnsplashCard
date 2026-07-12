package com.sungchanbong.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.PhotoDetail
import com.sungchanbong.domain.usecase.GetPhotosUseCase
import com.sungchanbong.feature.PhotoDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val getPhotosUseCase: GetPhotosUseCase,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<DetailScreenState, DetailScreenIntent, DetailScreenEffect>(initialState = DetailScreenState()) {
    private val photoId: String = savedStateHandle.toRoute<PhotoDetailRoute>().photoId
    private val loadedDetail = MutableStateFlow<PhotoDetail?>(value = null)
    override fun onIntent(intent: DetailScreenIntent) {
    }

    init {
        viewModelScope.launch {
            getPhotosUseCase.getDetail(photoId)
                .onSuccess { detail ->
                    loadedDetail.value = detail
                    reduce {
                        copy(detail = detail)
                    }
                }
        }
    }
}