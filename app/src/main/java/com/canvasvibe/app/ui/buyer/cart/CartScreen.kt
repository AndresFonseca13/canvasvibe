package com.canvasvibe.app.ui.buyer.cart

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.ui.buyer.home.formatCop
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private const val SHIPPING_COP = 15000L

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val placedOrderId by viewModel.checkoutOrderId.collectAsStateWithLifecycle()

    LaunchedEffect(placedOrderId) {
        placedOrderId?.let {
            onOrderPlaced(it)
            viewModel.clearCheckoutFlag()
        }
    }

    val subtotal = items.sumOf { it.unitPrice * it.quantity }
    val discount = 0L
    val total = subtotal + SHIPPING_COP - discount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CartTopBar(count = items.size, onBack = onBack)

        if (items.isEmpty()) {
            EmptyCart()
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    CartItemCard(
                        item = item,
                        onDecrement = {
                            viewModel.updateQuantity(item.productId, item.quantity - 1)
                        },
                        onIncrement = {
                            viewModel.updateQuantity(item.productId, item.quantity + 1)
                        }
                    )
                }
                Spacer(Modifier.height(6.dp))
                SummaryCard(
                    subtotal = subtotal,
                    shipping = SHIPPING_COP,
                    discount = discount,
                    total = total
                )
                Spacer(Modifier.height(14.dp))
                PayButton(onClick = { viewModel.checkout() })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CartTopBar(count: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "Mi Carrito",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (count == 1) "1 ítem" else "$count ítems",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(colors = listOf(SurfaceDark, PrimaryAccent))
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.ifBlank { "Obra" },
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.sellerName.ifBlank { "Artista" },
                color = TextSecondary,
                fontSize = 12.sp
            )
            if (item.size.isNotBlank() || item.material.isNotBlank()) {
                Text(
                    text = listOf(item.material, item.size).filter { it.isNotBlank() }.joinToString(" · "),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatCop(item.unitPrice * item.quantity),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        QuantityControl(
            qty = item.quantity,
            onDecrement = onDecrement,
            onIncrement = onIncrement
        )
    }
}

@Composable
private fun QuantityControl(qty: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QuantityBtn("−", onDecrement)
        Text(
            text = qty.toString(),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        QuantityBtn("+", onIncrement)
    }
}

@Composable
private fun QuantityBtn(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryCard(subtotal: Long, shipping: Long, discount: Long, total: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryRow("Subtotal", formatCop(subtotal))
        SummaryRow("Envío", formatCop(shipping))
        if (discount > 0) SummaryRow("Descuento", "−${formatCop(discount)}")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSubtle)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatCop(total),
                color = Primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun PayButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Proceder al pago →",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyCart() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tu carrito está vacío",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}
