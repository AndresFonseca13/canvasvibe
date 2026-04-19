package com.canvasvibe.app.data.model

data class CartItem(
    val productId: String = "",
    val title: String = "",
    val sellerName: String = "",
    val material: String = "",
    val size: String = "",
    val quantity: Int = 0,
    val unitPrice: Long = 0L,
    val imageUrl: String = ""
)