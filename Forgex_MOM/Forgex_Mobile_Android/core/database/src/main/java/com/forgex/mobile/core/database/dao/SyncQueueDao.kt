package com.forgex.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.forgex.mobile.core.database.entity.SyncQueueEntity
import com.forgex.mobile.core.database.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * 同步队列 DAO。
 *
 * 提供 SyncQueueEntity 的查询、状态更新、重试计数、清理等操作。
 * 所有方法均为 suspend (协程友好) 或返回 Flow (响应式)。
 */
@Dao
interface SyncQueueDao : BaseDao<SyncQueueEntity> {

    /**
     * 按状态查询同步队列记录。
     *
     * @param status 同步状态
     * @return 符合状态的记录列表
     */
    @Query("SELECT * FROM sync_queue WHERE syncStatus = :status ORDER BY queuedAt ASC")
    suspend fun findByStatus(status: String): List<SyncQueueEntity>

    /**
     * 查询待处理 (PENDING) 记录, 限制返回数量。
     *
     * @param limit 最大返回数量
     * @return PENDING 状态的记录列表
     */
    @Query("SELECT * FROM sync_queue WHERE syncStatus = :pendingStatus ORDER BY queuedAt ASC LIMIT :limit")
    suspend fun findPending(pendingStatus: String = SyncStatus.PENDING.name, limit: Int = 50): List<SyncQueueEntity>

    /**
     * 更新记录状态。
     *
     * @param id 记录 ID
     * @param status 新状态 (SyncStatus 枚举名)
     */
    @Query("UPDATE sync_queue SET syncStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 递增重试计数并记录错误信息。
     * 如果 retryCount 未超过 maxRetry, 将状态重置为 PENDING; 否则标记为 FAILED。
     *
     * @param id 记录 ID
     * @param error 错误信息
     * @param maxRetry 最大重试次数
     */
    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, lastError = :error, " +
            "syncStatus = CASE WHEN retryCount + 1 >= maxRetry THEN 'FAILED' ELSE 'PENDING' END, " +
            "updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementRetry(id: Long, error: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 批量标记记录为 SYNCING 状态。
     *
     * @param ids 记录 ID 列表
     */
    @Query("UPDATE sync_queue SET syncStatus = 'SYNCING', updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun markSyncing(ids: List<Long>, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除已同步 (SYNCED) 的记录。
     */
    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSynced()

    /**
     * 标记记录为 FAILED 并记录错误信息。
     * 不可恢复的失败 (409/400/401) 使用此方法, 不递增 retryCount。
     *
     * @param id 记录 ID
     * @param error 错误信息
     */
    @Query("UPDATE sync_queue SET syncStatus = 'FAILED', lastError = :error, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markFailed(id: Long, error: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 按状态统计记录数 (响应式)。
     *
     * @param status 同步状态
     * @return 该状态的记录数量 Flow
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = :status")
    fun countByStatus(status: String): Flow<Int>

    /**
     * 统计待处理 (PENDING) 记录数 (响应式)。
     *
     * @return PENDING 记录数量 Flow
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    /**
     * 按 ID 查询记录。
     *
     * @param id 记录 ID
     * @return 记录, 不存在返回 null
     */
    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun findById(id: Long): SyncQueueEntity?

    /**
     * 删除所有记录 (登出时清空)。
     */
    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()
}
