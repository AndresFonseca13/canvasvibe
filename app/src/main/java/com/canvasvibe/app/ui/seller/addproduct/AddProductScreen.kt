package com.canvasvibe.app.ui.seller.addproduct

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import coil.compose.AsyncImage
import com.canvasvibe.app.util.CameraImage
import com.canvasvibe.app.ui.theme.BorderSubtle
import com.canvasvibe.app.ui.theme.Primary
import com.canvasvibe.app.ui.theme.PrimaryAccent
import com.canvasvibe.app.ui.theme.SurfaceDark
import com.canvasvibe.app.ui.theme.TextPrimary
import com.canvasvibe.app.ui.theme.TextSecondary

private val MATERIALS = listOf("Vinilo", "Resina epóxica", "Óleo", "Digital")
private val SIZES = listOf("30x40", "50x70", "60x90")

@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    productId: String? = null,
    viewModel: AddProductViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val sizes by viewModel.sizes.collectAsStateWithLifecycle()
    val priceText by viewModel.priceText.collectAsStateWithLifecycle()
    val stockText by viewModel.stockText.collectAsStateWithLifecycle()
    val elaborationDays by viewModel.elaborationDays.collectAsStateWithLifecycle()
    val isCustomizable by viewModel.isCustomizable.collectAsStateWithLifecycle()
    val imageUris by viewModel.imageUris.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    val existingImageUrls by viewModel.existingImageUrls.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val isEditing = editingId != null

    LaunchedEffect(productId) {
        if (!productId.isNullOrBlank()) viewModel.loadForEdit(productId)
    }

    val context = LocalContext.current
    var showSourceSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.setImages(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { wasTaken ->
        val uri = pendingCameraUri
        if (wasTaken && uri != null) viewModel.addImage(uri)
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = CameraImage.createTempImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = CameraImage.createTempImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGallery() {
        pickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    LaunchedEffect(state) {
        if (state is AddProductUiState.Success) {
            viewModel.resetForm()
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(isEditing = isEditing)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ImagePickerCard(
                uris = imageUris,
                existingUrls = existingImageUrls,
                onPick = { showSourceSheet = true }
            )

            CategoryRow(
                options = availableCategories.map { it.slug to "${it.emoji} ${it.name}".trim() },
                selected = category,
                onSelect = viewModel::setCategory
            )

            FormCard(
                title = title,
                description = description,
                priceText = priceText,
                stockText = stockText,
                elaborationDays = elaborationDays,
                materials = materials,
                sizes = sizes,
                isCustomizable = isCustomizable,
                onTitleChange = viewModel::setTitle,
                onDescriptionChange = viewModel::setDescription,
                onPriceChange = viewModel::setPriceText,
                onStockChange = viewModel::setStockText,
                onElaborationDaysChange = viewModel::setElaborationDays,
                onMaterialToggle = viewModel::toggleMaterial,
                onSizeToggle = viewModel::toggleSize,
                onCustomizableChange = viewModel::setCustomizable
            )

            (state as? AddProductUiState.Error)?.let {
                Text(
                    text = it.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            PublishButton(
                loading = state is AddProductUiState.Submitting,
                isEditing = isEditing,
                onClick = { viewModel.publish() }
            )
        }
    }

        if (showSourceSheet) {
            ImageSourceSheet(
                onDismiss = { showSourceSheet = false },
                onCamera = {
                    showSourceSheet = false
                    launchCamera()
                },
                onGallery = {
                    showSourceSheet = false
                    launchGallery()
                }
            )
        }
    }
}

@Composable
private fun ImageSourceSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceDark)
                .clickable(enabled = false) {}
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(BorderSubtle)
            )
            Text(
                text = "Agregar imagen",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Elige de dónde quieres tomar la foto del producto.",
                color = TextSecondary,
                fontSize = 12.sp
            )
            SourceOption(
                icon = Icons.Filled.PhotoCamera,
                title = "Tomar foto",
                subtitle = "Usa la cámara del dispositivo",
                onClick = onCamera
            )
            SourceOption(
                icon = Icons.Filled.PhotoLibrary,
                title = "Elegir de galería",
                subtitle = "Selecciona hasta 3 imágenes",
                onClick = onGallery
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SourceOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TopBar(isEditing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isEditing) "Editar producto" else "Agregar producto",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isEditing) "Editando" else "Borrador",
            color = PrimaryAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ImagePickerCard(
    uris: List<android.net.Uri>,
    existingUrls: List<String> = emptyList(),
    onPick: () -> Unit
) {
    if (uris.isEmpty() && existingUrls.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .clickable { onPick() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = existingUrls.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }
            Text(
                text = "Imágenes actuales · toca el cuadro para reemplazarlas",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        return
    }
    if (uris.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        0f to SurfaceDark,
                        1f to Color(0xFF242424)
                    )
                )
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .clickable { onPick() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Subir arte del producto",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Toca para elegir imágenes",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .clickable { onPick() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uris.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }
            if (uris.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uris.drop(1).forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
            Text(
                text = "${uris.size} imagen(es) seleccionada(s) · toca el cuadro para reemplazar",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CategoryRow(
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    if (options.isEmpty()) {
        Text(
            "Cargando categorías…",
            color = TextSecondary,
            fontSize = 12.sp
        )
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            val isSelected = selected == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) Primary else SurfaceDark)
                    .then(
                        if (isSelected) Modifier
                        else Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(999.dp))
                    )
                    .clickable { onSelect(if (isSelected) null else key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
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
private fun FormCard(
    title: String,
    description: String,
    priceText: String,
    stockText: String,
    elaborationDays: String,
    materials: Set<String>,
    sizes: Set<String>,
    isCustomizable: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onElaborationDaysChange: (String) -> Unit,
    onMaterialToggle: (String) -> Unit,
    onSizeToggle: (String) -> Unit,
    onCustomizableChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LabeledField(
            label = "Nombre del producto",
            value = title,
            placeholder = "Ej: Neón Urbano #12",
            onValueChange = onTitleChange
        )
        LabeledField(
            label = "Descripción",
            value = description,
            placeholder = "Cuéntanos sobre tu obra…",
            onValueChange = onDescriptionChange,
            singleLine = false
        )
        LabeledField(
            label = "Precio (COP)",
            value = priceText,
            placeholder = "185000",
            onValueChange = onPriceChange,
            keyboardType = KeyboardType.Number
        )
        LabeledField(
            label = "Stock",
            value = stockText,
            placeholder = "5",
            onValueChange = onStockChange,
            keyboardType = KeyboardType.Number
        )
        LabeledField(
            label = "Tiempo de elaboración",
            value = elaborationDays,
            placeholder = "7-10 días",
            onValueChange = onElaborationDaysChange
        )

        ChipsSection(
            label = "Materiales",
            options = MATERIALS,
            selected = materials,
            onToggle = onMaterialToggle
        )
        ChipsSection(
            label = "Tamaños",
            options = SIZES,
            selected = sizes,
            onToggle = onSizeToggle
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Personalizable",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "El comprador puede solicitar variaciones",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isCustomizable,
                onCheckedChange = onCustomizableChange,
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
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
}

@Composable
private fun ChipsSection(
    label: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option in selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) Primary else MaterialTheme.colorScheme.background)
                        .border(
                            1.dp,
                            if (isSelected) Primary else BorderSubtle,
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { onToggle(option) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
}

@Composable
private fun PublishButton(loading: Boolean, isEditing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
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
            Text(
                text = if (isEditing) "Guardar cambios" else "Publicar producto",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
