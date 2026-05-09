package com.canvasvibe.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.canvasvibe.app.data.prefs.BiometricPreferences
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.ui.admin.artists.AdminArtistScreen
import com.canvasvibe.app.ui.admin.buyers.AdminBuyersScreen
import com.canvasvibe.app.ui.admin.categories.AdminCategoriesScreen
import com.canvasvibe.app.ui.admin.dashboard.AdminDashboardScreen
import com.canvasvibe.app.ui.admin.reports.AdminReportsScreen
import com.canvasvibe.app.ui.auth.BiometricScreen
import com.canvasvibe.app.ui.auth.LoginScreen
import com.canvasvibe.app.ui.buyer.cart.CartScreen
import com.canvasvibe.app.ui.buyer.detail.ProductDetailScreen
import com.canvasvibe.app.ui.buyer.home.BuyerHomeScreen
import com.canvasvibe.app.ui.buyer.profile.BuyerProfileScreen
import com.canvasvibe.app.ui.buyer.tracking.OrderTrackingScreen
import com.canvasvibe.app.ui.seller.addproduct.AddProductScreen
import com.canvasvibe.app.ui.seller.dashboard.SellerDashboardScreen
import com.canvasvibe.app.ui.seller.orders.SellerOrdersScreen
import com.canvasvibe.app.ui.seller.products.SellerProductsScreen
import com.canvasvibe.app.ui.seller.profile.SellerProfileScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    fun dashboardFor(role: String): String = when (role) {
        "ROLE_SELLER" -> Screen.SellerDashboard.route
        "ROLE_ADMIN"  -> Screen.AdminDashboard.route
        else          -> Screen.BuyerHome.route
    }

    val logout: () -> Unit = {
        AuthRepository().logout()
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    val toSellerDashboard: () -> Unit = {
        navController.navigate(Screen.SellerDashboard.route) {
            popUpTo(Screen.SellerDashboard.route) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = { user ->
                val alreadyDecided = BiometricPreferences.hasDecided(context, user.uid)
                val target = if (alreadyDecided) {
                    dashboardFor(user.role)
                } else {
                    Screen.Biometric.createRoute(user.role)
                }
                navController.navigate(target) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.BuyerHome.route) {
            BuyerHomeScreen(
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onProfileClick = { navController.navigate(Screen.BuyerProfile.route) },
                onLogout = logout
            )
        }

        composable(Screen.BuyerProfile.route) {
            BuyerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = logout,
                onHomeClick = {
                    navController.navigate(Screen.BuyerHome.route) {
                        popUpTo(Screen.BuyerHome.route) { inclusive = true }
                    }
                },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.ProductDetail.route) { backStack ->
            val productId = backStack.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                        popUpTo(Screen.BuyerHome.route)
                    }
                }
            )
        }

        composable(Screen.OrderTracking.route) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(orderId = orderId, onBack = { navController.popBackStack() })
        }

        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                onOrdersClick = { navController.navigate(Screen.SellerOrders.route) },
                onAddProductClick = { navController.navigate(Screen.AddProduct.createRoute()) },
                onProductsClick = { navController.navigate(Screen.SellerProducts.route) },
                onProfileClick = { navController.navigate(Screen.SellerProfile.route) },
                onLogout = logout
            )
        }

        composable(Screen.SellerProducts.route) {
            SellerProductsScreen(
                onAddProductClick = { navController.navigate(Screen.AddProduct.createRoute()) },
                onEditProduct = { id -> navController.navigate(Screen.AddProduct.createRoute(id)) },
                onHomeClick = toSellerDashboard,
                onOrdersClick = { navController.navigate(Screen.SellerOrders.route) },
                onProfileClick = { navController.navigate(Screen.SellerProfile.route) }
            )
        }

        composable(
            route = Screen.AddProduct.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val productId = backStack.arguments?.getString("productId")
            AddProductScreen(
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SellerOrders.route) {
            SellerOrdersScreen(
                onBack = { navController.popBackStack() },
                onHomeClick = toSellerDashboard,
                onProductsClick = {
                    navController.navigate(Screen.SellerProducts.route) {
                        popUpTo(Screen.SellerDashboard.route)
                    }
                },
                onProfileClick = { navController.navigate(Screen.SellerProfile.route) }
            )
        }

        composable(Screen.SellerProfile.route) {
            SellerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = logout,
                onHomeClick = toSellerDashboard,
                onProductsClick = {
                    navController.navigate(Screen.SellerProducts.route) {
                        popUpTo(Screen.SellerDashboard.route)
                    }
                },
                onOrdersClick = { navController.navigate(Screen.SellerOrders.route) }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onArtistClick = { navController.navigate(Screen.AdminArtist.route) },
                onBuyersClick = { navController.navigate(Screen.AdminBuyers.route) },
                onCategoriesClick = { navController.navigate(Screen.AdminCategories.route) },
                onReportsClick = { navController.navigate(Screen.AdminReports.route) },
                onLogout = logout
            )
        }

        composable(Screen.AdminArtist.route) {
            AdminArtistScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
                onBuyersClick = { navController.navigate(Screen.AdminBuyers.route) },
                onCategoriesClick = { navController.navigate(Screen.AdminCategories.route) },
                onReportsClick = { navController.navigate(Screen.AdminReports.route) }
            )
        }

        composable(Screen.AdminBuyers.route) {
            AdminBuyersScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
                onArtistsClick = { navController.navigate(Screen.AdminArtist.route) },
                onCategoriesClick = { navController.navigate(Screen.AdminCategories.route) },
                onReportsClick = { navController.navigate(Screen.AdminReports.route) }
            )
        }

        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
                onArtistsClick = { navController.navigate(Screen.AdminArtist.route) },
                onBuyersClick = { navController.navigate(Screen.AdminBuyers.route) },
                onReportsClick = { navController.navigate(Screen.AdminReports.route) }
            )
        }

        composable(Screen.AdminReports.route) {
            AdminReportsScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
                onArtistsClick = { navController.navigate(Screen.AdminArtist.route) },
                onBuyersClick = { navController.navigate(Screen.AdminBuyers.route) },
                onCategoriesClick = { navController.navigate(Screen.AdminCategories.route) }
            )
        }

        composable(Screen.Biometric.route) { backStack ->
            val role = backStack.arguments?.getString("role") ?: "ROLE_BUYER"
            val dashboard = dashboardFor(role)
            BiometricScreen(
                onAuthSuccess = {
                    navController.navigate(dashboard) {
                        popUpTo(Screen.Biometric.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(dashboard) {
                        popUpTo(Screen.Biometric.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
