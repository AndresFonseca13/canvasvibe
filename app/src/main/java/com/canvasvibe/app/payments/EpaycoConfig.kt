package com.canvasvibe.app.payments

import com.canvasvibe.app.BuildConfig

object EpaycoConfig {
    val publicKey: String  = BuildConfig.EPAYCO_PUBLIC_KEY
    val customerId: String = BuildConfig.EPAYCO_CUST_ID
    val testMode: Boolean  = BuildConfig.EPAYCO_TEST_MODE
    const val responseUrl: String = "https://canvasvibe.app/payment/response"
    const val confirmationUrl: String = "https://canvasvibe.app/payment/confirmation"

    fun isConfigured(): Boolean = publicKey.isNotBlank()
}

data class EpaycoResult(
    val status: String,        // "Aceptada" | "Rechazada" | "Pendiente" | "Fallida" | "Cancelada"
    val refPayco: String,
    val responseReason: String,
    val approved: Boolean
)
