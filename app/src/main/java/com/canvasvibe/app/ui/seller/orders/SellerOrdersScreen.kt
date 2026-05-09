package com.canvasvibe.app.ui.seller.orders

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.ui.seller.components.SellerBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellerOrdersScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onProductsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val vm: SellerOrdersViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val visible = state.orders.byTab(state.tab)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        OrdersTopBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabPill(
                label = "Activos",
                selected = state.tab == OrdersTab.ACTIVOS,
                onClick = { vm.selectTab(OrdersTab.ACTIVOS) }
            )
            TabPill(
                label = "Entregados",
                selected = state.tab == OrdersTab.ENTREGADOS,
                onClick = { vm.selectTab(OrdersTab.ENTREGADOS) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (visible.isEmpty()) {
                item { EmptyOrders(state.tab) }
            } else {
                items(visible, key = { it.id }) { order ->
                    OrderCard(order = order, onAdvance = { vm.advanceStatus(order) })
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
            item { RouteMapPlaceholder() }
            item { Spacer(Modifier.height(4.dp)) }
        }

        SellerBottomNav(
            selectedIndex = 2,
            onSelect = { ix ->
                when (ix) {
                    0 -> onHomeClick()
                    1 -> onProductsClick()
                    3 -> onProfileClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun OrdersTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pedidos",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Filtrar",
            color = PrimaryAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Primary else Color.Transparent
    val fg = if (selected) Color.White else TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderCard(order: Order, onAdvance: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${order.id.takeLast(5).uppercase()} · ${formatCop(order.totalPrice)}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "Cliente: ${order.buyerName.ifBlank { "—" }}",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = "Timeline: ${timelineText(order.status)}",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(4.dp))
        AdvanceButton(order.status, onAdvance)
    }
}

@Composable
private fun AdvanceButton(status: String, onAdvance: () -> Unit) {
    val label = when (status) {
        "PENDING"   -> "Confirmar pedido"
        "PREPARING" -> "Marcar como enviado"
        "SHIPPED"   -> "Marcar como entregado"
        "DELIVERED" -> "Entregado"
        "CANCELLED" -> "Cancelado"
        else        -> "Actualizar estado"
    }
    val terminal = status == "DELIVERED" || status == "CANCELLED"
    val bg = if (terminal) SurfaceDark else Primary.copy(alpha = 0.18f)
    val fg = if (terminal) TextSecondary else Primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .let { if (terminal) it else it.clickable { onAdvance() } }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (!terminal) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RouteMapPlaceholder() {
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
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text("Mapa de rutas del día", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Placeholder estilizado", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EmptyOrders(tab: OrdersTab) {
    val (title, sub) = when (tab) {
        OrdersTab.ACTIVOS    -> "Sin pedidos activos" to "Cuando recibas un pedido aparecerá aquí"
        OrdersTab.ENTREGADOS -> "Sin pedidos entregados" to "Aún no has cerrado ningún pedido"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(sub, color = TextSecondary, fontSize = 12.sp)
    }
}

private fun timelineText(status: String): String = when (status) {
    "PENDING"   -> "Confirmado pendiente"
    "PREPARING" -> "Confirmado · En preparación"
    "SHIPPED"   -> "Empacado · Recogido · En ruta"
    "DELIVERED" -> "Empacado · Recogido · Entregado"
    "CANCELLED" -> "Cancelado"
    else        -> status
}

private fun formatCop(value: Long): String {
    val nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"))
    return "COP ${nf.format(value)}"
}
