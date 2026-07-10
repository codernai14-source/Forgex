package com.forgex.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.forgex.mobile.core.database.entity.MessageCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息缓存 DAO。
 *
 * 提供按分类查询、全量替换、标记已读、清空等操作。
 * 离线标记已读时通过 markRead 更新本地缓存, 同步队列负责联网重放。
 */
@Dao
interface MessageCacheDao : BaseDao<MessageCacheEntity> {

    /**
     * 按分类查询缓存记录 (响应式)。
     *
     * @param category 缓存分类 (UNREAD/READ)
     * @return 该分类的缓存记录 Flow
     */
    @Query("SELECT * FROM message_cache WHERE category = :category ORDER BY updatedAt DESC")
    fun findByCategory(category: String): Flow<List<MessageCacheEntity>>

    /**
     * 清空指定分类的所有缓存记录。
     *
     * @param category 缓存分类
     */
    @Query("DELETE FROM message_cache WHERE category = :category")
    suspend fun clearByCategory(category: String)

    /**
     * 全量替换指定分类的缓存。
     * 先清空该分类, 再批量插入新数据。调用方需在事务中调用。
     *
     * @param category 缓存分类
     * @param items 新的缓存记录列表
     */
    suspend fun replaceAll(category: String, items: List<MessageCacheEntity>) {
        clearByCategory(category)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }

    /**
     * 标记指定消息为已读 (更新本地缓存)。
     *
     * @param remoteId 服务端消息 ID
     */
    @Query("UPDATE message_cache SET msgStatus = 1, readTime = :readTime, updatedAt = :updatedAt WHERE remoteId = :remoteId")
    suspend fun markRead(remoteId: Long, readTime: String = "", updatedAt: Long = System.currentTimeMillis())

    /**
     * 标记所有消息为已读。
     */
    @Query("UPDATE message_cache SET msgStatus = 1, readTime = :readTime, updatedAt = :updatedAt WHERE msgStatus = 0")
    suspend fun markAllRead(readTime: String = "", updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除所有缓存记录 (登出时清空)。
     */
    @Query("DELETE FROM message_cache")
    suspend fun deleteAll()
}
