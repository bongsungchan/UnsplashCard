package com.sungchanbong.feature.detail

import com.sungchanbong.core.architecture.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor() :
    BaseViewModel<DetailScreenState, DetailScreenIntent, DetailScreenEffect>(initialState = DetailScreenState()) {
    override fun onIntent(intent: DetailScreenIntent) {
    }
}