package com.canvasvibe.app.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextSecondary

private data class AdminNavItem(val label: String, val icon: ImageVector)

@Composable
fun AdminBottomNav(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val items = listOf(
        AdminNavItem("Dashboard", Icons.Filled.Speed),
        AdminNavItem("Artistas", Icons.Filled.Palette),
        AdminNavItem("Compradores", Icons.Filled.Group),
        AdminNavItem("Categorías", Icons.Filled.Category),
        AdminNavItem("Reportes", Icons.AutoMirrored.Filled.ShowChart)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(SurfaceDark)
            .drawBehind {
                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
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
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) Primary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.label,
                    color = if (selected) Primary else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}
