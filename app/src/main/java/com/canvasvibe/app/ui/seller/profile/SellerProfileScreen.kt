package com.canvasvibe.app.ui.seller.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.ui.seller.components.SellerBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun SellerProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = onBack,
    onHomeClick: () -> Unit = onBack,
    onProductsClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("Tienda CanvasVibe") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = AuthRepository().currentUser()?.uid ?: return@LaunchedEffect
        runCatching {
            val snap = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .get().await()
            name = snap.getString("name").orEmpty().ifBlank { name }
            email = snap.getString("email").orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ProfileTopBar()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ShopCard(name = name, email = email) }
            item { StatsRow() }
            item { ConfigList() }
            item { Spacer(Modifier.height(4.dp)) }
            item { LogoutButton(onLogout) }
        }

        SellerBottomNav(
            selectedIndex = 3,
            onSelect = { ix ->
                when (ix) {
                    0 -> onHomeClick()
                    1 -> onProductsClick()
                    2 -> onOrdersClick()
                    else -> {}
                }
            }
        )
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Perfil", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Editar", color = PrimaryAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ShopCard(name: String, email: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = name.ifBlank { "Tienda CanvasVibe" },
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (email.isNotBlank()) {
            Text(text = email, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Plan Pro · Comisión 8%",
            color = PrimaryAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "Reputación", value = "4.8/5", modifier = Modifier.weight(1f))
        StatCard(label = "Seguidores", value = "2.134", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConfigList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BulletItem("Datos de la tienda")
        BulletItem("Notificaciones")
        BulletItem("Métodos de pago (Nequi, PSE)")
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(PrimaryAccent)
                .padding(2.dp)
        )
        Spacer(Modifier.padding(4.dp))
        Text(text = "· $text", color = TextPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceDark)
            .border(1.dp, Primary, RoundedCornerShape(999.dp))
            .clickable { onLogout() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cerrar sesión",
            color = Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
