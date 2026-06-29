package com.example.splixter

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as ComposeSurface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.theme.SplixterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enable120HzHighRefreshRate()
        setContent {
            val viewModel: SplitterViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            SplixterTheme(darkTheme = uiState.isDarkMode) {
                ComposeSurface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(splitterViewModel = viewModel)
                }
            }
        }
    }

    private fun enable120HzHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display
                if (currentDisplay != null) {
                    val supportedModes = currentDisplay.supportedModes
                    val maxRefreshMode = supportedModes.maxByOrNull { it.refreshRate }
                    if (maxRefreshMode != null && maxRefreshMode.refreshRate >= 89f) {
                        val params = window.attributes
                        params.preferredDisplayModeId = maxRefreshMode.modeId
                        window.attributes = params
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val defaultDisplay = windowManager.defaultDisplay
                val supportedModes = defaultDisplay.supportedModes
                val maxRefreshMode = supportedModes.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null && maxRefreshMode.refreshRate >= 89f) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
