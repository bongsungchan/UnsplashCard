package com.sungchanbong.feature.main

import com.sungchanbong.core.architecture.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor() :
    BaseViewModel<MainScreenState, MainScreenIntent, MainScreenEffect>(initialState = MainScreenState()) {
    override fun onIntent(intent: MainScreenIntent) {
    }
}