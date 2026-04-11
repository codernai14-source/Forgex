package com.forgex.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.forgex.mobile.ui.ForgexMobileApp
import com.forgex.mobile.ui.theme.ForgexMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgexMobileTheme {
                ForgexMobileApp()
            }
        }
    }
}
