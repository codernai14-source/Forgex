package com.forgex.mobile.core.sync.di

import android.content.Context
import androidx.work.WorkManager
import com.forgex.mobile.core.sync.conflict.ConflictResolver
import com.forgex.mobile.core.sync.conflict.LwwConflictResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 同步模块 Hilt 依赖注入配置。
 *
 * 仅提供无法通过 @Inject constructor 直接构造的绑定:
 * - ConflictResolver: 接口绑定到 LwwConflictResolver 实现
 * - WorkManager: 需 Context 构造, 且使用 Configuration.Provider on-demand 初始化
 *
 * NetworkMonitor / SyncCoordinator / SyncManager 均有 @Inject constructor,
 * 由 Hilt 自动构造, 无需 @Provides (避免绑定冲突)。
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    /**
     * 提供 ConflictResolver (绑定 LwwConflictResolver)。
     *
     * @return LwwConflictResolver 实例, 作为 ConflictResolver 接口实现
     */
    @Provides
    @Singleton
    fun provideConflictResolver(): ConflictResolver {
        return LwwConflictResolver()
    }

    /**
     * 提供 WorkManager (on-demand 初始化, 依赖 ForgexApplication 实现 Configuration.Provider)。
     *
     * @param context 应用上下文
     * @return WorkManager 实例
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
