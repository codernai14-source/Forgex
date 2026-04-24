package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.api.FileApi
import java.io.File

/**
 * 文件下载服务。
 */
class DownloadService(private val fileApi: FileApi) {

    suspend fun downloadTo(url: String, target: File): AppResult<File> {
        return try {
            val body = fileApi.download(url)
            target.outputStream().use { output ->
                body.byteStream().copyTo(output)
            }
            AppResult.Success(target)
        } catch (throwable: Throwable) {
            AppResult.Error(throwable.message ?: "文件下载失败")
        }
    }
}
