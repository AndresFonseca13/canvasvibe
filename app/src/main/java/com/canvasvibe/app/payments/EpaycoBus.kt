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

        val status = uri.getQueryParameter("x_response").orEmpty()
        val refPayco = uri.getQueryParameter("x_ref_payco")
            ?: uri.getQueryParameter("ref_payco").orEmpty()
        val reason = uri.getQueryParameter("x_response_reason_text").orEmpty()
        val approved = status.equals("Aceptada", ignoreCase = true) ||
            status.equals("Aprobada", ignoreCase = true)

        publish(
            EpaycoResult(
                status = status.ifBlank { "Pendiente" },
                refPayco = refPayco,
                responseReason = reason,
                approved = approved
            )
        )
        return true
    }
}
