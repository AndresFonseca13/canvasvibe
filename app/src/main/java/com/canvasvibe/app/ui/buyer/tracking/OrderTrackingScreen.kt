package com.canvasvibe.app.ui.buyer.tracking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
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
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private data class TimelineStep(val label: String, val time: String, val status: String)

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
        TrackingTopBar(onBack = onBack)
        when (val s = state) {
            is OrderTrackingUiState.Loading -> CenterText("Cargando…")
            is OrderTrackingUiState.Error -> CenterText(s.message)
            is OrderTrackingUiState.Ready -> TrackingContent(s.order)
        }
    }
}

@Composable
private fun TrackingTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Seguimiento en vivo",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Ayuda",
            color = PrimaryAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
private fun TrackingContent(order: Order) {
    val currentStatusIndex = STATUS_ORDER.indexOf(order.status).coerceAtLeast(0)
    val steps = listOf(
        TimelineStep("Pedido confirmado", formatTime(order.createdAt), "PENDING"),
        TimelineStep("En preparación", "—", "PREPARING"),
        TimelineStep("Recogido", "—", "SHIPPED"),
        TimelineStep("En camino", "—", "IN_TRANSIT"),
        TimelineStep("Entregado", "—", "DELIVERED")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderCard(order = order)
        MapPlaceholder()
        TimelineCard(steps = steps, currentIndex = currentStatusIndex)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OrderCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pedido",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "#${order.id.take(8).uppercase()}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusChip(status = order.status)
        }
        Text(
            text = order.productTitle.ifBlank { "Obra" },
            color = TextPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "DELIVERED" -> "Entregado" to Color(0xFF4CAF50)
        "SHIPPED", "IN_TRANSIT" -> "En camino" to Primary
        "PREPARING" -> "En preparación" to Color(0xFFFF9800)
        "CANCELLED" -> "Cancelado" to Color(0xFFF44336)
        else -> "Confirmado" to PrimaryAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D1F12), Color(0xFF1F3A24))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mapa en vivo",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TimelineCard(steps: List<TimelineStep>, currentIndex: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Actualizaciones",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        steps.forEachIndexed { index, step ->
            TimelineRow(
                step = step,
                isCompleted = index <= currentIndex,
                isLast = index == steps.lastIndex
            )
        }
    }
}

@Composable
private fun TimelineRow(step: TimelineStep, isCompleted: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) Primary else BorderSubtle)
            )
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                ) {
                    drawRect(
                        color = if (isCompleted) Primary.copy(alpha = 0.5f) else BorderSubtle,
                        topLeft = androidx.compose.ui.geometry.Offset.Zero,
                        size = size
                    )
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(
                text = step.label,
                color = if (isCompleted) TextPrimary else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = step.time,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = TextSecondary, fontSize = 13.sp)
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0L) return "—"
    val date = java.util.Date(millis)
    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return fmt.format(date)
}
