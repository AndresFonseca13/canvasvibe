package com.canvasvibe.app.ui.auth

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.canvasvibe.app.data.prefs.BiometricPreferences
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun BiometricScreen(
    onAuthSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    fun rememberDecision(enabled: Boolean) {
        val uid = AuthRepository().currentUser()?.uid.orEmpty()
        BiometricPreferences.setDecision(context, uid, enabled)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Head()
        Spacer(Modifier.height(24.dp))
        Actions(
            onActivate = {
                val biometricManager = BiometricManager.from(context)
                val canAuthenticate = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )

                when (canAuthenticate) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        val executor = ContextCompat.getMainExecutor(context)
                        val prompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(
                                    result: BiometricPrompt.AuthenticationResult
                                ) {
                                    rememberDecision(enabled = true)
                                    onAuthSuccess()
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Error: $errString",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                        val info = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Ingreso seguro")
                            .setSubtitle("Confirma tu identidad para CanvasVibe")
                            .setNegativeButtonText("Cancelar")
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                            .build()
                        prompt.authenticate(info)
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        Toast.makeText(
                            context,
                            "Este dispositivo no tiene sensor biométrico",
                            Toast.LENGTH_LONG
                        ).show()
                        rememberDecision(enabled = false)
                        onSkip()
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        Toast.makeText(
                            context,
                            "El sensor biométrico no está disponible",
                            Toast.LENGTH_LONG
                        ).show()
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                        Toast.makeText(
                            context,
                            "No tienes huella registrada en el dispositivo",
                            Toast.LENGTH_LONG
                        ).show()
                    else ->
                        Toast.makeText(
                            context,
                            "Biometría no disponible (código $canAuthenticate)",
                            Toast.LENGTH_LONG
                        ).show()
                }
            },
            onSkip = {
                rememberDecision(enabled = false)
                onSkip()
            }
        )
    }
}

@Composable
private fun Head() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ingreso Seguro",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Activa biometría para entrar más rápido a CanvasVibe",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        BiometricCard()
    }
}

@Composable
private fun BiometricCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(14.dp, Primary, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PrimaryAccent)
            )
        }
        Text(
            text = "Huella y rostro",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tus datos biométricos se guardan en tu dispositivo",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Actions(
    onActivate: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Primary)
                .clickable(onClick = onActivate),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Activar biometría",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(25.dp))
                .clickable(onClick = onSkip),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ahora no",
                color = TextPrimary,
                fontSize = 14.sp
            )
        }

        Text(
            text = "Puedes cambiar esta opción en Ajustes cuando quieras",
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
