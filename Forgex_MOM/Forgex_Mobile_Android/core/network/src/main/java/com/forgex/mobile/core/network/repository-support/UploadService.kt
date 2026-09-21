package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.api.FileApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * 统一文件上传服务，走 sys/file/upload 协议并返回可访问 URL。
 */
class UploadService @Inject constructor(private val fileApi: FileApi) {
    suspend fun upload(file: File, moduleCode: String? = null, moduleName: String? = null): AppResult<String> {
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return AppResult.Error("文件不存在或为空")
        }
        return try {
            val mediaType = file.extension.ifBlank { "octet-stream" }.toMediaTypeOrNull()
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(mediaType))
            val moduleCodePart = moduleCode?.toRequestBody("text/plain".toMediaTypeOrNull())
            val moduleNamePart = moduleName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = fileApi.upload(part, moduleCodePart, moduleNamePart)
            if (response.isSuccess() && !response.data.isNullOrBlank()) {
                AppResult.Success(response.data!!)
            } else {
                AppResult.Error(response.errorMessage())
            }
        } catch (throwable: Throwable) {
            AppResult.Error(throwable.message ?: "文件上传失败")
        }
    }
}
