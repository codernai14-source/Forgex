package com.forgex.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.datastore.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * 应用壳层状态：
 * 目前用于为全局 TopBar 提供动态系统名（来自系统基础配置）。
 */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    sessionStore: SessionStore
) : ViewModel() {

    val systemName = sessionStore.systemName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "FORGEX_MOM"
    )
}
