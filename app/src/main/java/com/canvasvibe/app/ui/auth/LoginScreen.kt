package com.canvasvibe.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.data.model.User
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var name           by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (val s = state) {
            is AuthState.Success -> {
                onLoginSuccess(s.user)
                viewModel.resetState()
            }
            is AuthState.Registered -> {
                kotlinx.coroutines.delay(1800)
                password = ""
                name = ""
                isRegisterMode = false
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Hero()

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRegisterMode) {
                DarkInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre completo"
                )
            }

            DarkInput(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo electrónico",
                keyboardType = KeyboardType.Email
            )

            DarkInput(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            if (!isRegisterMode) {
                Text(
                    text = "Olvidé mi contraseña",
                    color = PrimaryAccent,
                    fontSize = 13.sp
                )
            }

            (state as? AuthState.Error)?.let {
                Text(
                    text = it.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            PrimaryButton(
                text = if (isRegisterMode) "Crear cuenta" else "Iniciar sesión",
                loading = state is AuthState.Loading,
                onClick = {
                    if (isRegisterMode) viewModel.register(email, password, name)
                    else viewModel.login(email, password)
                }
            )

            Text(
                text = "o continúa con",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SocialButton("Google", Modifier.weight(1f))
                SocialButton("Apple",  Modifier.weight(1f))
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(if (isRegisterMode) "¿Ya tienes cuenta? " else "¿No tienes cuenta? ")
                    }
                    withStyle(SpanStyle(color = PrimaryAccent)) {
                        append(if (isRegisterMode) "Inicia sesión" else "Crear cuenta")
                    }
                },
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable { isRegisterMode = !isRegisterMode; viewModel.resetState() }
            )
        }
    }

        AnimatedVisibility(
            visible = state is AuthState.Registered,
            enter = fadeIn(tween(220)),
            exit  = fadeOut(tween(220))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state is AuthState.Registered,
                    enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.6f) + fadeIn(tween(220)),
                    exit  = scaleOut(tween(180), targetScale = 0.8f) + fadeOut(tween(180))
                ) {
                    SuccessCard(name = (state as? AuthState.Registered)?.user?.name)
                }
            }
        }
    }
}

@Composable
private fun SuccessCard(name: String?) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(colors = listOf(Primary, PrimaryAccent))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = TextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "¡Cuenta creada!",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (!name.isNullOrBlank())
                "Bienvenido, $name. Inicia sesión para continuar."
            else
                "Ahora inicia sesión para continuar.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Hero() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary, PrimaryAccent)
                    )
                )
        )
        Text(
            text = "CanvasVibe",
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Compra arte digital único",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DarkInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(Primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = TextSecondary, fontSize = 14.sp)
                }
                inner()
            }
        )
    }
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Primary)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = TextPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(text = text, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SocialButton(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { /* TODO: integración OAuth */ },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp)
    }
}