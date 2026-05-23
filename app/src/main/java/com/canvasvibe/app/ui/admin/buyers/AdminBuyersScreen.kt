package com.canvasvibe.app.ui.admin.buyers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.ui.admin.components.AdminBottomNav
import com.canvasvibe.app.ui.admin.components.ConfirmDeleteDialog
import com.canvasvibe.app.ui.admin.components.EditUserDialog
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
fun AdminBuyersScreen(
    onBack: () -> Unit,
    onDashboardClick: () -> Unit = onBack,
    onArtistsClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    val vm: AdminBuyersViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val visible = state.buyers.applyFilters(state.tab, state.query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(total = state.buyers.size)
        state.errorMessage?.let { ErrorBanner(it) { vm.dismissError() } }
        SearchBox(state.query, vm::setQuery)
        TabsRow(state.tab, vm::selectTab)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.isLoading) {
                item { Text("Cargando compradores…", color = TextSecondary, fontSize = 13.sp) }
            } else if (visible.isEmpty()) {
                item { EmptyHint(state.tab) }
            } else {
                items(visible, key = { it.uid }) { buyer ->
                    BuyerCard(
                        buyer = buyer,
                        onPrimary = {
                            val next = if (buyer.status == BuyerStatus.ACTIVO)
                                BuyerStatus.BLOQUEADO else BuyerStatus.ACTIVO
                            vm.changeStatus(buyer.uid, next)
                        },
                        onEdit   = { vm.startEdit(buyer) },
                        onDelete = { vm.startDelete(buyer) }
                    )
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        AdminBottomNav(
            selectedIndex = 2,
            onSelect = { ix ->
                when (ix) {
                    0 -> onDashboardClick()
                    1 -> onArtistsClick()
                    3 -> onCategoriesClick()
                    4 -> onReportsClick()
                    else -> {}
                }
            }
        )
    }

    state.editingBuyer?.let { buyer ->
        EditUserDialog(
            initialName = buyer.name,
            initialRole = "ROLE_BUYER",
            showRoleSelector = true,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = { vm.cancelEdit() },
            onSave = { name, role -> vm.saveEdit(name, role) }
        )
    }

    state.deletingBuyer?.let { buyer ->
        ConfirmDeleteDialog(
            userName = buyer.name,
            isDeleting = state.isDeleting,
            onDismiss = { vm.cancelDelete() },
            onConfirm = { vm.confirmDelete() }
        )
    }
}

@Composable
private fun TopBar(total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Compradores", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary.copy(alpha = 0.18f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$total registrados",
                color = PrimaryAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
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
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar",
                tint = Color(0xFFFF8A80),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SearchBox(query: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
            cursorBrush = SolidColor(Primary),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Buscar por nombre o email…", color = TextSecondary, fontSize = 13.sp)
                }
                inner()
            }
        )
    }
}

@Composable
private fun TabsRow(selected: BuyerTab, onSelect: (BuyerTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BuyerTab.values().forEach { tab ->
            Pill(
                label = tab.label(),
                selected = selected == tab,
                onClick = { onSelect(tab) }
            )
        }
    }
}

private fun BuyerTab.label(): String = when (this) {
    BuyerTab.TODOS      -> "Todos"
    BuyerTab.ACTIVOS    -> "Activos"
    BuyerTab.BLOQUEADOS -> "Bloqueados"
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Primary else Color.Transparent
    val border = if (selected) Primary else BorderSubtle
    val fg = if (selected) Color.White else TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BuyerCard(
    buyer: AdminBuyer,
    onPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBlocked = buyer.status == BuyerStatus.BLOQUEADO
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                initial = buyer.name.firstOrNull()?.uppercaseChar()?.toString() ?: "C",
                muted = isBlocked
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buyer.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (buyer.email.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buyer.email,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatChip(icon = Icons.Filled.CheckCircle, label = "${buyer.orderCount} pedidos")
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = formatRevenue(buyer.totalSpentCop),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            StatusBadge(buyer.status)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlineAction(
                label = if (isBlocked) "Desbloquear" else "Bloquear",
                color = if (isBlocked) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f),
                icon = if (isBlocked) Icons.Filled.CheckCircle else Icons.Filled.Block,
                onClick = onPrimary
            )
            IconAction(icon = Icons.Filled.Edit, color = Primary, onClick = onEdit)
            IconAction(icon = Icons.Filled.Delete, color = Color(0xFFCF6679), onClick = onDelete)
        }
    }
}

@Composable
private fun IconAction(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Primary.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryAccent,
            modifier = Modifier.size(11.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(label, color = PrimaryAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(status: BuyerStatus) {
    val (label, color) = when (status) {
        BuyerStatus.ACTIVO     -> "Activo"     to Color(0xFF4CAF50)
        BuyerStatus.BLOQUEADO  -> "Bloqueado"  to Color(0xFFF44336)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlineAction(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.size(4.dp))
        }
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Avatar(initial: String, muted: Boolean) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                if (muted) Brush.linearGradient(0f to TextSecondary.copy(alpha = 0.3f), 1f to TextSecondary.copy(alpha = 0.18f))
                else Brush.linearGradient(0f to Primary, 1f to PrimaryAccent)
            )
            .border(2.dp, TextPrimary.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyHint(tab: BuyerTab) {
    val text = when (tab) {
        BuyerTab.TODOS      -> "Aún no hay compradores registrados"
        BuyerTab.ACTIVOS    -> "Sin compradores activos"
        BuyerTab.BLOQUEADOS -> "No hay compradores bloqueados"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}

private fun formatRevenue(value: Long): String {
    return when {
        value >= 1_000_000 -> "$" + "%.1f".format(value / 1_000_000.0) + "M"
        value >= 1_000     -> "$" + "%.0f".format(value / 1_000.0) + "K"
        value > 0          -> "$" + NumberFormat.getInstance(Locale.forLanguageTag("es-CO")).format(value)
        else               -> "Sin compras"
    }
}
