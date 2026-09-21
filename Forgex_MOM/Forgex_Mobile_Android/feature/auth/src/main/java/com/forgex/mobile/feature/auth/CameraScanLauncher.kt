package com.forgex.mobile.feature.auth

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.forgex.mobile.core.device.FxCameraCapture
import com.forgex.mobile.core.device.FxCameraScanManager
import com.forgex.mobile.core.device.FxScanFeedback
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.ui.R

/**
 * 摄像头扫码启动器。
 *
 * 负责相机权限申请、调用系统相机拍摄、ML Kit 解码与临时文件清理，
 * 解码成功后通过回调交给调用方（通常投递到全局扫描总线统一路由）。
 *
 * @param cameraScanManager 摄像头扫码管理器
 * @param scanFeedback 扫码成功反馈
 * @param onDecoded 解码成功回调
 * @param onNotify 用户提示回调，入参为字符串资源 id
 * @return 可直接绑定到按钮的启动函数
 */
@Composable
fun rememberCameraScanLauncher(
    cameraScanManager: FxCameraScanManager,
    scanFeedback: FxScanFeedback,
    onDecoded: (FxScanResult) -> Unit,
    onNotify: (Int) -> Unit
): () -> Unit {
    var pendingCapture by remember { mutableStateOf<FxCameraCapture?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onNotify(R.string.scan_camera_unavailable)
        }
    }

    val captureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val capture = pendingCapture
        pendingCapture = null
        if (capture == null) {
            return@rememberLauncherForActivityResult
        }
        if (result.resultCode != Activity.RESULT_OK) {
            cameraScanManager.deleteCapture(capture)
            return@rememberLauncherForActivityResult
        }
        cameraScanManager.decode(
            capture = capture,
            onResult = { scanResult ->
                cameraScanManager.deleteCapture(capture)
                if (scanResult != null) {
                    scanFeedback.onScanSuccess(FxScanFeedback.HARDWARE_SCAN_CONFIG)
                    onDecoded(scanResult)
                } else {
                    onNotify(R.string.scan_camera_no_code)
                }
            },
            onError = {
                cameraScanManager.deleteCapture(capture)
                onNotify(R.string.scan_camera_failed)
            }
        )
    }

    return startScan@{
        if (!cameraScanManager.hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return@startScan
        }
        val capture = cameraScanManager.createCaptureRequest()
        if (capture == null) {
            onNotify(R.string.scan_camera_unavailable)
            return@startScan
        }
        pendingCapture = capture
        try {
            captureLauncher.launch(capture.intent)
        } catch (_: ActivityNotFoundException) {
            pendingCapture = null
            cameraScanManager.deleteCapture(capture)
            onNotify(R.string.scan_camera_unavailable)
        }
    }
}
