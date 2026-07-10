package com.forgex.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.forgex.mobile.core.database.converter.Converters
import com.forgex.mobile.core.database.dao.MessageCacheDao
import com.forgex.mobile.core.database.dao.SyncQueueDao
import com.forgex.mobile.core.database.dao.WorkflowCacheDao
import com.forgex.mobile.core.database.entity.MessageCacheEntity
import com.forgex.mobile.core.database.entity.SyncQueueEntity
import com.forgex.mobile.core.database.entity.WorkflowCacheEntity

/**
 * Forgex Room 数据库。
 *
 * 聚合所有 Entity 与 Dao, version = 1 (全新数据库, 无存量数据需迁移)。
 * 首批允许 fallbackToDestructiveMigration, 后续 schema 变更需新增 Migration。
 *
 * @see DatabaseModule 提供 Hilt 依赖注入
 */
@Database(
    entities = [
        SyncQueueEntity::class,
        WorkflowCacheEntity::class,
        MessageCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ForgexDatabase : RoomDatabase() {

    /**
     * 获取同步队列 Dao。
     *
     * @return SyncQueueDao 实例
     */
    abstract fun syncQueueDao(): SyncQueueDao

    /**
     * 获取工作流缓存 Dao。
     *
     * @return WorkflowCacheDao 实例
     */
    abstract fun workflowCacheDao(): WorkflowCacheDao

    /**
     * 获取消息缓存 Dao。
     *
     * @return MessageCacheDao 实例
     */
    abstract fun messageCacheDao(): MessageCacheDao

    companion object {
        /** 数据库名称 */
        const val DATABASE_NAME = "forgex.db"
    }
}
