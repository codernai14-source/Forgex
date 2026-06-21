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

/**
 * 消息 ViewModel (离线增强版)。
 *
 * 适配离线 Repository:
 * - load: 在线刷新/离线缓存均返回数据, UI 标注离线模式
 * - markRead: 乐观更新 UI (立即标记已读), 离线操作入队 SyncManager 自动同步
 */
@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    private var loadedEntryMode: MessageEntryMode? = null

    /**
     * 加载消息列表。
     *
     * @param entryMode 入口模式
     * @param force 是否强制刷新
     */
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
                            messages = result.data,
                            isFromCache = false
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            errorText = result.appText,
                            messages = emptyList(),
                            isFromCache = false
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * 标记消息已读 (乐观更新)。
     *
     * 离线时 Repository 先更新本地缓存并入队 SyncManager, 始终返回 Success。
     * UI 立即显示"已读", 用户无感知离线。
     *
     * @param messageId 消息 ID
     * @param entryMode 当前入口模式 (用于刷新列表)
     */
    fun markRead(messageId: Long, entryMode: MessageEntryMode) {
        if (messageId <= 0L || _uiState.value.readingIds.contains(messageId)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(readingIds = it.readingIds + messageId) }

            // 乐观更新: 立即在 UI 中标记已读
            _uiState.update { uiState ->
                val updatedMessages = uiState.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(status = 1)
                    } else {
                        msg
                    }
                }
                uiState.copy(messages = updatedMessages)
            }

            when (val result = messageRepository.markRead(messageId)) {
                is AppResult.Success -> {
                    // 乐观更新成功, 无需强制刷新 (本地缓存已更新)
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
