package com.canvasvibe.app.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "ROLE_BUYER",
    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val deletedAt: Long? = null
)
