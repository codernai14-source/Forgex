package com.forgex.mobile.core.architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 页面状态标记接口。
 */
interface UiState

/**
 * 页面事件标记接口。
 */
interface UiEvent

/**
 * 统一 ViewModel 基类，负责状态与一次性事件分发。
 */
abstract class BaseViewModel<S : UiState, E : UiEvent>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val eventChannel = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = eventChannel.receiveAsFlow()

    protected fun updateState(reducer: (S) -> S) {
        _uiState.value = reducer(_uiState.value)
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
