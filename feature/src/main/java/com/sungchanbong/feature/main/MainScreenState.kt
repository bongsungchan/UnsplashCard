package com.sungchanbong.feature.main

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState

data class MainScreenState(
    val isLoading: Boolean = false
) : UIState

sealed interface MainScreenIntent : UIIntent {

}

sealed interface MainScreenEffect : UIEffect {

}