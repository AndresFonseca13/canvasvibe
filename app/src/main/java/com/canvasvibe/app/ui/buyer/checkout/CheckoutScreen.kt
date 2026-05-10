package com.canvasvibe.app.ui.buyer.checkout

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvasvibe.app.ui.buyer.home.formatCop
import com.canvasvibe.app.ui.theme.Background
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPaid: (orderId: String) -> Unit,
    vm: CheckoutViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        val s = state
        if (s is CheckoutState.Success) onPaid(s.orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(onBack)

        when (val s = state) {
            is CheckoutState.Form -> {
                FormBody(
                    form = s.form,
                    subtotal = items.sumOf { it.unitPrice * it.quantity },
                    shipping = vm.shippingCost,
                    onMethodChange = vm::setMethod,
                    onFullNameChange = vm::setFullName,
                    onPhoneChange = vm::setPhone,
                    onAddressChange = vm::setAddress,
                    onCityChange = vm::setCity,
                    onNotesChange = vm::setNotes,
                    onCardNumberChange = vm::setCardNumber,
                    onCardExpiryChange = vm::setCardExpiry,
                    onCardCvvChange = vm::setCardCvv,
                    onPseBankChange = vm::setPseBank,
                    onNequiPhoneChange = vm::setNequiPhone,
                    onPay = vm::pay,
                    isProcessing = false,
                    errorMessage = null
                )
            }
            is CheckoutState.Processing -> {
                FormBody(
                    form = (state as? CheckoutState.Form)?.form ?: vm.lastKnownForm(),
                    subtotal = items.sumOf { it.unitPrice * it.quantity },
                    shipping = vm.shippingCost,
                    onMethodChange = {},
                    onFullNameChange = {},
                    onPhoneChange = {},
                    onAddressChange = {},
                    onCityChange = {},
                    onNotesChange = {},
                    onCardNumberChange = {},
                    onCardExpiryChange = {},
                    onCardCvvChange = {},
                    onPseBankChange = {},
                    onNequiPhoneChange = {},
                    onPay = {},
                    isProcessing = true,
                    errorMessage = null
                )
            }
            is CheckoutState.Error -> {
                FormBody(
                    form = vm.lastKnownForm(),
                    subtotal = items.sumOf { it.unitPrice * it.quantity },
                    shipping = vm.shippingCost,
                    onMethodChange = vm::setMethod,
                    onFullNameChange = vm::setFullName,
                    onPhoneChange = vm::setPhone,
                    onAddressChange = vm::setAddress,
                    onCityChange = vm::setCity,
                    onNotesChange = vm::setNotes,
                    onCardNumberChange = vm::setCardNumber,
                    onCardExpiryChange = vm::setCardExpiry,
                    onCardCvvChange = vm::setCardCvv,
                    onPseBankChange = vm::setPseBank,
                    onNequiPhoneChange = vm::setNequiPhone,
                    onPay = { vm.consumeError(); vm.pay() },
                    isProcessing = false,
                    errorMessage = s.message
                )
            }
            is CheckoutState.Success -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Pago confirmado", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun CheckoutViewModel.lastKnownForm(): CheckoutForm {
    return (state.value as? CheckoutState.Form)?.form ?: CheckoutForm()
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Background)
                .border(1.dp, BorderSubtle, CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Text("Pasarela de pagos", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FormBody(
    form: CheckoutForm,
    subtotal: Long,
    shipping: Long,
    onMethodChange: (PaymentMethod) -> Unit,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onPseBankChange: (String) -> Unit,
    onNequiPhoneChange: (String) -> Unit,
    onPay: () -> Unit,
    isProcessing: Boolean,
    errorMessage: String?
) {
    val total = subtotal + shipping
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        errorMessage?.let { msg ->
            item { ErrorBanner(msg) }
        }
        item { SectionTitle("Resumen del pedido") }
        item {
            SummaryCard(subtotal = subtotal, shipping = shipping, total = total)
        }

        item { SectionTitle("Datos de envío") }
        item {
            CardContainer {
                LabeledField("Nombre completo", form.fullName, onFullNameChange)
                LabeledField("Teléfono", form.phone, onPhoneChange, keyboardType = KeyboardType.Phone)
                LabeledField("Dirección", form.address, onAddressChange)
                LabeledField("Ciudad", form.city, onCityChange)
                LabeledField("Notas (opcional)", form.notes, onNotesChange, singleLine = false)
            }
        }

        item { SectionTitle("Método de pago") }
        item {
            MethodSelector(selected = form.method, onSelect = onMethodChange)
        }

        item {
            CardContainer {
                when (form.method) {
                    PaymentMethod.TARJETA -> CardForm(form, onCardNumberChange, onCardExpiryChange, onCardCvvChange)
                    PaymentMethod.PSE     -> PseForm(form, onPseBankChange)
                    PaymentMethod.NEQUI   -> NequiForm(form, onNequiPhoneChange)
                }
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item { SecureBadge() }
        item {
            PayButton(
                total = total,
                processing = isProcessing,
                onClick = onPay
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun CardContainer(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) { content() }
}

@Composable
private fun SummaryCard(subtotal: Long, shipping: Long, total: Long) {
    CardContainer {
        SummaryRow("Subtotal", formatCop(subtotal))
        SummaryRow("Comisión y envío", formatCop(shipping))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(formatCop(total), color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun MethodSelector(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MethodOption(
            method = PaymentMethod.PSE,
            icon = Icons.Filled.AccountBalance,
            selected = selected == PaymentMethod.PSE,
            onClick = { onSelect(PaymentMethod.PSE) }
        )
        MethodOption(
            method = PaymentMethod.TARJETA,
            icon = Icons.Filled.CreditCard,
            selected = selected == PaymentMethod.TARJETA,
            onClick = { onSelect(PaymentMethod.TARJETA) }
        )
        MethodOption(
            method = PaymentMethod.NEQUI,
            icon = Icons.Filled.Smartphone,
            selected = selected == PaymentMethod.NEQUI,
            onClick = { onSelect(PaymentMethod.NEQUI) }
        )
    }
}

@Composable
private fun MethodOption(
    method: PaymentMethod,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) Primary else BorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(method.label(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(method.subtitle(), color = TextSecondary, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) Primary else BorderSubtle, CircleShape)
                .background(if (selected) Primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun CardForm(
    form: CheckoutForm,
    onCardNumber: (String) -> Unit,
    onCardExpiry: (String) -> Unit,
    onCardCvv: (String) -> Unit
) {
    LabeledField("Número de tarjeta", form.cardNumber, onCardNumber, keyboardType = KeyboardType.Number, placeholder = "1234 5678 9012 3456")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            LabeledField("Vencimiento", form.cardExpiry, onCardExpiry, placeholder = "MM/AA")
        }
        Box(modifier = Modifier.weight(1f)) {
            LabeledField("CVV", form.cardCvv, onCardCvv, keyboardType = KeyboardType.Number, placeholder = "123")
        }
    }
}

@Composable
private fun PseForm(form: CheckoutForm, onBank: (String) -> Unit) {
    val banks = listOf("Bancolombia", "Davivienda", "Banco de Bogotá", "BBVA", "Nequi (Bancolombia)", "Nu")
    Text("Selecciona tu banco", color = TextSecondary, fontSize = 12.sp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        banks.forEach { name ->
            val selected = form.pseBank == name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Primary.copy(alpha = 0.16f) else Background)
                    .border(1.dp, if (selected) Primary else BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable { onBank(name) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                if (selected) Icon(Icons.Filled.CheckCircle, null, tint = Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun NequiForm(form: CheckoutForm, onPhone: (String) -> Unit) {
    LabeledField(
        label = "Número Nequi",
        value = form.nequiPhone,
        onChange = onPhone,
        keyboardType = KeyboardType.Phone,
        placeholder = "Ej: 3001234567"
    )
    Text(
        "Recibirás una notificación push para confirmar el pago.",
        color = TextSecondary,
        fontSize = 11.sp
    )
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Background)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = singleLine,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty() && placeholder.isNotBlank()) {
                        Text(placeholder, color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun SecureBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Lock, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
        Spacer(Modifier.size(4.dp))
        Text(
            "Pago protegido — tus datos no se almacenan",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PayButton(total: Long, processing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Primary)
            .clickable(enabled = !processing) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (processing) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = "Pagar ${formatCop(total)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF44336).copy(alpha = 0.16f))
            .border(1.dp, Color(0xFFF44336).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(message, color = Color(0xFFFF8A80), fontSize = 12.sp)
    }
}

@Suppress("unused") private val unusedAccent = PrimaryAccent
