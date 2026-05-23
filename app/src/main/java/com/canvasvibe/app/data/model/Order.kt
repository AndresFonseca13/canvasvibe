package com.canvasvibe.app.data.model

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val material: String = "",
    val size: String = "",
    val quantity: Int = 0,
    val unitPrice: Long = 0L,
    val totalPrice: Long = 0L,
    val status: String = "PENDING",  // PENDING, PREPARING, SHIPPED, DELIVERED, CANCELLED
    val shippingAddress: String = "",
    val shippingCity: String = "",
    val shippingPhone: String = "",
    val shippingNotes: String = "",
    val shippingLatitude: Double? = null,
    val shippingLongitude: Double? = null,
    val paymentMethod: String = "",
    val paymentRef: String = "",
    val paymentStatus: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
