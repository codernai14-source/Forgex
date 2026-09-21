package com.forgex.mobile.core.device

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import com.forgex.mobile.core.model.FxScannerConfig

/**
 * 扫码成功反馈能力。
 */
class FxScanFeedback(private val context: Context) {

    fun onScanSuccess(config: FxScannerConfig) {
        if (config.beepOnSuccess) {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                .startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        }
        if (config.vibrateOnSuccess) {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    companion object {
        /** 硬件扫码（PDA/NFC/键盘/摄像头）成功后的默认反馈配置：短提示音 + 短震动。 */
        val HARDWARE_SCAN_CONFIG = FxScannerConfig(
            beepOnSuccess = true,
            vibrateOnSuccess = true
        )
    }
}
