package com.canvasvibe.app.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun AdminDashboardScreen(
    onArtistClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Dashboard Admin", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Próximamente: KPIs globales y reportes.", color = TextSecondary, fontSize = 13.sp)
        StubBtn("Artistas", onArtistClick)
        StubBtn("Categorías", onCategoriesClick)
        StubBtn("Reportes", onReportsClick)
    }
}

@Composable
private fun StubBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(SurfaceDark)
            .border(1.dp, Primary, RoundedCornerShape(26.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
