package com.forgex.mobile.core.sync.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 响应式网络状态监听器。
 *
 * 封装 ConnectivityManager.NetworkCallback, 暴露:
 * - [isOnline]: StateFlow<Boolean>, 网络状态变化时自动发射
 * - [isCurrentlyOnline()]: 同步检查当前网络状态
 *
 * 联网恢复时自动触发 SyncCoordinator 的即时同步。
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(isCurrentlyOnline())

    /** 网络在线状态 (StateFlow, 初始值为当前状态) */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var callback: ConnectivityManager.NetworkCallback? = null
    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * 同步检查当前网络状态。
     *
     * @return true 表示当前有可用网络连接
     */
    fun isCurrentlyOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * 注册网络状态监听。
     * 网络状态变化时自动更新 [isOnline] StateFlow。
     */
    fun register() {
        if (callback != null) {
            return
        }

        val manager = connectivityManager ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                _isOnline.value = hasInternet
            }
        }

        manager.registerNetworkCallback(request, networkCallback)
        callback = networkCallback
    }

    /**
     * 注销网络状态监听。
     */
    fun unregister() {
        val manager = connectivityManager ?: return
        callback?.let {
            manager.unregisterNetworkCallback(it)
        }
        callback = null
    }
}
