package com.forgex.mobile.core.network.interceptor

import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一网络配置管理器。
 */
@Singleton
class NetworkConfigManager @Inject constructor(
    private val sessionStore: SessionStore
) {
    val serverEndpoint: Flow<ServerEndpointConfig?> = sessionStore.serverEndpoint

    suspend fun updateServer(host: String, port: Int, scheme: String) {
        sessionStore.saveServerEndpoint(host = host, port = port, scheme = scheme)
    }

    suspend fun resetServer() {
        sessionStore.clearServerEndpoint()
    }
}
