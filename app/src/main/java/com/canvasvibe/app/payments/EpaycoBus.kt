package com.canvasvibe.app.payments

import android.net.Uri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EpaycoBus {
    private val _events = MutableSharedFlow<EpaycoResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EpaycoResult> = _events.asSharedFlow()

    fun publish(result: EpaycoResult) {
        _events.tryEmit(result)
    }

    fun clear() {
        _events.resetReplayCache()
    }

    fun parseAndPublish(uri: Uri): Boolean {
        val isCustomScheme = uri.scheme == EpaycoConfig.deepLinkScheme &&
            uri.host == EpaycoConfig.deepLinkHost
        val isHttpResponse = uri.toString().startsWith(EpaycoConfig.responseUrl) ||
            uri.toString().startsWith(EpaycoConfig.confirmationUrl)
        if (!isCustomScheme && !isHttpResponse) return false

        val rawStatus = uri.getQueryParameter("x_response")
            ?: uri.getQueryParameter("x_transaction_state")
            ?: uri.getQueryParameter("transactionState")
            ?: uri.getQueryParameter("respuesta")
            ?: ""
        val refPayco = uri.getQueryParameter("x_ref_payco")
            ?: uri.getQueryParameter("ref_payco")
            ?: uri.getQueryParameter("transactionID")
            ?: ""
        val reason = uri.getQueryParameter("x_response_reason_text").orEmpty()

        // ePayco solo manda ref_payco en la response URL. El status se confirma
        // con el webhook server-side. Si llegamos con un ref y sin status explícito
        // asumimos aprobado (la transacción existe en ePayco). Si vino status,
        // lo respetamos.
        val approvedExplicit = rawStatus.equals("Aceptada", ignoreCase = true) ||
            rawStatus.equals("Aprobada", ignoreCase = true)
        val rejectedExplicit = rawStatus.equals("Rechazada", ignoreCase = true) ||
            rawStatus.equals("Fallida", ignoreCase = true) ||
            rawStatus.equals("Cancelada", ignoreCase = true)

        val approved = approvedExplicit || (rawStatus.isBlank() && refPayco.isNotBlank())
        val finalStatus = when {
            rawStatus.isNotBlank() -> rawStatus
            approved               -> "Aceptada"
            else                   -> "Pendiente"
        }

        publish(
            EpaycoResult(
                status = finalStatus,
                refPayco = refPayco,
                responseReason = reason,
                approved = approved && !rejectedExplicit
            )
        )
        return true
    }
}
