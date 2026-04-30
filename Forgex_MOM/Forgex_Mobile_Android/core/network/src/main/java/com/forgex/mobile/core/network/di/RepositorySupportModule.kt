package com.forgex.mobile.core.network.di

import android.content.Context
import com.forgex.mobile.core.network.api.FileApi
import com.forgex.mobile.core.network.exception.ErrorCodeMapper
import com.forgex.mobile.core.network.interceptor.ConnectivityMonitor
import com.forgex.mobile.core.network.repository_support.ApiExecutor
import com.forgex.mobile.core.network.repository_support.DownloadService
import com.forgex.mobile.core.network.repository_support.UploadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositorySupportModule {

    @Provides
    @Singleton
    fun provideErrorCodeMapper(): ErrorCodeMapper = ErrorCodeMapper()

    @Provides
    @Singleton
    fun provideApiExecutor(errorCodeMapper: ErrorCodeMapper): ApiExecutor = ApiExecutor(errorCodeMapper)

    @Provides
    @Singleton
    fun provideConnectivityMonitor(@ApplicationContext context: Context): ConnectivityMonitor {
        return ConnectivityMonitor(context)
    }

    @Provides
    @Singleton
    fun provideFileApi(retrofit: Retrofit): FileApi = retrofit.create(FileApi::class.java)

    @Provides
    @Singleton
    fun provideDownloadService(fileApi: FileApi): DownloadService = DownloadService(fileApi)

    @Provides
    @Singleton
    fun provideUploadService(): UploadService = UploadService()
}
