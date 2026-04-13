package com.forgex.mobile.core.network.di

import com.forgex.mobile.core.network.api.AuthApi
import com.forgex.mobile.core.network.api.MessageApi
import com.forgex.mobile.core.network.api.MenuApi
import com.forgex.mobile.core.network.api.WorkbenchApi
import com.forgex.mobile.core.network.api.WorkflowApi
import com.forgex.mobile.core.network.interceptor.AuthInterceptor
import com.forgex.mobile.core.network.interceptor.DynamicBaseUrlInterceptor
import com.forgex.mobile.core.network.interceptor.SessionCookieJar
import com.forgex.mobile.core.network.interceptor.TenantInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
        tenantInterceptor: TenantInterceptor,
        sessionCookieJar: SessionCookieJar,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            // 地址重写要在认证头注入前执行，确保所有后续拦截器都基于目标环境运行
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(tenantInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMenuApi(retrofit: Retrofit): MenuApi {
        return retrofit.create(MenuApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkflowApi(retrofit: Retrofit): WorkflowApi {
        return retrofit.create(WorkflowApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageApi(retrofit: Retrofit): MessageApi {
        return retrofit.create(MessageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkbenchApi(retrofit: Retrofit): WorkbenchApi {
        return retrofit.create(WorkbenchApi::class.java)
    }
}
