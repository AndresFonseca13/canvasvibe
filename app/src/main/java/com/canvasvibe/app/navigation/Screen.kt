package com.canvasvibe.app.navigation

sealed class Screen( val route: String) {
    object Login: Screen("login")
    object BuyerHome: Screen("buyer/home")
    object ProductDetail: Screen("buyer/product/{productId}") {
        fun createRoute(id: String) = "buyer/product/$id"
    }
    object Cart: Screen("buyer/cart")
    object OrderTracking: Screen("buyer/order/{orderId}"){
        fun createRoute(id: String) = "buyer/order/$id"
    }
    object SellerDashboard: Screen("seller/dashboard")
    object AddProduct: Screen("seller/add-product")
    object SellerOrders: Screen("seller/orders")
    object SellerProfile: Screen("seller/profile")
    object AdminDashboard: Screen("admin/dashboard")
    object AdminArtist: Screen("admin/artists")
    object AdminCategories: Screen("admin/categories")
    object AdminReports: Screen("admin/reports")
}