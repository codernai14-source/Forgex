package com.forgex.mobile.core.device

/**
 * 连续扫码去重工具。
 */
class FxScanDebouncer {
    private var lastValue: String? = null
    private var lastTimestamp: Long = 0L

    fun shouldEmit(value: String, now: Long, debounceMillis: Long): Boolean {
        val duplicated = value == lastValue && now - lastTimestamp < debounceMillis
        if (!duplicated) {
            lastValue = value
            lastTimestamp = now
        }
        return !duplicated
    }
}
