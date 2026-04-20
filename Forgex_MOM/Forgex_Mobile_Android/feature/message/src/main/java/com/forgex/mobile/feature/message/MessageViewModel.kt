package com.forgex.mobile.feature.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.ui.R
import com.forgex.mobile.feature.message.data.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    private var loadedEntryMode: MessageEntryMode? = null

    fun load(entryMode: MessageEntryMode, force: Boolean = false) {
        if (!force && loadedEntryMode == entryMode && _uiState.value.messages.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, errorText = null) }

            when (val result = messageRepository.loadMessages(entryMode = entryMode)) {
                is AppResult.Success -> {
                    loadedEntryMode = entryMode
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            errorText = null,
                            messages = result.data
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            errorText = result.appText,
                            messages = emptyList()
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun markRead(messageId: Long, entryMode: MessageEntryMode) {
        if (messageId <= 0L || _uiState.value.readingIds.contains(messageId)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(readingIds = it.readingIds + messageId) }
            when (val result = messageRepository.markRead(messageId)) {
                is AppResult.Success -> {
                    load(entryMode, force = true)
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message,
                            errorText = result.appText ?: AppText.Resource(R.string.message_mark_read_failed)
                        )
                    }
                }

                AppResult.Loading -> Unit
            }
            _uiState.update { it.copy(readingIds = it.readingIds - messageId) }
        }
    }
}
