package com.forgex.mobile.core.device

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * PDA 广播扫码注册协调器。
 *
 * 该协调器负责在应用前台生命周期内注册和注销 PDA 扫码广播，
 * 并将硬件广播统一转发到全局扫描结果总线。
 *
 * @param scannerManager 全局扫描结果分发管理器
 * @param actionRegistry PDA 广播 action 注册表
 */
class FxPdaScanCoordinator(
    private val scannerManager: FxScannerManager,
    private val actionRegistry: FxScannerActionRegistry
) {
    private var receiver: FxPdaScanReceiver? = null
    private var registeredActions: List<String> = emptyList()

    /**
     * 注册 PDA 扫码广播接收器。
     *
     * @param context 任意 Context，内部会自动切换到 ApplicationContext
     * @return 当前已注册的广播 action 列表；为空表示本次未完成注册
     */
    fun register(context: Context): List<String> {
        if (receiver != null) {
            return registeredActions
        }

        val actions = actionRegistry.actionsForBrand(Build.BRAND)
        if (actions.isEmpty()) {
            return emptyList()
        }

        val applicationContext = context.applicationContext
        val scanReceiver = FxPdaScanReceiver { result ->
            scannerManager.submit(result)
        }
        val intentFilter = IntentFilter().apply {
            actions.forEach(::addAction)
        }

        ContextCompat.registerReceiver(
            applicationContext,
            scanReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        receiver = scanReceiver
        registeredActions = actions
        return registeredActions
    }

    /**
     * 注销 PDA 扫码广播接收器。
     *
     * @param context 任意 Context，内部会自动切换到 ApplicationContext
     */
    fun unregister(context: Context) {
        val currentReceiver = receiver ?: return
        val applicationContext = context.applicationContext
        runCatching {
            applicationContext.unregisterReceiver(currentReceiver)
        }
        receiver = null
        registeredActions = emptyList()
    }

    /**
     * 判断当前是否已经完成 PDA 扫码广播注册。
     *
     * @return `true` 表示当前仍处于已注册状态
     */
    fun isRegistered(): Boolean = receiver != null
}
