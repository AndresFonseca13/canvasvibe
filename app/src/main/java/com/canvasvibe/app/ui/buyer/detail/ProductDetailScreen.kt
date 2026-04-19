package com.canvasvibe.app.ui.buyer.detail

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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onCartClick: () -> Unit = {},
    viewModel: ProductDetailViewModel = viewModel()
) {
    LaunchedEffect(productId) { viewModel.load(productId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val material by viewModel.selectedMaterial.collectAsStateWithLifecycle()
    val size by viewModel.selectedSize.collectAsStateWithLifecycle()
    val added by viewModel.addedToCart.collectAsStateWithLifecycle()

    LaunchedEffect(added) {
        if (added) {
            onCartClick()
            viewModel.clearAddedFlag()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DetailTopBar(onBack = onBack)
        when (val s = state) {
            is ProductDetailUiState.Loading -> LoadingBox()
            is ProductDetailUiState.Error -> ErrorBox(s.message)
            is ProductDetailUiState.Ready -> DetailContent(
                product = s.product,
                selectedMaterial = material,
                selectedSize = size,
                onSelectMaterial = viewModel::selectMaterial,
                onSelectSize = viewModel::selectSize,
                onAddToCart = viewModel::addToCart
            )
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconPill(icon = {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }, onClick = onBack)

        Text(
            text = "Detalle",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconPill(icon = {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Opciones",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }, onClick = {})
    }
}

@Composable
private fun IconPill(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { icon() }
}

@Composable
private fun DetailContent(
    product: Product,
    selectedMaterial: String?,
    selectedSize: String?,
    onSelectMaterial: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onAddToCart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroImage()
        Text(
            text = product.title.ifBlank { "Obra sin título" },
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${product.sellerName.ifBlank { "Artista" }} · Edición única",
            color = TextSecondary,
            fontSize = 13.sp
        )
        RatingRow(rating = product.rating, reviews = product.reviewCount)

        if (product.description.isNotBlank()) {
            Text(
                text = product.description,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        if (product.materials.isNotEmpty()) {
            SectionTitle("Material")
            ChipRow(
                options = product.materials,
                selected = selectedMaterial,
                onSelect = onSelectMaterial
            )
        }

        if (product.sizes.isNotEmpty()) {
            SectionTitle("Tamaño")
            ChipRow(
                options = product.sizes,
                selected = selectedSize,
                onSelect = onSelectSize
            )
        }

        PriceRow(priceBase = product.priceBase)
        Spacer(Modifier.height(4.dp))
        ActionButtons(onAddToCart = onAddToCart)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeroImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SurfaceDark, Primary)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .padding(start = 28.dp, top = 32.dp)
                .size(74.dp)
                .clip(CircleShape)
                .background(PrimaryAccent)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 28.dp)
                .size(110.dp)
                .clip(CircleShape)
                .border(14.dp, Color.White.copy(alpha = 0.18f), CircleShape)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (isSelected) Primary else SurfaceDark)
                    .then(
                        if (isSelected) Modifier
                        else Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(17.dp))
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Double, reviews: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { index ->
            val filled = index < rating.toInt()
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (filled) Color(0xFFFFC107) else BorderSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (reviews > 0) "$rating ($reviews reseñas)" else "Sin reseñas",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PriceRow(priceBase: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Precio", color = TextSecondary, fontSize = 13.sp)
        Text(
            text = com.canvasvibe.app.ui.buyer.home.formatCop(priceBase),
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButtons(onAddToCart: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .border(2.dp, Primary, RoundedCornerShape(27.dp))
                .clickable { }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Favoritos",
                    color = Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1.4f)
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(Primary)
                .clickable { onAddToCart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Agregar al carrito",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun ErrorBox(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = TextSecondary, fontSize = 13.sp)
    }
}
