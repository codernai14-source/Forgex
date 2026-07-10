package com.forgex.mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.forgex.mobile.core.network.i18n.AppLanguageManager
import com.forgex.mobile.core.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Forgex Application。
 *
 * 实现 [Configuration.Provider] 接口, 使 WorkManager 使用 HiltWorkerFactory 进行延迟初始化。
 * 在 onCreate 中初始化:
 * - AppLanguageManager: 语言设置
 * - SyncManager: 注册周期同步 + 网络监听
 */
@HiltAndroidApp
class ForgexApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var appLanguageManager: AppLanguageManager

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 语言初始化
        runBlocking {
            appLanguageManager.applyBootstrapLanguage()
        }
        applicationScope.launch {
            appLanguageManager.initialize()
        }

        // 离线同步初始化
        syncManager.registerNetworkMonitor()
        syncManager.schedulePeriodicSync()
    }
}
