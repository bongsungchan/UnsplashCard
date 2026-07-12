package com.sungchanbong.feature.main

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.domain.models.Photo
import com.sungchanbong.domain.usecase.GetPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    getPhotosUseCase: GetPhotosUseCase
) :
    BaseViewModel<MainScreenState, MainScreenIntent, MainScreenEffect>(initialState = MainScreenState()) {

    val photos: Flow<PagingData<Photo>> = getPhotosUseCase().cachedIn(viewModelScope)
    override fun onIntent(intent: MainScreenIntent) {
    }

}