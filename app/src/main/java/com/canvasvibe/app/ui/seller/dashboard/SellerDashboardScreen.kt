package com.canvasvibe.app.ui.seller.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.ui.seller.components.SellerBottomNav
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellerDashboardScreen(
    onOrdersClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onProductsClick: () -> Unit = onAddProductClick,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val vm: SellerDashboardViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.canvasvibe.app.ui.theme.Background)
    ) {
        DashboardTopBar(onLogout = onLogout)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Hola, ${state.sellerName}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            item { SalesTodayCard(state) }
            item { WeeklyKpisCard() }
            item { ActivityCard(state) }
            item {
                Text(
                    text = "Pedidos recientes",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            if (state.recentOrders.isEmpty()) {
                item { EmptyOrdersHint(onAddProductClick) }
            } else {
                items(state.recentOrders, key = { it.id }) { order ->
                    RecentOrderRow(order = order, onClick = onOrdersClick)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { PrimaryActionButton("Publicar nuevo producto", onAddProductClick) }
            item { Spacer(Modifier.height(4.dp)) }
        }

        SellerBottomNav(
            selectedIndex = 0,
            onSelect = { index ->
                when (index) {
                    1 -> onProductsClick()
                    2 -> onOrdersClick()
                    3 -> onProfileClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun DashboardTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dashboard",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hoy",
            color = PrimaryAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SalesTodayCard(state: SellerDashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Ventas de hoy",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatCop(state.salesTodayCop),
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pedidos: ${state.ordersToday}",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "Productos activos: ${state.activeProducts}",
                color = PrimaryAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WeeklyKpisCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "KPIs semanales",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Placeholder de gráfica",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ActivityCard(state: SellerDashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Actividad",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        ActivityRow(
            icon = Icons.Filled.LocalShipping,
            text = "${state.readyToShip} pedidos listos para despacho"
        )
        ActivityRow(
            icon = Icons.Filled.Schedule,
            text = "${state.pendingPayments} pagos por confirmar"
        )
    }
}

@Composable
private fun ActivityRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.size(10.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun RecentOrderRow(order: Order, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory2,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = order.productTitle.ifBlank { "Pedido" },
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${order.material} · ${order.size}",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        StatusPill(order.status)
    }
}

@Composable
private fun StatusPill(status: String) {
    val (label, color) = when (status) {
        "PENDING"    -> "Pendiente"  to Color(0xFFFF9800)
        "PREPARING"  -> "Preparando" to Primary
        "SHIPPED"    -> "Enviado"    to Color(0xFF2196F3)
        "DELIVERED"  -> "Entregado"  to Color(0xFF4CAF50)
        "CANCELLED"  -> "Cancelado"  to Color(0xFFF44336)
        else         -> status       to TextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyOrdersHint(onAddProductClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.AddShoppingCart,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Aún no tienes pedidos",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Publica tu primera obra para empezar a vender",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PrimaryActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatCop(value: Long): String {
    val nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"))
    return "COP ${nf.format(value)}"
}
