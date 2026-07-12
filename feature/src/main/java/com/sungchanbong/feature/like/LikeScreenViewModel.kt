package com.sungchanbong.feature.like

import com.sungchanbong.core.architecture.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LikeScreenViewModel @Inject constructor() :
    BaseViewModel<LikeScreenState, LikeScreenIntent, LikeScreenEffect>(initialState = LikeScreenState()) {
    override fun onIntent(intent: LikeScreenIntent) {
    }
}