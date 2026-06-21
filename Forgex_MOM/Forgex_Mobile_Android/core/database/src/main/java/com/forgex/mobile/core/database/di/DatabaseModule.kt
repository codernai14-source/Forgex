package com.forgex.mobile.core.database.di

import android.content.Context
import androidx.room.Room
import com.forgex.mobile.core.database.ForgexDatabase
import com.forgex.mobile.core.database.dao.MessageCacheDao
import com.forgex.mobile.core.database.dao.SyncQueueDao
import com.forgex.mobile.core.database.dao.WorkflowCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库 Hilt 依赖注入模块。
 *
 * 提供 ForgexDatabase 单例和各 Dao 的注入。
 * 使用 fallbackToDestructiveMigration 允许首批破坏性迁移 (无存量数据)。
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供 ForgexDatabase 单例。
     *
     * @param context 应用上下文
     * @return ForgexDatabase 实例
     */
    @Provides
    @Singleton
    fun provideForgexDatabase(
        @ApplicationContext context: Context
    ): ForgexDatabase {
        return Room.databaseBuilder(
            context,
            ForgexDatabase::class.java,
            ForgexDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * 提供 SyncQueueDao。
     *
     * @param database ForgexDatabase 实例
     * @return SyncQueueDao 实例
     */
    @Provides
    @Singleton
    fun provideSyncQueueDao(database: ForgexDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }

    /**
     * 提供 WorkflowCacheDao。
     *
     * @param database ForgexDatabase 实例
     * @return WorkflowCacheDao 实例
     */
    @Provides
    @Singleton
    fun provideWorkflowCacheDao(database: ForgexDatabase): WorkflowCacheDao {
        return database.workflowCacheDao()
    }

    /**
     * 提供 MessageCacheDao。
     *
     * @param database ForgexDatabase 实例
     * @return MessageCacheDao 实例
     */
    @Provides
    @Singleton
    fun provideMessageCacheDao(database: ForgexDatabase): MessageCacheDao {
        return database.messageCacheDao()
    }
}
