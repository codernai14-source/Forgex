package com.forgex.mobile.core.network.interceptor

import com.forgex.mobile.core.datastore.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TenantInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tenantId = runBlocking { sessionStore.tenantId.first() }

        if (tenantId.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val withTenantRequest = request.newBuilder()
            .header("Tenant-Id", tenantId)
            .build()

        return chain.proceed(withTenantRequest)
    }
}
