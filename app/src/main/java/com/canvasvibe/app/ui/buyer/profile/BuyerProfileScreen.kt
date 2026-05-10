package com.canvasvibe.app.ui.buyer.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasvibe.app.data.prefs.BiometricPreferences
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.ui.buyer.components.BuyerBottomNav
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun BuyerProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = onBack,
    onHomeClick: () -> Unit = onBack,
    onCartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var memberSince by remember { mutableStateOf("") }
    var orderCount by remember { mutableStateOf(0) }
    val uid = remember { AuthRepository().currentUser()?.uid.orEmpty() }
    var biometricEnabled by remember(uid) {
        mutableStateOf(BiometricPreferences.isEnabled(context, uid))
    }

    LaunchedEffect(uid) {
        if (uid.isBlank()) return@LaunchedEffect
        runCatching {
            val db = FirebaseFirestore.getInstance()
            val userSnap = db.collection("users").document(uid).get().await()
            name = userSnap.getString("name").orEmpty()
            email = userSnap.getString("email").orEmpty()
            val createdAt = userSnap.getLong("createdAt") ?: 0L
            if (createdAt > 0L) {
                val year = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(createdAt))
                memberSince = "Miembro desde $year"
            }
            val ordersSnap = db.collection("orders")
                .whereEqualTo("buyerId", uid)
                .get().await()
            orderCount = ordersSnap.size()
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
            item {
                HeroCard(
                    name = name.ifBlank { "Comprador" },
                    email = email,
                    memberSince = memberSince
                )
            }
            item { StatsRow(orderCount) }
            item { SectionTitle("Cuenta") }
            item {
                ConfigGroup(
                    items = listOf(
                        ConfigEntry(Icons.AutoMirrored.Filled.ListAlt, "Mis pedidos", subtitle = if (orderCount > 0) "$orderCount pedidos" else null),
                        ConfigEntry(Icons.Filled.Favorite, "Favoritos"),
                        ConfigEntry(Icons.Filled.LocationOn, "Direcciones de envío"),
                        ConfigEntry(Icons.Filled.Payment, "Métodos de pago")
                    )
                )
            }
            item { SectionTitle("Seguridad") }
            item {
                BiometricToggleCard(
                    enabled = biometricEnabled,
                    onChange = { active ->
                        biometricEnabled = active
                        BiometricPreferences.setDecision(context, uid, active)
                    }
                )
            }
            item { SectionTitle("Preferencias") }
            item {
                ConfigGroup(
                    items = listOf(
                        ConfigEntry(Icons.Filled.Notifications, "Notificaciones")
                    )
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { LogoutButton(onLogout) }
            item { Spacer(Modifier.height(4.dp)) }
        }

        BuyerBottomNav(
            selectedIndex = 3,
            onSelect = { ix ->
                when (ix) {
                    0, 1 -> onHomeClick()
                    2    -> onCartClick()
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
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.size(4.dp))
            Text("Editar", color = PrimaryAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HeroCard(name: String, email: String, memberSince: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    0f to Primary.copy(alpha = 0.32f),
                    0.6f to SurfaceDark,
                    1f to SurfaceDark
                )
            )
            .border(1.dp, Primary.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "C")
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.size(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (email.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = email, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                RoleBadge()
                if (memberSince.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = memberSince,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PrimaryAccent.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Comprador",
            color = PrimaryAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Avatar(initial: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    0f to Primary,
                    1f to PrimaryAccent
                )
            )
            .border(2.dp, TextPrimary.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsRow(orderCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "Pedidos", value = orderCount.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Favoritos", value = "0", modifier = Modifier.weight(1f))
        StatCard(label = "Reseñas", value = "0", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

private data class ConfigEntry(
    val icon: ImageVector,
    val label: String,
    val subtitle: String? = null,
    val onClick: () -> Unit = {}
)

@Composable
private fun ConfigGroup(items: List<ConfigEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
    ) {
        items.forEachIndexed { index, entry ->
            ConfigItem(entry = entry)
            if (index != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(BorderSubtle)
                )
            }
        }
    }
}

@Composable
private fun ConfigItem(entry: ConfigEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { entry.onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (!entry.subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(entry.subtitle, color = TextSecondary, fontSize = 10.sp)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun BiometricToggleCard(enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Ingreso con biometría", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (enabled) "Activo · usa huella o rostro al iniciar sesión"
                       else "Inactivo · entra solo con contraseña",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = Primary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceDark,
                uncheckedBorderColor = BorderSubtle
            )
        )
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
            .border(1.dp, Color(0xFFF44336), RoundedCornerShape(999.dp))
            .clickable { onLogout() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cerrar sesión",
            color = Color(0xFFF44336),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
