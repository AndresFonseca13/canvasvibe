package com.canvasvibe.app.ui.buyer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

data class FeaturedArt(
    val title: String,
    val author: String?,
    val price: String,
    val startColor: Color,
    val endColor: Color,
    val big: Boolean
)

private val demoFeatured = listOf(
    FeaturedArt(
        title = "Neón Urbano #12",
        author = "by Ana Rojas",
        price = "COP \$220.000",
        startColor = Color(0xFF2A2A2A),
        endColor = Primary,
        big = true
    ),
    FeaturedArt(
        title = "Aurora Geométrica",
        author = null,
        price = "COP \$145.000",
        startColor = Color(0xFF1F1F1F),
        endColor = PrimaryAccent,
        big = false
    )
)

@Composable
fun BuyerHomeScreen(userName: String = "") {
    var selectedTab by remember { mutableStateOf(0) }
    var search      by remember { mutableStateOf("") }
    var categoryIx  by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SearchField(value = search, onValueChange = { search = it })
            CategoryChips(
                items = listOf("Destacados", "Ilustración", "3D", "Pintura"),
                selectedIndex = categoryIx,
                onSelect = { categoryIx = it }
            )
            demoFeatured.forEach { art -> FeaturedCard(art) }
            Spacer(Modifier.height(8.dp))
        }

        BuyerBottomNav(
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it }
        )
    }
}

@Composable
private fun TopBar(initial: String) {
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
                .border(1.dp, BorderSubtle, CircleShape),
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
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
private fun FeaturedCard(art: FeaturedArt) {
    val imageHeight = if (art.big) 180.dp else 150.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(art.startColor, art.endColor)
                    )
                )
        ) {
            if (art.big) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 28.dp)
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(PrimaryAccent)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 24.dp)
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(12.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 30.dp, top = 30.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 30.dp, top = 35.dp)
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.13f))
                )
            }
        }

        Text(
            text = art.title,
            color = TextPrimary,
            fontSize = if (art.big) 18.sp else 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (art.author != null) {
                Text(text = art.author, color = TextSecondary, fontSize = 13.sp)
            } else {
                Spacer(Modifier.width(0.dp))
            }
            Text(
                text = art.price,
                color = TextPrimary,
                fontSize = if (art.big) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class NavItem(
    val label: String,
    val iconActive: ImageVector,
    val iconInactive: ImageVector
)

@Composable
private fun BuyerBottomNav(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem("Inicio",   Icons.Filled.Home,         Icons.Filled.Home),
        NavItem("Explorar", Icons.Filled.Explore,      Icons.Filled.Explore),
        NavItem("Carrito",  Icons.Filled.ShoppingCart, Icons.Filled.ShoppingCart),
        NavItem("Perfil",   Icons.Filled.Person,       Icons.Filled.Person)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SurfaceDark)
            .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(0.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Primary.copy(alpha = 0.125f) else Color.Transparent)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (selected) item.iconActive else item.iconInactive,
                    contentDescription = item.label,
                    tint = if (selected) Primary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.label,
                    color = if (selected) Primary else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
