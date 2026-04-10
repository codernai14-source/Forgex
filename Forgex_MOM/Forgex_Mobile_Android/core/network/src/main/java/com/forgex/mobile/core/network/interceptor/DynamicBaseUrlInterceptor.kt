package com.forgex.mobile.core.network.interceptor

import com.forgex.mobile.core.datastore.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 动态网关地址拦截器。
 *
 * 作用：
 * 1. 登录页配置了自定义服务器后，不需要重建 Retrofit。
 * 2. 在请求发出前把 scheme/host/port 重写为用户当前配置的地址。
 */
class DynamicBaseUrlInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = runBlocking { sessionStore.serverEndpoint.first() }

        // 未配置自定义地址时，保持 BuildConfig.BASE_URL 的默认行为
        if (endpoint == null || endpoint.host.isBlank()) {
            return chain.proceed(request)
        }

        // 仅改写网关主机部分，path/query 保持原请求不变
        val targetUrl = request.url.newBuilder()
            .scheme(endpoint.scheme.ifBlank { "http" })
            .host(endpoint.host)
            .port(endpoint.port)
            .build()

        return chain.proceed(
            request.newBuilder()
                .url(targetUrl)
                .build()
        )
    }
}
