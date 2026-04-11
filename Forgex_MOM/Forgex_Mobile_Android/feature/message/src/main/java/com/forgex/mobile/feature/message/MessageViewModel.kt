package com.forgex.mobile.feature.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = messageRepository.loadMessages(entryMode = entryMode)) {
                is AppResult.Success -> {
                    loadedEntryMode = entryMode
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            messages = result.data
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
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
            when (messageRepository.markRead(messageId)) {
                is AppResult.Success -> {
                    load(entryMode, force = true)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = "标记已读失败，请稍后重试") }
                }

                AppResult.Loading -> {
                    // no-op
                }
            }
            _uiState.update { it.copy(readingIds = it.readingIds - messageId) }
        }
    }
}
