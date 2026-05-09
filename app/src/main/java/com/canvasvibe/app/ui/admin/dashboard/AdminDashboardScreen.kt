package com.canvasvibe.app.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.ui.admin.components.AdminBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    onArtistClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onReportsClick: () -> Unit,
    onBuyersClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val vm: AdminDashboardViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AdminTopBar(onLogout = onLogout)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { KpiGrid(state) }
            item { WeeklySalesCard() }
            item {
                Text(
                    text = "Accesos rápidos",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            item {
                ShortcutsGrid(
                    onArtistClick = onArtistClick,
                    onCategoriesClick = onCategoriesClick,
                    onReportsClick = onReportsClick
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { LogoutButton(onLogout) }
            item { Spacer(Modifier.height(4.dp)) }
        }

        AdminBottomNav(
            selectedIndex = 0,
            onSelect = { ix ->
                when (ix) {
                    1 -> onArtistClick()
                    2 -> onBuyersClick()
                    3 -> onCategoriesClick()
                    4 -> onReportsClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Panel de Administración",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun KpiGrid(state: AdminDashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiCell(
                value = formatCount(state.totalUsers),
                title = "Usuarios totales",
                subtitle = "↑ 87 este mes",
                accent = Primary,
                modifier = Modifier.weight(1f)
            )
            KpiCell(
                value = formatCop(state.totalSalesCop),
                title = "Ventas totales",
                subtitle = "↑ 23%",
                accent = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiCell(
                value = formatCount(state.activeOrders),
                title = "Pedidos activos",
                subtitle = "En proceso",
                accent = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            KpiCell(
                value = formatCount(state.verifiedArtists),
                title = "Artistas verificados",
                subtitle = "12 pendientes",
                accent = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCell(
    value: String,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun WeeklySalesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp)
    ) {
        Text("Ventas semanales", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        BarChartPlaceholder(
            values = listOf(0.45f, 0.30f, 0.55f, 0.65f, 1.0f, 0.50f, 0.40f),
            labels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        )
    }
}

@Composable
private fun BarChartPlaceholder(values: List<Float>, labels: List<String>) {
    val maxValue = values.maxOrNull() ?: 1f
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { ix, value ->
                val pct = (value / maxValue).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height((120 * pct).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (ix == values.indexOf(maxValue)) Primary else PrimaryAccent.copy(alpha = 0.6f))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(it, color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ShortcutsGrid(
    onArtistClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCell(
                label = "Gestionar Artistas",
                icon = Icons.Filled.Palette,
                modifier = Modifier.weight(1f),
                onClick = onArtistClick
            )
            ShortcutCell(
                label = "Ver Pedidos",
                icon = Icons.AutoMirrored.Filled.ListAlt,
                modifier = Modifier.weight(1f),
                onClick = onReportsClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCell(
                label = "Categorías",
                icon = Icons.Filled.Category,
                modifier = Modifier.weight(1f),
                onClick = onCategoriesClick
            )
            ShortcutCell(
                label = "Reportes",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                modifier = Modifier.weight(1f),
                onClick = onReportsClick
            )
        }
    }
}

@Composable
private fun ShortcutCell(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceDark)
            .border(1.dp, Color(0xFFF44336), RoundedCornerShape(999.dp))
            .clickable { onLogout() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cerrar sesión",
            color = Color(0xFFF44336),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCount(value: Int): String {
    val nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"))
    return nf.format(value)
}

private fun formatCop(value: Long): String {
    return when {
        value >= 1_000_000_000 -> "$" + "%.1f".format(value / 1_000_000_000.0) + "B"
        value >= 1_000_000     -> "$" + "%.1f".format(value / 1_000_000.0) + "M"
        value >= 1_000         -> "$" + "%.1f".format(value / 1_000.0) + "K"
        else                   -> "$$value"
    }
}
