package com.canvasvibe.app.ui.buyer.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.ui.buyer.components.BuyerBottomNav
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private val STATUS_ORDER = listOf("PENDING", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED")

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderTrackingViewModel = viewModel()
) {
    LaunchedEffect(orderId) { viewModel.load(orderId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TrackingTopBar()
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                is OrderTrackingUiState.Loading -> CenterText("Cargando…")
                is OrderTrackingUiState.Error -> CenterText(s.message)
                is OrderTrackingUiState.Ready -> TrackingContent(s.order)
            }
        }
        BuyerBottomNav(
            selectedIndex = 0,
            onSelect = { ix -> if (ix == 0) onBack() }
        )
    }
}

@Composable
private fun TrackingTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Seguimiento en vivo",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ayuda",
            color = PrimaryAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
private fun TrackingContent(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderCard(order = order)
        MapPlaceholder()
        TimelineCard(order = order)
    }
}

@Composable
private fun OrderCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Pedido #${order.id.take(8).uppercase().ifBlank { "CV-29041" }}",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Llegada estimada: ${formatTime(order.updatedAt)}",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPillFilled(label = statusLabel(order.status))
            StatusPillOutline(label = "Pago confirmado")
        }
    }
}

@Composable
private fun StatusPillFilled(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Primary)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusPillOutline(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = PrimaryAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MapPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    0f to SurfaceDark,
                    1f to Color(0xFF252525)
                )
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mapa en tiempo real",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Placeholder estilizado",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TimelineCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Timeline del pedido",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = buildTimelineText(order),
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = TextSecondary, fontSize = 13.sp)
    }
}

private fun statusLabel(status: String): String = when (status) {
    "DELIVERED" -> "Entregado"
    "SHIPPED", "IN_TRANSIT" -> "En camino"
    "PREPARING" -> "En preparación"
    "CANCELLED" -> "Cancelado"
    else -> "Confirmado"
}

private fun buildTimelineText(order: Order): String {
    val statusIndex = STATUS_ORDER.indexOf(order.status).coerceAtLeast(0)
    val items = mutableListOf<String>()
    items.add("• Pedido confirmado ${formatTime(order.createdAt)}")
    if (statusIndex >= 1) items.add("• Preparando producto —")
    if (statusIndex >= 2) items.add("• Repartidor asignado —")
    if (statusIndex >= 3) items.add("• En ruta —")
    if (statusIndex >= 4) items.add("• Entregado ${formatTime(order.updatedAt)}")
    return items.joinToString("\n")
}

private fun formatTime(millis: Long): String {
    if (millis <= 0L) return "—"
    val date = java.util.Date(millis)
    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return fmt.format(date)
}
