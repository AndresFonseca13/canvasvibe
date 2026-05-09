package com.canvasvibe.app.data.model

import com.google.firebase.firestore.PropertyName

data class Product(
    val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val materials: List<String> = emptyList(),
    val sizes: List<String> = emptyList(),
    val priceBase: Long = 0L,
    val imageUrls: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val stock: Int = 0,
    val elaborationDays: String = "",
    @get:PropertyName("isCustomizable")
    @set:PropertyName("isCustomizable")
    var isCustomizable: Boolean = true,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)