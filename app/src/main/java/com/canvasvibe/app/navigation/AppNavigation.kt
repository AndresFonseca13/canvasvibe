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
import com.canvasvibe.app.ui.buyer.checkout.CheckoutScreen
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
            popUpTo(Screen.SellerDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toSellerProducts: () -> Unit = {
        navController.navigate(Screen.SellerProducts.route) {
            popUpTo(Screen.SellerDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toSellerOrders: () -> Unit = {
        navController.navigate(Screen.SellerOrders.route) {
            popUpTo(Screen.SellerDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toSellerProfile: () -> Unit = {
        navController.navigate(Screen.SellerProfile.route) {
            popUpTo(Screen.SellerDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    val toAdminDashboard: () -> Unit = {
        navController.navigate(Screen.AdminDashboard.route) {
            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toAdminArtists: () -> Unit = {
        navController.navigate(Screen.AdminArtist.route) {
            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toAdminBuyers: () -> Unit = {
        navController.navigate(Screen.AdminBuyers.route) {
            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toAdminCategories: () -> Unit = {
        navController.navigate(Screen.AdminCategories.route) {
            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toAdminReports: () -> Unit = {
        navController.navigate(Screen.AdminReports.route) {
            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    val toBuyerHome: () -> Unit = {
        navController.navigate(Screen.BuyerHome.route) {
            popUpTo(Screen.BuyerHome.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toBuyerCart: () -> Unit = {
        navController.navigate(Screen.Cart.route) {
            popUpTo(Screen.BuyerHome.route) { inclusive = false }
            launchSingleTop = true
        }
    }
    val toBuyerProfile: () -> Unit = {
        navController.navigate(Screen.BuyerProfile.route) {
            popUpTo(Screen.BuyerHome.route) { inclusive = false }
            launchSingleTop = true
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
                onCartClick = toBuyerCart,
                onProfileClick = toBuyerProfile,
                onLogout = logout
            )
        }

        composable(Screen.BuyerProfile.route) {
            BuyerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = logout,
                onHomeClick = toBuyerHome,
                onCartClick = toBuyerCart
            )
        }

        composable(Screen.ProductDetail.route) { backStack ->
            val productId = backStack.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onHomeClick = toBuyerHome,
                onCartClick = toBuyerCart,
                onProfileClick = toBuyerProfile
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                onHomeClick = toBuyerHome,
                onProfileClick = toBuyerProfile
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPaid = { orderId ->
                    navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                        popUpTo(Screen.BuyerHome.route)
                    }
                }
            )
        }

        composable(Screen.OrderTracking.route) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                onHomeClick = toBuyerHome,
                onCartClick = toBuyerCart,
                onProfileClick = toBuyerProfile
            )
        }

        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                onOrdersClick = toSellerOrders,
                onAddProductClick = { navController.navigate(Screen.AddProduct.createRoute()) },
                onProductsClick = toSellerProducts,
                onProfileClick = toSellerProfile,
                onLogout = logout
            )
        }

        composable(Screen.SellerProducts.route) {
            SellerProductsScreen(
                onAddProductClick = { navController.navigate(Screen.AddProduct.createRoute()) },
                onEditProduct = { id -> navController.navigate(Screen.AddProduct.createRoute(id)) },
                onHomeClick = toSellerDashboard,
                onOrdersClick = toSellerOrders,
                onProfileClick = toSellerProfile
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
                onProductsClick = toSellerProducts,
                onProfileClick = toSellerProfile
            )
        }

        composable(Screen.SellerProfile.route) {
            SellerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = logout,
                onHomeClick = toSellerDashboard,
                onProductsClick = toSellerProducts,
                onOrdersClick = toSellerOrders
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onArtistClick = toAdminArtists,
                onBuyersClick = toAdminBuyers,
                onCategoriesClick = toAdminCategories,
                onReportsClick = toAdminReports,
                onLogout = logout
            )
        }

        composable(Screen.AdminArtist.route) {
            AdminArtistScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = toAdminDashboard,
                onBuyersClick = toAdminBuyers,
                onCategoriesClick = toAdminCategories,
                onReportsClick = toAdminReports
            )
        }

        composable(Screen.AdminBuyers.route) {
            AdminBuyersScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = toAdminDashboard,
                onArtistsClick = toAdminArtists,
                onCategoriesClick = toAdminCategories,
                onReportsClick = toAdminReports
            )
        }

        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = toAdminDashboard,
                onArtistsClick = toAdminArtists,
                onBuyersClick = toAdminBuyers,
                onReportsClick = toAdminReports
            )
        }

        composable(Screen.AdminReports.route) {
            AdminReportsScreen(
                onBack = { navController.popBackStack() },
                onDashboardClick = toAdminDashboard,
                onArtistsClick = toAdminArtists,
                onBuyersClick = toAdminBuyers,
                onCategoriesClick = toAdminCategories
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
