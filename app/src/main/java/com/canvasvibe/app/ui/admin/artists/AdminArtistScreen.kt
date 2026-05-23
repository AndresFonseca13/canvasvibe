package com.canvasvibe.app.ui.admin.artists

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminArtistScreen(
    onBack: () -> Unit,
    onDashboardClick: () -> Unit = onBack,
    onBuyersClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    val vm: AdminArtistViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val visible = state.artists.applyFilters(state.tab, state.query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar()
        SearchBox(state.query, vm::setQuery)
        TabsRow(state.tab, vm::selectTab)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (visible.isEmpty()) {
                item { EmptyHint() }
            } else {
                items(visible, key = { it.uid }) { artist ->
                    ArtistCard(
                        artist = artist,
                        onPrimary = {
                            val next = when (artist.status) {
                                ArtistStatus.PENDIENTE   -> ArtistStatus.VERIFICADO
                                ArtistStatus.VERIFICADO  -> ArtistStatus.SUSPENDIDO
                                ArtistStatus.SUSPENDIDO  -> ArtistStatus.VERIFICADO
                            }
                            vm.changeStatus(artist.uid, next)
                        },
                        onEdit   = { vm.startEdit(artist) },
                        onDelete = { vm.startDelete(artist) }
                    )
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        AdminBottomNav(
            selectedIndex = 1,
            onSelect = { ix ->
                when (ix) {
                    0 -> onDashboardClick()
                    2 -> onBuyersClick()
                    3 -> onCategoriesClick()
                    4 -> onReportsClick()
                    else -> {}
                }
            }
        )
    }

    state.editingArtist?.let { artist ->
        EditUserDialog(
            initialName = artist.name,
            initialRole = "ROLE_SELLER",
            showRoleSelector = true,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = { vm.cancelEdit() },
            onSave = { name, role -> vm.saveEdit(name, role) }
        )
    }

    state.deletingArtist?.let { artist ->
        ConfirmDeleteDialog(
            userName = artist.name,
            isDeleting = state.isDeleting,
            onDismiss = { vm.cancelDelete() },
            onConfirm = { vm.confirmDelete() }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Artistas", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                    Text("Buscar artista…", color = TextSecondary, fontSize = 13.sp)
                }
                inner()
            }
        )
    }
}

@Composable
private fun TabsRow(selected: ArtistTab, onSelect: (ArtistTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArtistTab.values().forEach { tab ->
            Pill(
                label = tab.label(),
                selected = selected == tab,
                onClick = { onSelect(tab) }
            )
        }
    }
}

private fun ArtistTab.label(): String = when (this) {
    ArtistTab.TODOS        -> "Todos"
    ArtistTab.VERIFICADOS  -> "Verificados"
    ArtistTab.PENDIENTES   -> "Pendientes"
    ArtistTab.SUSPENDIDOS  -> "Suspendidos"
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
private fun ArtistCard(
    artist: AdminArtist,
    onPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initial = artist.name.firstOrNull()?.uppercaseChar()?.toString() ?: "A")
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = artist.specialty.replaceFirstChar { it.uppercase() },
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "%.1f".format(artist.rating),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "${artist.sales} ventas",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = formatRevenue(artist.totalRevenueCop),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            StatusBadge(artist.status)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val (label, color) = when (artist.status) {
                ArtistStatus.PENDIENTE  -> "Verificar" to Color(0xFF4CAF50)
                ArtistStatus.VERIFICADO -> "Suspender" to Color(0xFFF44336)
                ArtistStatus.SUSPENDIDO -> "Reactivar" to Color(0xFF4CAF50)
            }
            OutlineAction(label = label, color = color, modifier = Modifier.weight(1f), onClick = onPrimary)
            IconAction(icon = Icons.Filled.Edit, color = Primary, onClick = onEdit)
            IconAction(icon = Icons.Filled.Delete, color = Color(0xFFCF6679), onClick = onDelete)
        }
    }
}

@Composable
private fun IconAction(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
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
private fun OutlineAction(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(status: ArtistStatus) {
    val (label, color) = when (status) {
        ArtistStatus.VERIFICADO -> "Verificado" to Color(0xFF4CAF50)
        ArtistStatus.PENDIENTE  -> "Pendiente" to Color(0xFFFF9800)
        ArtistStatus.SUSPENDIDO -> "Suspendido" to Color(0xFFF44336)
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
private fun Avatar(initial: String) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.22f))
            .border(2.dp, Primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Sin artistas para mostrar", color = TextSecondary, fontSize = 12.sp)
    }
}

private fun formatRevenue(value: Long): String {
    return when {
        value >= 1_000_000 -> "$" + "%.1f".format(value / 1_000_000.0) + "M"
        value >= 1_000     -> "$" + "%.0f".format(value / 1_000.0) + "K"
        value > 0          -> "$" + NumberFormat.getInstance(Locale.forLanguageTag("es-CO")).format(value)
        else               -> ""
    }
}
