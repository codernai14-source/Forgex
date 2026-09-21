package com.forgex.mobile.core.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScanSource
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

/**
 * 基于系统相机拍照和 ML Kit 条码识别的扫码能力。
 *
 * 该类只负责创建拍照请求和解析拍摄结果，页面通过 ActivityResultLauncher 启动
 * [FxCameraCapture.intent]，成功返回后再调用 [decode]。
 */
class FxCameraScanManager(
    context: Context,
    private val scanner: BarcodeScanner = BarcodeScanning.getClient()
) {
    private val appContext = context.applicationContext

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 创建相机拍照请求。返回 null 表示系统相册无法创建输出 Uri。
     */
    fun createCaptureRequest(): FxCameraCapture? {
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "forgex_scan_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Forgex")
        }
        val outputUri = appContext.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return null
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return FxCameraCapture(intent = intent, outputUri = outputUri)
    }

    /**
     * 解析相机输出图片中的第一个条码。
     * 回调 null 表示图片中没有可用条码；失败时回调 [onError]。
     */
    fun decode(
        capture: FxCameraCapture,
        onResult: (FxScanResult?) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val image = runCatching {
            InputImage.fromFilePath(appContext, capture.outputUri)
        }.getOrElse {
            onError(it)
            return
        }
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull { barcode ->
                    !barcode.rawValue.isNullOrBlank() || !barcode.displayValue.isNullOrBlank()
                }
                val rawValue = barcode?.rawValue ?: barcode?.displayValue
                onResult(
                    rawValue?.trim()?.takeIf { it.isNotEmpty() }?.let { value ->
                        FxScanResult(
                            rawValue = value,
                            type = barcode?.format?.toString(),
                            source = FxScanSource.CAMERA,
                            deviceBrand = android.os.Build.BRAND,
                            metadata = buildMap {
                                barcode?.format?.toString()?.let { put("format", it) }
                                barcode?.displayValue?.takeIf { it != rawValue }?.let {
                                    put("displayValue", it)
                                }
                            }
                        )
                    }
                )
            }
            .addOnFailureListener(onError)
    }

    fun deleteCapture(capture: FxCameraCapture) {
        runCatching { appContext.contentResolver.delete(capture.outputUri, null, null) }
    }

    fun close() {
        scanner.close()
    }

    companion object {
        /** 返回第一个非空条码值，独立方法便于无设备单元测试。 */
        fun firstValue(values: Iterable<String?>): String? {
            return values.firstOrNull { !it.isNullOrBlank() }?.trim()?.takeIf { it.isNotEmpty() }
        }
    }
}

data class FxCameraCapture(
    val intent: Intent,
    val outputUri: Uri
)
