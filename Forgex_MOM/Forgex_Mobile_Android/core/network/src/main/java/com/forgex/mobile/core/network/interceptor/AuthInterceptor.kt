package com.forgex.mobile.core.network.interceptor

import com.forgex.mobile.core.datastore.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = runBlocking { sessionStore.token.first() }

        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authorizedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authorizedRequest)
    }
}
