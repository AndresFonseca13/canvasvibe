package com.canvasvibe.app.ui.buyer.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.payments.EpaycoBus
import com.canvasvibe.app.payments.EpaycoCheckoutData
import com.canvasvibe.app.payments.EpaycoConfig
import com.canvasvibe.app.payments.EpaycoLauncher
import com.canvasvibe.app.payments.EpaycoResult
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun EpaycoCheckoutScreen(
    data: EpaycoCheckoutData,
    onResult: (EpaycoResult) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(data.invoice) {
        EpaycoBus.clear()
        if (EpaycoConfig.isConfigured()) {
            EpaycoLauncher.open(context, data)
        }
        EpaycoBus.events.collect { result ->
            currentOnResult(result)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBar(onCancel)

        if (!EpaycoConfig.isConfigured()) {
            EpaycoNotConfigured(onCancel)
            return@Column
        }

        WaitingPayment(
            amount = data.amount,
            onReopen = { EpaycoLauncher.open(context, data) },
            onCancel = onCancel
        )
    }
}

@Composable
private fun TopBar(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Background)
                .border(1.dp, BorderSubtle, CircleShape)
                .clickable { onCancel() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cancelar",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text("Pago con ePayco", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (EpaycoConfig.testMode) "Modo pruebas" else "Producción",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WaitingPayment(
    amount: Long,
    onReopen: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Esperando confirmación",
            color = TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Acabamos de abrir ePayco en una pestaña segura del navegador. Completa el pago y vuelve a esta pantalla.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Total: $ ${formatAmount(amount)} COP",
            color = PrimaryAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(22.dp))
        CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
        Spacer(Modifier.height(30.dp))

        OutlineButton(
            label = "Volver a abrir ePayco",
            icon = Icons.Filled.OpenInNew,
            onClick = onReopen
        )
        Spacer(Modifier.height(10.dp))
        OutlineButton(
            label = "Cancelar pago",
            icon = null,
            onClick = onCancel,
            destructive = true
        )
    }
}

@Composable
private fun OutlineButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val tone = if (destructive) TextSecondary else Primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tone.copy(alpha = 0.10f))
            .border(1.dp, tone.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, null, tint = tone, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(label, color = tone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EpaycoNotConfigured(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ePayco no está configurado",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Agrega EPAYCO_PUBLIC_KEY y EPAYCO_CUST_ID en local.properties y vuelve a compilar.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Primary)
                .clickable(onClick = onCancel)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text("Volver", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatAmount(amount: Long): String {
    return amount.toString().reversed().chunked(3).joinToString(".").reversed()
}
