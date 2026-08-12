package com.forgex.mobile.di

import android.content.Context
import androidx.work.WorkManager
import com.forgex.mobile.core.sync.SyncManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager 初始化辅助器。
 *
 * 封装周期同步注册逻辑, 在 Application onCreate 中调用。
 * WorkManager 通过 Configuration.Provider 接口实现 on-demand 初始化,
 * 首次调用 WorkManager.getInstance(context) 时触发。
 *
 * 周期同步:
 * - 间隔: 15 分钟 (WorkManager 最小周期)
 * - 约束: NETWORK_CONNECTED
 * - 策略: KEEP (不重复注册)
 */
@Singleton
class WorkManagerInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncManager: SyncManager
) {

    /**
     * 初始化 WorkManager 与周期同步任务。
     *
     * 调用时机: Application.onCreate()
     */
    fun initialize() {
        // 触发 WorkManager on-demand 初始化 (首次 getInstance 调用)
        WorkManager.getInstance(context)

        // 注册周期同步 (15 分钟, 仅联网时执行)
        syncManager.schedulePeriodicSync()

        // 注册网络监听 (联网恢复时自动触发同步)
        syncManager.registerNetworkMonitor()
    }
}
