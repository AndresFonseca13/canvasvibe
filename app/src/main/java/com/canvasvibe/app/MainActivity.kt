package com.canvasvibe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.canvasvibe.app.navigation.AppNavigation
import com.canvasvibe.app.ui.theme.CanvasVibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasVibeTheme {
                AppNavigation()
            }
        }
    }
}
