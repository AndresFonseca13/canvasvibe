package com.canvasvibe.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.canvasvibe.app.data.repository.CategoryRepository
import com.canvasvibe.app.navigation.AppNavigation
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.CanvasVibeTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                lifecycleScope.launch {
                    CategoryRepository().seedDefaultsIfEmpty()
                        .onFailure { Log.e("MainActivity", "category seed failed", it) }
                }
            }
        }

        setContent {
            CanvasVibeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .safeDrawingPadding()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
