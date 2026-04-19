package com.canvasvibe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.canvasvibe.app.data.model.User
import com.canvasvibe.app.ui.auth.LoginScreen
import com.canvasvibe.app.ui.buyer.home.BuyerHomeScreen
import com.canvasvibe.app.ui.theme.CanvasVibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasVibeTheme {
                var currentUser by remember { mutableStateOf<User?>(null) }

                if (currentUser == null) {
                    LoginScreen(
                        onLoginSuccess = { user -> currentUser = user }
                    )
                } else {
                    when (currentUser!!.role) {
                        "ROLE_BUYER"  -> BuyerHomeScreen(userName = currentUser!!.name)
                        "ROLE_SELLER" -> BuyerHomeScreen(userName = currentUser!!.name) // TODO SellerDashboard
                        "ROLE_ADMIN"  -> BuyerHomeScreen(userName = currentUser!!.name) // TODO AdminDashboard
                        else          -> BuyerHomeScreen(userName = currentUser!!.name)
                    }
                }
            }
        }
    }
}