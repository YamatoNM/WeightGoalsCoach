package com.polar.pulsecoach

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.polar.pulsecoach.ui.PulseCoachApp
import com.polar.pulsecoach.ui.theme.PulseCoachTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Launcher of the Pulse Coach flavor. Shares the app's single PolarBleApi
 * (provided by PolarBleSdkModule) with the PSDC screens that admins can open.
 */
@AndroidEntryPoint
class PulseCoachActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseCoachTheme {
                PulseCoachApp()
            }
        }
    }
}
