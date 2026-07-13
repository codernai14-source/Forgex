package com.forgex.mobile.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.forgex.mobile.core.database.dao.SyncQueueDao
import com.forgex.mobile.core.database.entity.SyncQueueEntity
import com.forgex.mobile.core.database.entity.SyncStatus
import com.forgex.mobile.core.sync.conflict.ConflictResolver
import com.forgex.mobile.core.sync.conflict.ConflictResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * 同步 Worker (HiltWorker + CoroutineWorker)。
 *
 * 从同步队列取出 PENDING 记录, 使用 OkHttpClient (含全部拦截器) 逐条重放,
 * 根据服务端响应通过 ConflictResolver 决定后续处理。
 *
 * 状态流转:
 * - PENDING -> SYNCING -> SYNCED (2xx/404) -> 删除
 * - PENDING -> SYNCING -> PENDING (5xx/超时, retryCount < maxRetry)
 * - PENDING -> SYNCING -> FAILED (409/400/401, 或 retryCount >= maxRetry)
 *
 * @HiltWorker 注入由 HiltWorkerFactory 自动处理, 需 ForgexApplication 实现 Configuration.Provider。
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit,
    private val conflictResolver: ConflictResolver
) : CoroutineWorker(appContext, params) {

    companion object {
        /** 单次同步最大处理记录数 */
        private const val BATCH_SIZE = 50

        /** UniqueWork 名称 (即时同步) */
        const val WORK_NAME_IMMEDIATE = "forgex_sync"

        /** UniqueWork 名称 (周期同步) */
        const val WORK_NAME_PERIODIC = "forgex_periodic_sync"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pendingItems = syncQueueDao.findPending(limit = BATCH_SIZE)

        if (pendingItems.isEmpty()) {
            return@withContext Result.success()
        }

        // 批量标记为 SYNCING
        val ids = pendingItems.map { it.id }
        syncQueueDao.markSyncing(ids)

        var hasRetry = false
        var hasFailure = false

        for (entity in pendingItems) {
            val success = executeOperation(entity)
            if (!success) {
                // 检查是否需要重试
                val updated = syncQueueDao.findById(entity.id)
                if (updated != null && updated.syncStatus == SyncStatus.PENDING.name) {
                    hasRetry = true
                } else if (updated != null && updated.syncStatus == SyncStatus.FAILED.name) {
                    hasFailure = true
                }
            }
        }

        // 清理已同步记录
        syncQueueDao.deleteSynced()

        when {
            hasRetry -> Result.retry()
            hasFailure -> Result.success() // 部分失败不阻断整体, 失败记录保留供人工处理
            else -> Result.success()
        }
    }

    /**
     * 执行单条同步操作。
     *
     * @param entity 同步队列记录
     * @return true 表示操作完成 (成功或失败), false 表示需要重试
     */
    private suspend fun executeOperation(entity: SyncQueueEntity): Boolean {
        val request = try {
            buildRequest(entity)
        } catch (e: Exception) {
            syncQueueDao.incrementRetry(entity.id, "构建请求失败: ${e.message}")
            return false
        }

        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            // 网络异常 -> RETRY
            syncQueueDao.incrementRetry(entity.id, "网络异常: ${e.message}")
            return false
        }

        response.use {
            val result = conflictResolver.resolve(entity, it)
            return handleResponse(entity, result, it.code)
        }
    }

    /**
     * 根据冲突解决结果处理记录状态。
     *
     * @param entity 同步队列记录
     * @param result 冲突解决结果
     * @param httpCode HTTP 状态码
     * @return true 表示操作完成, false 表示需要重试
     */
    private suspend fun handleResponse(
        entity: SyncQueueEntity,
        result: ConflictResult,
        httpCode: Int
    ): Boolean {
        when (result) {
            ConflictResult.ACCEPT_SERVER -> {
                syncQueueDao.updateStatus(entity.id, SyncStatus.SYNCED.name)
                return true
            }

            ConflictResult.SKIP -> {
                // 404 等场景, 目标已达成
                syncQueueDao.updateStatus(entity.id, SyncStatus.SYNCED.name)
                return true
            }

            ConflictResult.RETRY -> {
                syncQueueDao.incrementRetry(entity.id, "HTTP $httpCode")
                return false
            }

            ConflictResult.FAIL -> {
                syncQueueDao.markFailed(entity.id, "HTTP $httpCode")
                return true
            }
        }
    }

    /**
     * 根据同步队列记录构建 OkHttp Request。
     *
     * @param entity 同步队列记录
     * @return OkHttp Request
     */
    private fun buildRequest(entity: SyncQueueEntity): Request {
        val baseUrl = retrofit.baseUrl().toString()
        val fullUrl = "${baseUrl}${entity.path}"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val body = if (entity.payload.isNotEmpty()) {
            entity.payload.toRequestBody(mediaType)
        } else {
            null
        }

        val builder = Request.Builder()
            .url(fullUrl)

        when (entity.method.uppercase()) {
            "GET" -> builder.get()
            "POST" -> builder.post(body ?: "".toRequestBody(mediaType))
            "PUT" -> builder.put(body ?: "".toRequestBody(mediaType))
            "DELETE" -> {
                if (body != null) {
                    builder.delete(body)
                } else {
                    builder.delete()
                }
            }
        }

        return builder.build()
    }
}
