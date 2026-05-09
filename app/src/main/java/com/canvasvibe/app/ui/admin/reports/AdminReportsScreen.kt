package com.canvasvibe.app.ui.admin.reports

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.ui.admin.components.AdminBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminReportsScreen(
    onBack: () -> Unit,
    onDashboardClick: () -> Unit = onBack,
    onArtistsClick: () -> Unit = {},
    onBuyersClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {}
) {
    val vm: AdminReportsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar()
        state.errorMessage?.let { ErrorBanner(it) { vm.dismissError() } }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PeriodFilter(
                    selected = state.period,
                    onSelect = { vm.setPeriod(it) }
                )
            }
            item { SalesCard(state) }
            item { LineChartCard(state) }
            item { TopProductsCard(state) }
            item { TopArtistsCard(state) }
            item { ExportRow() }
            item { Spacer(Modifier.height(4.dp)) }
        }

        AdminBottomNav(
            selectedIndex = 4,
            onSelect = { ix ->
                when (ix) {
                    0 -> onDashboardClick()
                    1 -> onArtistsClick()
                    2 -> onBuyersClick()
                    3 -> onCategoriesClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Reportes", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF44336).copy(alpha = 0.16f))
            .border(1.dp, Color(0xFFF44336).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = Color(0xFFFF8A80),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PeriodFilter(selected: ReportPeriod, onSelect: (ReportPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportPeriod.values().forEach { p ->
            val isSelected = p == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) Primary else SurfaceDark)
                    .border(
                        1.dp,
                        if (isSelected) Primary else BorderSubtle,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelect(p) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = p.label(),
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SalesCard(state: AdminReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = formatCop(state.totalCop) + " COP",
            color = Primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        VariationLine(state)
    }
}

@Composable
private fun VariationLine(state: AdminReportsUiState) {
    if (!state.previousAvailable) {
        Text(
            text = "Sin datos del ${state.previousLabel}",
            color = TextSecondary,
            fontSize = 12.sp
        )
        return
    }
    val pct = state.variationPct
    val (arrow, color) = when {
        pct > 0  -> "↑" to Color(0xFF4CAF50)
        pct < 0  -> "↓" to Color(0xFFF44336)
        else     -> "→" to TextSecondary
    }
    Text(
        text = "$arrow ${"%.1f".format(kotlin.math.abs(pct))}% vs ${state.previousLabel}",
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun LineChartCard(state: AdminReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = chartTitle(state.period),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        if (state.chartPoints.isEmpty() || state.chartPoints.all { it.valueCop == 0L }) {
            EmptyChartHint()
        } else {
            SalesLineChart(values = state.chartPoints.map { it.valueCop })
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                state.chartPoints.forEach {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = it.label,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun chartTitle(period: ReportPeriod): String = when (period) {
    ReportPeriod.HOY    -> "Ventas por franja horaria"
    ReportPeriod.SEMANA -> "Ventas por día"
    ReportPeriod.MES    -> "Ventas por semana"
    ReportPeriod.ANIO   -> "Ventas por mes"
}

@Composable
private fun EmptyChartHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Sin ventas en este periodo", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SalesLineChart(values: List<Long>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = BorderSubtle
            val rows = 4
            val rowGap = size.height / rows
            repeat(rows + 1) { i ->
                val y = i * rowGap
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }
            if (values.isNotEmpty()) {
                val maxV = (values.max().takeIf { it > 0L } ?: 1L).toFloat()
                val stepX = size.width / (values.size - 1).coerceAtLeast(1)
                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v.toFloat() / maxV) * size.height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = Primary, style = Stroke(width = 4f))
                values.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v.toFloat() / maxV) * size.height
                    drawCircle(color = Primary, radius = 5f, center = Offset(x, y))
                }
            }
        }
    }
}

@Composable
private fun TopProductsCard(state: AdminReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Top 3 productos más vendidos",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (state.topProducts.isEmpty()) {
            EmptyRow("Sin ventas en este periodo")
        } else {
            state.topProducts.forEachIndexed { ix, p ->
                TopRow(
                    rank = ix + 1,
                    label = p.title,
                    mid = "${p.units} u",
                    right = formatCop(p.revenueCop)
                )
            }
        }
    }
}

@Composable
private fun TopArtistsCard(state: AdminReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Top 3 artistas",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (state.topArtists.isEmpty()) {
            EmptyRow("Sin ventas en este periodo")
        } else {
            state.topArtists.forEach { a ->
                TopRow(
                    label = "● ${a.name}",
                    mid = formatCop(a.revenueCop),
                    right = if (a.rating > 0) "★ ${"%.1f".format(a.rating)}" else "Sin reseñas"
                )
            }
        }
    }
}

@Composable
private fun TopRow(rank: Int? = null, label: String, mid: String, right: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (rank != null) {
                Text(
                    text = "$rank.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.size(6.dp))
            }
            Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
        Text(mid, color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.size(8.dp))
        Text(right, color = PrimaryAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun ExportRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExportButton(
            label = "Exportar PDF",
            icon = Icons.Filled.PictureAsPdf,
            modifier = Modifier.weight(1f)
        )
        ExportButton(
            label = "Exportar CSV",
            icon = Icons.Filled.TableChart,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExportButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .border(1.dp, Primary, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(6.dp))
        Text(label, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatCop(value: Long): String {
    val nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"))
    return "$" + nf.format(value)
}
