package com.forgex.mobile.core.device.di

import android.content.Context
import com.forgex.mobile.core.device.FxNfcScanManager
import com.forgex.mobile.core.device.FxPdaScanCoordinator
import com.forgex.mobile.core.device.FxScanFeedback
import com.forgex.mobile.core.device.FxScannerActionRegistry
import com.forgex.mobile.core.device.FxScannerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 设备能力依赖注入模块。
 *
 * 统一提供扫描、NFC、PDA 广播注册与扫码反馈等单例能力。
 */
@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {

    /**
     * 提供全局扫描结果分发管理器。
     *
     * @return 全局扫描结果总线
     */
    @Provides
    @Singleton
    fun provideFxScannerManager(): FxScannerManager {
        return FxScannerManager()
    }

    /**
     * 提供 NFC 扫描管理器。
     *
     * @return NFC 扫描管理器
     */
    @Provides
    @Singleton
    fun provideFxNfcScanManager(): FxNfcScanManager {
        return FxNfcScanManager()
    }

    /**
     * 提供 PDA 扫码广播 action 注册表。
     *
     * @return PDA 扫码广播 action 注册表
     */
    @Provides
    @Singleton
    fun provideFxScannerActionRegistry(): FxScannerActionRegistry {
        return FxScannerActionRegistry()
    }

    /**
     * 提供 PDA 扫码广播协调器。
     *
     * @param scannerManager 全局扫描结果分发管理器
     * @param scannerActionRegistry PDA 广播 action 注册表
     * @return PDA 扫码广播协调器
     */
    @Provides
    @Singleton
    fun provideFxPdaScanCoordinator(
        scannerManager: FxScannerManager,
        scannerActionRegistry: FxScannerActionRegistry
    ): FxPdaScanCoordinator {
        return FxPdaScanCoordinator(
            scannerManager = scannerManager,
            actionRegistry = scannerActionRegistry
        )
    }

    /**
     * 提供扫码反馈能力。
     *
     * @param context 应用上下文
     * @return 扫码反馈能力
     */
    @Provides
    @Singleton
    fun provideFxScanFeedback(@ApplicationContext context: Context): FxScanFeedback {
        return FxScanFeedback(context)
    }
}
