package com.sungchanbong.core.architecture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update


interface UIState
interface UIIntent
interface UIEffect

abstract class BaseViewModel<S : UIState, I : UIIntent, E : UIEffect>(initialState: S) :
    ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.UNLIMITED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected fun reduce(reducer: S.() -> S) = _state.update(function = reducer)

    protected fun postSideEffect(effect: E) = _effect.trySend(effect)

    override fun onCleared() {
        _effect.close()
    }
}