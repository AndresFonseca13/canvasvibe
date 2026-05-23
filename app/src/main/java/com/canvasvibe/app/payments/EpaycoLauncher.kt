package com.canvasvibe.app.payments

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

object EpaycoLauncher {

    fun open(context: Context, data: EpaycoCheckoutData) {
        val url = buildCheckoutUrl(data)
        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(0xFF1A1A1A.toInt())
                    .setSecondaryToolbarColor(0xFF7C4DFF.toInt())
                    .build()
            )
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .build()
        intent.launchUrl(context, url.toUri())
    }

    private fun buildCheckoutUrl(data: EpaycoCheckoutData): String {
        val params = mapOf(
            "key"     to EpaycoConfig.publicKey,
            "cust"    to EpaycoConfig.customerId,
            "amount"  to data.amount.toString(),
            "invoice" to data.invoice,
            "desc"    to data.description,
            "name"    to data.buyerName,
            "email"   to data.buyerEmail,
            "phone"   to data.buyerPhone,
            "test"    to if (EpaycoConfig.testMode) "true" else "false"
        )
        val query = params
            .filter { it.value.isNotBlank() }
            .entries
            .joinToString("&") { "${it.key}=${Uri.encode(it.value)}" }
        return "$BOOTSTRAP_URL?$query"
    }

    private const val BOOTSTRAP_URL =
        "https://andresfonseca13.github.io/canvasvibe/web/checkout.html"
}

data class EpaycoCheckoutData(
    val amount: Long,
    val description: String,
    val invoice: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String
)
