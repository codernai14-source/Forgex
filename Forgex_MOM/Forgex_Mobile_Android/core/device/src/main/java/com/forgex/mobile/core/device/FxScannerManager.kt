package com.forgex.mobile.core.device

import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScannerConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 扫码结果统一分发管理器。
 */
class FxScannerManager(
    private val debouncer: FxScanDebouncer = FxScanDebouncer()
) {
    private val _results = MutableSharedFlow<FxScanResult>(
        replay = 1,
        extraBufferCapacity = 16
    )
    val results: SharedFlow<FxScanResult> = _results.asSharedFlow()

    fun submit(result: FxScanResult, config: FxScannerConfig = FxScannerConfig()) {
        val shouldEmit = if (config.allowContinuousScan) {
            debouncer.shouldEmit(result.rawValue, result.timestamp, config.debounceMillis)
        } else {
            debouncer.shouldEmit(result.rawValue, result.timestamp, Long.MAX_VALUE)
        }
        if (shouldEmit) {
            _results.tryEmit(result)
        }
    }

    /**
     * 清空最近一次缓存的扫描结果，避免页面重建后重复消费。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearCachedResult() {
        _results.resetReplayCache()
    }
}
