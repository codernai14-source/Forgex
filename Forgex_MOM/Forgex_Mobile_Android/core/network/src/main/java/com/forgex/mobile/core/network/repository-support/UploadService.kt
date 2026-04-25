package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.common.result.AppResult
import java.io.File

/**
 * 文件上传服务占位，首版仅收口协议。
 */
class UploadService {
    suspend fun upload(file: File): AppResult<String> {
        return AppResult.Error("上传服务待接入 /sys/file")
    }
}
