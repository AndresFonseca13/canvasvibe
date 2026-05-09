package com.canvasvibe.app.data.model

import com.google.firebase.firestore.PropertyName

data class Category(
    val id: String = "",
    val slug: String = "",
    val name: String = "",
    val emoji: String = "",
    val colorHex: String = "#7C4DFF",
    val productCount: Int = 0,
    val order: Int = 0,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
