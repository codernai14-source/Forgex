package com.forgex.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.network.i18n.AppLanguageManager
import com.forgex.mobile.core.network.i18n.AppLanguageState
import com.forgex.mobile.core.datastore.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppShellViewModel @Inject constructor(
    sessionStore: SessionStore,
    appLanguageManager: AppLanguageManager
) : ViewModel() {

    val uiState = combine(
        sessionStore.systemName,
        appLanguageManager.observeUiState()
    ) { systemName, languageState ->
        AppShellUiState(
            systemName = systemName?.ifBlank { "FORGEX_MOM" } ?: "FORGEX_MOM",
            languageState = languageState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppShellUiState(
            systemName = "FORGEX_MOM",
            languageState = AppLanguageState(currentLanguageTag = AppLanguage.DEFAULT_LANGUAGE_TAG)
        )
    )
}

data class AppShellUiState(
    val systemName: String,
    val languageState: AppLanguageState
)
