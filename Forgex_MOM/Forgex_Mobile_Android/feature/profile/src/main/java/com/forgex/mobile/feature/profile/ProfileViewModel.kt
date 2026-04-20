package com.forgex.mobile.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.i18n.LanguageMode
import com.forgex.mobile.core.network.i18n.AppLanguageManager
import com.forgex.mobile.core.network.model.i18n.LanguageType
import com.forgex.mobile.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appLanguageManager: AppLanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            appLanguageManager.observeUiState().collect { state ->
                _uiState.update {
                    it.copy(
                        mode = state.mode,
                        currentLanguageTag = state.currentLanguageTag,
                        languages = state.availableLanguages
                    )
                }
            }
        }
    }

    fun followSystem() {
        viewModelScope.launch {
            appLanguageManager.followSystem()
            _uiState.update { it.copy(noticeMessageRes = R.string.profile_language_changed) }
        }
    }

    fun selectLanguage(languageTag: String) {
        viewModelScope.launch {
            appLanguageManager.selectLanguage(languageTag)
            _uiState.update { it.copy(noticeMessageRes = R.string.profile_language_changed) }
        }
    }
}

data class ProfileUiState(
    val mode: LanguageMode = LanguageMode.FOLLOW_SYSTEM,
    val currentLanguageTag: String = "zh-CN",
    val languages: List<LanguageType> = emptyList(),
    val noticeMessageRes: Int? = null
)
