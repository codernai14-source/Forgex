package com.forgex.mobile.core.sync.coordinator

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.forgex.mobile.core.database.dao.SyncQueueDao
import com.forgex.mobile.core.database.entity.SyncQueueEntity
import com.forgex.mobile.core.database.entity.SyncStatus
import com.forgex.mobile.core.sync.model.HttpMethod
import com.forgex.mobile.core.sync.model.SyncOperationType
import com.forgex.mobile.core.sync.network.NetworkMonitor
import com.forgex.mobile.core.sync.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步协调器。
 *
 * 管理同步队列状态流转、Worker 调度与重试策略。
 * 是 SyncManager 的内部实现, 不直接对外暴露。
 *
 * 职责:
 * - enqueue: 将操作写入同步队列
 * - triggerImmediateSync: 即时触发同步 (OneTimeWorkRequest + NETWORK_CONNECTED)
 * - schedulePeriodicSync: 注册周期同步 (PeriodicWorkRequest 15min)
 * - observePendingCount: 响应式观察待处理记录数
 * - onNetworkRestored: 网络恢复时自动触发同步
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val workManager: WorkManager,
    private val networkMonitor: NetworkMonitor
) {

    companion object {
        /** 周期同步间隔 (分钟), WorkManager 最小周期为 15 分钟 */
        private const val PERIODIC_INTERVAL_MINUTES = 15L
    }

    /**
     * 将操作入队 (写入同步队列)。
     *
     * @param operation 操作类型
     * @param payload 请求体 JSON
     * @param entityId 关联业务实体 ID
     * @return 队列记录 ID
     */
    suspend fun enqueue(
        operation: SyncOperationType,
        payload: String,
        entityId: Long? = null
    ): Long {
        val entity = SyncQueueEntity(
            operationType = operation.name,
            method = operation.method.name,
            path = operation.path,
            payload = payload,
            entityId = entityId
        )
        val id = syncQueueDao.insert(entity)

        // 入队后立即尝试触发同步 (如果网络可用)
        triggerImmediateSync()

        return id
    }

    /**
     * 触发即时同步。
     * 使用 OneTimeWorkRequest + NETWORK_CONNECTED 约束, 替换已有同名任务。
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * 注册周期性同步任务。
     * 每 15 分钟执行一次, 仅在网络连接时触发。
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            PERIODIC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * 响应式观察待处理 (PENDING) 记录数。
     *
     * @return PENDING 记录数量 Flow
     */
    fun observePendingCount(): Flow<Int> {
        return syncQueueDao.observePendingCount()
    }

    /**
     * 网络恢复时的回调, 触发即时同步。
     * 由 SyncManager 在 NetworkMonitor 状态变化时调用。
     */
    fun onNetworkRestored() {
        triggerImmediateSync()
    }
}
