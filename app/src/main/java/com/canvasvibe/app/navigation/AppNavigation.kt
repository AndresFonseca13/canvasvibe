package com.canvasvibe.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canvasvibe.app.ui.auth.LoginScreen
import com.canvasvibe.app.ui.buyer.home.BuyerHomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route){

        composable(Screen.Login.route){
            LoginScreen(onLoginSuccess = { user ->
                val  dest = when (user.role) {
                    "ROLER_SELLER" -> Screen.SellerDashboard.route
                    "ROLLER_ADMIN" -> Screen.AdminDashboard.route
                    else -> Screen.BuyerHome.route
                }
                navController.navigate(dest) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.BuyerHome.route) {
            BuyerHomeScreen(
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.ProductDetail.route) { backStack ->
            val productId = backStack.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId = productId, onBack = { navController.popBackStack() })
        }

        composable(Screen.Cart.route){
            CartScreen(
                onBack = { navController.popBackStack() },
                OnOrderPlaced = { orderId -> navController.navigate(Screen.OrderTracking.createRoute(orderId)) }
            )
        }

        composable(Screen.OrderTracking.route) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(orderId = orderId, onBack = { navController.popBackStack() })
        }

        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                onOrdersClick = { navController.navigate(Screen.SellerOrders.route) },
                onAddProductClick = { navController.navigate(Screen.AddProduct.route) }
                onProfileClick = { navController.navigate(Screen.SellerProfile.route)}
            )
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.SellerOrders.route){
            SellerOrdersScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.SellerProfile.route) {
            SellerProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onArtistClick = { navController.navigate(Screen.AdminArtist.route) },
                onCategoriesClick = { navController.navigate(Screen.AdminCategories.route) },
                onReportsClick = { navController.navigate(Screen.AdminReports.route) }
            )
        }

        composable(Screen.AdminArtist.route) {
            AdminArtistScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminReports.route) {
            AdminReportsScreen(onBack = { navController.popBackStack() })
        }

    }

}