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
            "p_cust_id_cliente" to EpaycoConfig.customerId,
            "p_key"             to EpaycoConfig.publicKey,
            "p_id_invoice"      to data.invoice,
            "p_description"     to data.description,
            "p_amount"          to data.amount.toString(),
            "p_tax"             to "0",
            "p_amount_base"     to data.amount.toString(),
            "p_currency_code"   to "COP",
            "p_test_request"    to if (EpaycoConfig.testMode) "TRUE" else "FALSE",
            "p_url_response"    to EpaycoConfig.responseUrl,
            "p_url_confirmation" to EpaycoConfig.confirmationUrl,
            "p_billing_name"    to data.buyerName,
            "p_billing_email"   to data.buyerEmail,
            "p_billing_mobilephone" to data.buyerPhone,
            "p_extra1"          to "CanvasVibe",
            "p_lang"            to "es"
        )

        val query = params
            .filter { it.value.isNotBlank() }
            .entries
            .joinToString("&") { "${it.key}=${Uri.encode(it.value)}" }

        return "https://secure.epayco.co/checkout.php?$query"
    }
}

data class EpaycoCheckoutData(
    val amount: Long,
    val description: String,
    val invoice: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String
)
