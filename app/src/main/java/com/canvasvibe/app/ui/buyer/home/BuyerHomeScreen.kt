package com.canvasvibe.app.ui.buyer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.ui.buyer.components.BuyerBottomNav
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private data class CategoryTab(val label: String, val firestoreKey: String?)

@Composable
fun BuyerHomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    userName: String = "",
    viewModel: BuyerHomeViewModel = viewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val dynamicCategories by viewModel.categories.collectAsStateWithLifecycle()

    val categoryTabs = remember(dynamicCategories) {
        buildList {
            add(CategoryTab("Destacados", null))
            dynamicCategories.forEach {
                add(CategoryTab("${it.emoji} ${it.name}".trim(), it.slug))
            }
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var categoryIx by remember { mutableStateOf(0) }

    LaunchedEffect(categoryTabs.size) {
        if (categoryIx >= categoryTabs.size) {
            categoryIx = 0
            viewModel.selectCategory(null)
        }
    }

    val filtered = remember(products, query) {
        if (query.isBlank()) products
        else products.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.sellerName.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "",
            onAvatarClick = onProfileClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SearchField(value = query, onValueChange = viewModel::setQuery)
            CategoryChips(
                items = categoryTabs.map { it.label },
                selectedIndex = categoryIx.coerceAtMost(categoryTabs.lastIndex.coerceAtLeast(0)),
                onSelect = { ix ->
                    categoryIx = ix
                    viewModel.selectCategory(categoryTabs[ix].firestoreKey)
                }
            )

            if (filtered.isEmpty()) {
                EmptyState()
            } else {
                filtered.forEachIndexed { index, product ->
                    ProductCard(
                        product = product,
                        big = index == 0,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        BuyerBottomNav(
            selectedIndex = selectedTab,
            onSelect = { ix ->
                selectedTab = ix
                when (ix) {
                    2 -> onCartClick()
                    3 -> onProfileClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun TopBar(initial: String, onAvatarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Inicio",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(1.dp, BorderSubtle, CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNotBlank()) {
                Text(
                    text = initial,
                    color = PrimaryAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(Primary),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = "Buscar arte, artistas, estilos",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (isSelected) Primary else SurfaceDark)
                    .then(
                        if (isSelected) Modifier
                        else Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(17.dp))
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    big: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val coverUrl = product.imageUrls.firstOrNull()
        if (big) FeaturedArtwork(coverUrl) else CompactArtwork(coverUrl)

        if (big) {
            Text(
                text = product.title.ifBlank { "Obra sin título" },
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${product.sellerName.ifBlank { "Artista" }}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = formatCop(product.priceBase),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.title.ifBlank { "Obra sin título" },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCop(product.priceBase),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeaturedArtwork(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(colors = listOf(BorderSubtle, Primary)))
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 28.dp)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(PrimaryAccent)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-8).dp)
                    .size(90.dp)
                    .border(20.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
        }
    }
}

@Composable
private fun CompactArtwork(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(colors = listOf(SurfaceDark, PrimaryAccent)))
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = 30.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.13f))
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aún no hay obras en esta categoría",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

internal fun formatCop(value: Long): String {
    if (value <= 0L) return "COP \$0"
    val digits = value.toString()
    val withSeparators = buildString {
        digits.reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "COP \$$withSeparators"
}
