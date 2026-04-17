package com.canvasvibe.app.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "ROLE_BUYER",
    val createdAt: Long = System.currentTimeMillis()
)