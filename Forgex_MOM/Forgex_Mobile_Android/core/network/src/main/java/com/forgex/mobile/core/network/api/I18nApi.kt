package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.i18n.LanguageType
import com.forgex.mobile.core.network.model.i18n.MobileI18nBundleRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface I18nApi {

    @POST("sys/i18n/languageType/listEnabled")
    suspend fun listEnabledLanguages(): ApiResponse<List<LanguageType>>

    @POST("sys/i18n/languageType/getDefault")
    suspend fun getDefaultLanguage(): ApiResponse<LanguageType>

    @POST("sys/i18n/message/mobileBundle")
    suspend fun getMobileBundle(@Body request: MobileI18nBundleRequest): ApiResponse<Map<String, String>>
}
