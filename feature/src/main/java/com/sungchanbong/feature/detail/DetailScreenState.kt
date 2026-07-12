package com.sungchanbong.feature.detail

import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState

data class DetailScreenState(
    val isLoading: Boolean = false
) : UIState

sealed interface DetailScreenIntent : UIIntent {

}

sealed interface DetailScreenEffect : UIEffect {

}