package com.forgex.mobile.core.device

import android.view.KeyEvent

/**
 * 硬件键盘扫码枪（键盘模拟模式）组帧器。
 *
 * 扫码枪以极快速度注入一串字符并以回车结束，普通人工输入达不到该速度。
 * 该类只做旁路观察，从不消费按键事件，由宿主 Activity 在 dispatchKeyEvent 中调用。
 *
 * 组帧规则：
 * - 可打印字符进入缓冲，首字符超过 [framingMillis] 视为新的一帧；
 * - 回车结束当前帧，长度达到 [minPayloadLength] 才提交；
 * - 其他非打印按键（方向、退格、音量等）视为打断，清空当前帧。
 *
 * @param onSubmit 帧提交回调，入参为完整扫码内容
 * @param framingMillis 一帧允许的最大跨度
 * @param minPayloadLength 提交所需的最小字符数
 * @param clock 时间源，便于单元测试
 */
class FxKeyboardScanDispatcher(
    private val onSubmit: (String) -> Unit,
    private val framingMillis: Long = DEFAULT_FRAMING_MILLIS,
    private val minPayloadLength: Int = DEFAULT_MIN_PAYLOAD_LENGTH,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val buffer = StringBuilder()
    private var frameStartMillis = 0L

    /**
     * 旁路观察一次按键事件。永不拦截，宿主继续正常分发。
     */
    fun offer(action: Int, keyCode: Int, unicodeChar: Int) {
        if (action != KeyEvent.ACTION_DOWN) {
            return
        }
        when {
            keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER -> flush()
            isPrintable(unicodeChar) -> {
                val now = clock()
                if (buffer.isEmpty()) {
                    frameStartMillis = now
                } else if (now - frameStartMillis > framingMillis) {
                    buffer.clear()
                    frameStartMillis = now
                }
                buffer.append(unicodeChar.toChar())
            }
            else -> reset()
        }
    }

    private fun flush() {
        val payload = buffer.toString().trim()
        reset()
        if (payload.length >= minPayloadLength) {
            onSubmit(payload)
        }
    }

    private fun reset() {
        buffer.clear()
        frameStartMillis = 0L
    }

    private fun isPrintable(unicodeChar: Int): Boolean {
        if (unicodeChar < 0x20 || unicodeChar == 0x7F) {
            return false
        }
        return Character.isLetterOrDigit(unicodeChar.toChar()) ||
            Character.isWhitespace(unicodeChar.toChar()) ||
            "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".any { it.code == unicodeChar }
    }

    companion object {
        const val DEFAULT_FRAMING_MILLIS = 1200L
        const val DEFAULT_MIN_PAYLOAD_LENGTH = 2
    }
}
