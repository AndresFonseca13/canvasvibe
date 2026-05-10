package com.canvasvibe.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object BuyerHome : Screen("buyer/home")
    object BuyerProfile : Screen("buyer/profile")
    object ProductDetail : Screen("buyer/product/{productId}") {
        fun createRoute(id: String) = "buyer/product/$id"
    }
    object Cart : Screen("buyer/cart")
    object Checkout : Screen("buyer/checkout")
    object OrderTracking : Screen("buyer/order/{orderId}") {
        fun createRoute(id: String) = "buyer/order/$id"
    }
    object SellerDashboard : Screen("seller/dashboard")
    object SellerProducts : Screen("seller/products")
    object AddProduct : Screen("seller/product/edit?productId={productId}") {
        fun createRoute(productId: String? = null): String =
            if (productId.isNullOrBlank()) "seller/product/edit"
            else "seller/product/edit?productId=$productId"
    }
    object SellerOrders : Screen("seller/orders")
    object SellerProfile : Screen("seller/profile")
    object AdminDashboard : Screen("admin/dashboard")
    object AdminArtist : Screen("admin/artists")
    object AdminBuyers : Screen("admin/buyers")
    object AdminCategories : Screen("admin/categories")
    object AdminReports : Screen("admin/reports")
    object Biometric : Screen("shared/biometric/{role}") {
        fun createRoute(role: String) = "shared/biometric/$role"
    }
}
