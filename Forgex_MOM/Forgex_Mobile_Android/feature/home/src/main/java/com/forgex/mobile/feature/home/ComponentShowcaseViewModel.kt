package com.forgex.mobile.feature.home

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.architecture.PagingUiState
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.component.FxDictOption
import com.forgex.mobile.core.model.FxPageData
import com.forgex.mobile.core.network.repository_support.DictRepository
import com.forgex.mobile.core.network.repository_support.UploadService
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 组件验收页状态。
 */
data class ComponentShowcaseUiState(
    val dictOptions: List<FxDictOption> = emptyList(),
    val dictLoading: Boolean = true,
    val dictFromRemote: Boolean = false,
    val dictFallback: Boolean = false
)

/**
 * 附件上传演示状态。
 */
sealed interface UploadDemoState {
    data object Idle : UploadDemoState
    data object Uploading : UploadDemoState
    data class Success(val url: String) : UploadDemoState
    data class Error(val message: String) : UploadDemoState
}

/**
 * 组件验收页 ViewModel：提供字典加载、分页模拟与附件上传演示数据。
 */
@HiltViewModel
class ComponentShowcaseViewModel @Inject constructor(
    private val dictRepository: DictRepository,
    private val uploadService: UploadService,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComponentShowcaseUiState())
    val uiState: StateFlow<ComponentShowcaseUiState> = _uiState.asStateFlow()

    private val _pagingState = MutableStateFlow(PagingUiState<String>())
    val pagingState: StateFlow<PagingUiState<String>> = _pagingState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadDemoState>(UploadDemoState.Idle)
    val uploadState: StateFlow<UploadDemoState> = _uploadState.asStateFlow()

    init {
        loadDict()
        refreshPaging()
    }

    /**
     * 拉取字典选项；接口不可用或返回空时回退到内置示例，保证验收页离线可用。
     */
    fun loadDict(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(dictLoading = true) }
            val remoteOptions = when (
                val result = dictRepository.items(SAMPLE_DICT_CODE, forceRefresh)
            ) {
                is AppResult.Success -> result.data
                    .filterNot { it.disabled }
                    .map { option -> FxDictOption(value = option.value, label = option.label) }
                is AppResult.Error -> emptyList()
                AppResult.Loading -> emptyList()
            }
            _uiState.update { state ->
                if (remoteOptions.isNotEmpty()) {
                    state.copy(
                        dictOptions = remoteOptions,
                        dictLoading = false,
                        dictFromRemote = true,
                        dictFallback = false
                    )
                } else {
                    state.copy(
                        dictOptions = fallbackDictOptions,
                        dictLoading = false,
                        dictFromRemote = false,
                        dictFallback = true
                    )
                }
            }
        }
    }

    /** 模拟首页加载。 */
    fun refreshPaging() {
        viewModelScope.launch {
            _pagingState.update { it.copy(isRefreshing = true, error = null) }
            delay(500)
            _pagingState.value = PagingUiState.fromPage(buildPage(1))
        }
    }

    /** 模拟加载下一页。 */
    fun loadMorePaging() {
        val current = _pagingState.value
        if (current.isLoadingMore || !current.hasMore) {
            return
        }
        viewModelScope.launch {
            _pagingState.update { it.copy(isLoadingMore = true) }
            delay(600)
            val nextPage = current.list.size / PAGE_SIZE + 1
            _pagingState.update { it.append(buildPage(nextPage)) }
        }
    }

    /**
     * 将选中图片复制到缓存后走统一上传协议，演示附件上传与结果回显。
     */
    fun uploadImage(uri: Uri) {
        if (_uploadState.value is UploadDemoState.Uploading) {
            return
        }
        viewModelScope.launch {
            _uploadState.value = UploadDemoState.Uploading
            val result = withContext(Dispatchers.IO) {
                val tempFile = runCatching {
                    File(appContext.cacheDir, "showcase_upload_${System.currentTimeMillis()}")
                        .also { file ->
                            appContext.contentResolver.openInputStream(uri)?.use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            } ?: error("无法读取所选文件")
                        }
                }.getOrElse { throwable ->
                    return@withContext AppResult.Error(throwable.message ?: "读取文件失败")
                }
                val uploadResult = uploadService.upload(tempFile, moduleCode = "component-showcase")
                tempFile.delete()
                uploadResult
            }
            _uploadState.value = when (result) {
                is AppResult.Success -> UploadDemoState.Success(result.data)
                is AppResult.Error -> UploadDemoState.Error(result.message)
                AppResult.Loading -> UploadDemoState.Idle
            }
        }
    }

    private fun buildPage(pageNum: Int): FxPageData<String> {
        val start = (pageNum - 1) * PAGE_SIZE + 1
        return FxPageData(
            records = (start until start + PAGE_SIZE).map { index -> "示例条目 %02d".format(index) },
            total = (PAGE_SIZE * TOTAL_PAGES).toLong(),
            size = PAGE_SIZE.toLong(),
            current = pageNum.toLong(),
            pages = TOTAL_PAGES.toLong()
        )
    }

    companion object {
        /** 演示用字典编码：后端内置的集成调用状态。 */
        const val SAMPLE_DICT_CODE = "callStatus"

        private const val PAGE_SIZE = 8
        private const val TOTAL_PAGES = 4

        private val fallbackDictOptions = listOf(
            FxDictOption(value = "PENDING", label = "待处理", color = Color(0xFFF59E0B)),
            FxDictOption(value = "RUNNING", label = "进行中", color = Color(0xFF3B82F6)),
            FxDictOption(value = "SUCCESS", label = "成功", color = Color(0xFF10B981)),
            FxDictOption(value = "FAILED", label = "失败", color = Color(0xFFEF4444)),
            FxDictOption(value = "CANCELLED", label = "已取消", enabled = false)
        )
    }
}
