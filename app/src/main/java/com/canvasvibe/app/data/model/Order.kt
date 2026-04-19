package com.canvasvibe.app.data.model

data class Order(
    val id: String = "",
    val sellerId: String = "",
    val product: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val material: String = "",
    val size: String = "",
    val quantity: Int = 0,
    val unitPrice: Long = 0L,
    val totalPrice: Long = 0L,
    val status: String = "PENDING",  // PENDING, PREPARING, SHIPPED, DELIVERED, CANCELED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)