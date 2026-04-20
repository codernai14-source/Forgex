package com.forgex.mobile.core.network.interceptor

import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.datastore.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class LanguageInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val resolvedLanguage = runBlocking {
            sessionStore.lastResolvedLanguageTag.first()
                ?: sessionStore.languageTag.first()
                ?: AppLanguage.currentSystemLanguage()
        }
        val languageTag = AppLanguage.normalize(resolvedLanguage)

        val localizedRequest = request.newBuilder()
            .header("X-Lang", languageTag)
            .header("Accept-Language", languageTag)
            .build()

        return chain.proceed(localizedRequest)
    }
}
