package com.forgex.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.forgex.mobile.core.database.entity.WorkflowCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 工作流待办缓存 DAO。
 *
 * 提供按分类查询、全量替换、清空等操作。
 * 在线刷新时使用 replaceAll: 先清空该分类再批量插入, 事务包裹。
 */
@Dao
interface WorkflowCacheDao : BaseDao<WorkflowCacheEntity> {

    /**
     * 按分类查询缓存记录 (响应式)。
     *
     * @param category 缓存分类 (PENDING/APPROVED/MINE)
     * @return 该分类的缓存记录 Flow
     */
    @Query("SELECT * FROM workflow_cache WHERE category = :category ORDER BY updatedAt DESC")
    fun findByCategory(category: String): Flow<List<WorkflowCacheEntity>>

    /**
     * 清空指定分类的所有缓存记录。
     *
     * @param category 缓存分类
     */
    @Query("DELETE FROM workflow_cache WHERE category = :category")
    suspend fun clearByCategory(category: String)

    /**
     * 全量替换指定分类的缓存。
     * 先清空该分类, 再批量插入新数据。调用方需在事务中调用。
     *
     * @param category 缓存分类
     * @param items 新的缓存记录列表
     */
    suspend fun replaceAll(category: String, items: List<WorkflowCacheEntity>) {
        clearByCategory(category)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }

    /**
     * 获取指定分类的缓存写入时间。
     *
     * @param category 缓存分类
     * @return 缓存时间 (epoch millis), 无缓存返回 null
     */
    @Query("SELECT cacheTime FROM workflow_cache WHERE category = :category LIMIT 1")
    suspend fun getCacheTime(category: String): Long?

    /**
     * 删除所有缓存记录 (登出时清空)。
     */
    @Query("DELETE FROM workflow_cache")
    suspend fun deleteAll()
}
