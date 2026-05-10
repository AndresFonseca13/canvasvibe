package com.canvasvibe.app.ui.buyer.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.ui.buyer.components.BuyerBottomNav
import com.canvasvibe.app.ui.buyer.home.formatCop
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private const val COMMISSION_COP = 10000L

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckoutClick: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onProfileClick: () -> Unit = {},
    viewModel: CartViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    val subtotal = items.sumOf { it.unitPrice * it.quantity }
    val total = subtotal + COMMISSION_COP

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CartTopBar(count = items.size)

        if (items.isEmpty()) {
            EmptyCart(modifier = Modifier.weight(1f))
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, item ->
                    CartItemCard(
                        item = item,
                        thumbVariant = index % 2,
                        onDecrement = {
                            viewModel.updateQuantity(item.productId, item.quantity - 1)
                        },
                        onIncrement = {
                            viewModel.updateQuantity(item.productId, item.quantity + 1)
                        }
                    )
                }
                SummaryCard(
                    subtotal = subtotal,
                    commission = COMMISSION_COP,
                    total = total
                )
                PayButton(onClick = onCheckoutClick)
            }
        }

        BuyerBottomNav(
            selectedIndex = 2,
            onSelect = { ix ->
                when (ix) {
                    0, 1 -> onHomeClick()
                    3    -> onProfileClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun CartTopBar(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Carrito",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (count == 1) "1 ítem" else "$count ítems",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    thumbVariant: Int,
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
        CartThumb(variant = thumbVariant, imageUrl = item.imageUrl)
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
                text = listOf(item.material, item.size)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { "Licencia personal" },
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatCop(item.unitPrice * item.quantity),
                color = TextPrimary,
                fontSize = 15.sp,
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
private fun CartThumb(variant: Int, imageUrl: String?) {
    Box(
        modifier = Modifier
            .size(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (variant == 0)
                    Brush.linearGradient(colors = listOf(BorderSubtle, Primary))
                else
                    Brush.verticalGradient(colors = listOf(BorderSubtle, PrimaryAccent))
            )
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
            )
        } else if (variant == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(PrimaryAccent)
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 26.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary)
            )
        }
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
private fun SummaryCard(subtotal: Long, commission: Long, total: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryRow("Subtotal", formatCop(subtotal))
        SummaryRow("Comisión", formatCop(commission))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatCop(total),
                color = TextPrimary,
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
            text = "Pagar ahora",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyCart(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
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
