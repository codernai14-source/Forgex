package com.forgex.mobile.core.datastore.di

import android.content.Context
import com.forgex.mobile.core.datastore.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore {
        return SessionStore(context)
    }
}
