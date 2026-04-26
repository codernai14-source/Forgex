package com.forgex.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.forgex.mobile.core.device.FxNfcScanManager
import com.forgex.mobile.core.device.FxPdaScanCoordinator
import com.forgex.mobile.core.device.FxScannerManager
import com.forgex.mobile.ui.ForgexMobileApp
import com.forgex.mobile.ui.theme.ForgexMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android 主入口 Activity。
 *
 * 负责接管 PDA 广播与 NFC Intent，并将硬件扫描结果统一投递到扫描总线。
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var scannerManager: FxScannerManager

    @Inject
    lateinit var nfcScanManager: FxNfcScanManager

    @Inject
    lateinit var pdaScanCoordinator: FxPdaScanCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchScanIntent(intent)
        setContent {
            ForgexMobileTheme {
                ForgexMobileApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        pdaScanCoordinator.register(this)
    }

    override fun onResume() {
        super.onResume()
        nfcScanManager.enableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchScanIntent(intent)
    }

    /**
     * 在界面离开前台时关闭 NFC 前台分发。
     */
    override fun onPause() {
        nfcScanManager.disableForegroundDispatch(this)
        super.onPause()
    }

    /**
     * 在界面离开前台后注销 PDA 广播接收器。
     */
    override fun onStop() {
        pdaScanCoordinator.unregister(this)
        super.onStop()
    }

    /**
     * 处理系统分发到 Activity 的 NFC Intent，并投递到统一扫描总线。
     *
     * @param intent 系统分发的 Intent
     */
    private fun dispatchScanIntent(intent: Intent?) {
        val result = nfcScanManager.parseIntent(intent) ?: return
        scannerManager.submit(result)
    }
}
