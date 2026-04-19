package com.canvasvibe.app.data.model

data class Product (
    val id: String ="",
    val sellerId: String = "",
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
    val isCustomizable: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
    )