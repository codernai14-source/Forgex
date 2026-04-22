package com.forgex.mobile

import android.app.Application
import com.forgex.mobile.core.network.i18n.AppLanguageManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class ForgexApplication : Application() {

    @Inject
    lateinit var appLanguageManager: AppLanguageManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            appLanguageManager.applyBootstrapLanguage()
        }
        applicationScope.launch {
            appLanguageManager.initialize()
        }
    }
}
