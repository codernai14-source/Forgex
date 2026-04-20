package com.forgex.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.forgex.mobile.ui.ForgexMobileApp
import com.forgex.mobile.ui.theme.ForgexMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgexMobileTheme {
                ForgexMobileApp()
            }
        }
    }
}
