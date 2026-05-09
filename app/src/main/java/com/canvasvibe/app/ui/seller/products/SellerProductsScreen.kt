package com.canvasvibe.app.ui.seller.products

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.canvasvibe.app.data.model.Product
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
fun SellerProductsScreen(
    onAddProductClick: () -> Unit,
    onEditProduct: (String) -> Unit,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val vm: SellerProductsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ProductsTopBar(onAddProductClick)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                item { Text("Cargando…", color = TextSecondary, fontSize = 13.sp) }
            } else if (state.products.isEmpty()) {
                item { EmptyState(onAddProductClick) }
            } else {
                items(state.products, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        pending = state.pendingActionId == product.id,
                        onEdit = { onEditProduct(product.id) },
                        onToggleActive = { vm.toggleActive(product) },
                        onDelete = { deleteTarget = product },
                        onStockChange = { vm.updateStock(product, it) }
                    )
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        SellerBottomNav(
            selectedIndex = 1,
            onSelect = { ix ->
                when (ix) {
                    0 -> onHomeClick()
                    2 -> onOrdersClick()
                    3 -> onProfileClick()
                    else -> {}
                }
            }
        )
    }

    deleteTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar producto", color = TextPrimary) },
            text = {
                Text(
                    "¿Eliminar definitivamente \"${product.title}\"? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(product)
                    deleteTarget = null
                }) {
                    Text("Eliminar", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ProductsTopBar(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Productos", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { onAddClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text("Crear", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("Aún no has publicado obras", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Toca \"Crear\" para subir tu primera pieza",
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
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("Crear producto", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    pending: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onStockChange: (Int) -> Unit
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
            ProductThumb(url = product.imageUrls.firstOrNull())
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title.ifBlank { "Sin título" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatCop(product.priceBase),
                    color = PrimaryAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = product.category.replaceFirstChar { it.uppercase() },
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            ActiveBadge(product.isActive)
        }

        StockRow(stock = product.stock, onChange = onStockChange)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Filled.Edit,
                label = "Editar",
                color = Primary,
                modifier = Modifier.weight(1f),
                enabled = !pending,
                onClick = onEdit
            )
            ActionButton(
                icon = if (product.isActive) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                label = if (product.isActive) "Inhabilitar" else "Activar",
                color = PrimaryAccent,
                modifier = Modifier.weight(1f),
                enabled = !pending,
                onClick = onToggleActive
            )
            ActionButton(
                icon = Icons.Filled.Delete,
                label = "Borrar",
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f),
                enabled = !pending,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ProductThumb(url: String?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ActiveBadge(active: Boolean) {
    val (label, color) = if (active) "Activo" to Color(0xFF4CAF50) else "Oculto" to TextSecondary
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
private fun StockRow(stock: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Stock", color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StockButton(icon = Icons.Filled.Remove, onClick = { onChange(-1) })
            Spacer(Modifier.size(12.dp))
            Text(
                text = stock.toString(),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.size(12.dp))
            StockButton(icon = Icons.Filled.Add, onClick = { onChange(1) })
        }
    }
}

@Composable
private fun StockButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Primary.copy(alpha = 0.18f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatCop(value: Long): String {
    val nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"))
    return "COP ${nf.format(value)}"
}
