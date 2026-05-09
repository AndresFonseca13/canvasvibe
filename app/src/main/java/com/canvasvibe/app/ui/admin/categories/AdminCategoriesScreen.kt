package com.canvasvibe.app.ui.admin.categories

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.canvasvibe.app.data.model.Category
import com.canvasvibe.app.ui.admin.components.AdminBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun AdminCategoriesScreen(
    onBack: () -> Unit,
    onDashboardClick: () -> Unit = onBack,
    onArtistsClick: () -> Unit = {},
    onBuyersClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    val vm: AdminCategoriesViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(onAddClick = { vm.showCreate(true) })

        state.errorMessage?.let { msg ->
            ErrorBanner(message = msg, onDismiss = { vm.dismissError() })
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.isLoading) {
                    item { Text("Cargando categorías…", color = TextSecondary, fontSize = 13.sp) }
                } else if (state.categories.isEmpty()) {
                    item {
                        EmptyHint(
                            isSeeding = state.isSeeding,
                            onSeed = { vm.seedDefaultsManual() }
                        )
                    }
                } else {
                    items(state.categories, key = { it.id }) { row ->
                        CategoryItem(
                            row = row,
                            onToggle = { active -> vm.toggle(row, active) },
                            onDelete = { vm.delete(row) }
                        )
                    }
                    item {
                        SeedAgainButton(
                            isSeeding = state.isSeeding,
                            onSeed = { vm.seedDefaultsManual() }
                        )
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }

            FloatingAddButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { vm.showCreate(true) }
            )
        }

        AdminBottomNav(
            selectedIndex = 3,
            onSelect = { ix ->
                when (ix) {
                    0 -> onDashboardClick()
                    1 -> onArtistsClick()
                    2 -> onBuyersClick()
                    4 -> onReportsClick()
                    else -> {}
                }
            }
        )
    }

    if (state.showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { vm.showCreate(false) },
            onCreate = { name, emoji, color -> vm.create(name, emoji, color) }
        )
    }
}

@Composable
private fun TopBar(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Categorías", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { onAddClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "+ Nueva categoría",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryItem(
    row: Category,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.DragIndicator,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(parseHexColor(row.colorHex))
        )
        Spacer(Modifier.size(10.dp))
        Text(text = row.emoji.ifBlank { "🎨" }, fontSize = 16.sp)
        Spacer(Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(row.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("(${row.productCount} productos)", color = TextSecondary, fontSize = 11.sp)
        }
        StatusBadge(row.isActive)
        Spacer(Modifier.size(8.dp))
        Switch(
            checked = row.isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = Primary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceDark,
                uncheckedBorderColor = BorderSubtle
            )
        )
        Spacer(Modifier.size(4.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF44336).copy(alpha = 0.14f))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar",
                tint = Color(0xFFF44336),
                modifier = Modifier.size(14.dp)
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
private fun EmptyHint(isSeeding: Boolean, onSeed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Aún no hay categorías",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Crea las 5 por defecto (Gamer, Paisajes, Animales, Anime, Abstracto) o agrega una nueva con el +",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary)
                .clickable(enabled = !isSeeding, onClick = onSeed),
            contentAlignment = Alignment.Center
        ) {
            if (isSeeding) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    "Cargar categorías por defecto",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SeedAgainButton(isSeeding: Boolean, onSeed: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(enabled = !isSeeding, onClick = onSeed)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSeeding) {
            CircularProgressIndicator(
                color = Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.size(6.dp))
        Text(
            "Restaurar 5 categorías por defecto",
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(active: Boolean) {
    val (label, color) = if (active) "ON" to Color(0xFF4CAF50) else "OFF" to TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FloatingAddButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Primary)
            .border(2.dp, Primary.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Agregar categoría",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, emoji: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎨") }
    var color by remember { mutableStateOf("#7C4DFF") }
    val colorOptions = listOf("#7C4DFF", "#4CAF50", "#FF9800", "#F44336", "#2196F3", "#E91E63", "#00BCD4")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Nueva categoría", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DialogField(label = "Nombre", value = name, onChange = { name = it })
                DialogField(label = "Emoji (opcional)", value = emoji, onChange = { emoji = it })
                Text("Color", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val selected = hex == color
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    if (selected) 2.dp else 0.dp,
                                    if (selected) TextPrimary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { color = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, emoji, color) }) {
                Text("Crear", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DialogField(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Background)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return runCatching {
        val clean = hex.removePrefix("#")
        Color(android.graphics.Color.parseColor("#$clean"))
    }.getOrDefault(Primary)
}
