package com.canvasvibe.app

import android.content.Intent
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
import com.canvasvibe.app.payments.EpaycoBus
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

        handleEpaycoIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleEpaycoIntent(intent)
    }

    private fun handleEpaycoIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (EpaycoBus.parseAndPublish(data)) {
            Log.d("MainActivity", "ePayco deep link processed: $data")
        }
    }
}
