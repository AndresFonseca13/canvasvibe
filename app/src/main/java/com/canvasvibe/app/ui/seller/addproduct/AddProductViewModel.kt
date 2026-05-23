package com.canvasvibe.app.ui.seller.addproduct

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Category
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.data.repository.CategoryRepository
import com.canvasvibe.app.data.repository.ProductRepository
import com.canvasvibe.app.data.repository.StorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed interface AddProductUiState {
    object Idle : AddProductUiState
    object Submitting : AddProductUiState
    data class Success(val productId: String) : AddProductUiState
    data class Error(val message: String) : AddProductUiState
}

class AddProductViewModel(
    private val productRepo: ProductRepository = ProductRepository(),
    private val storageRepo: StorageRepository = StorageRepository(),
    private val categoryRepo: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val state: StateFlow<AddProductUiState> = _state.asStateFlow()

    val availableCategories: StateFlow<List<Category>> = categoryRepo.observeActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _editingId = MutableStateFlow<String?>(null)
    val editingId: StateFlow<String?> = _editingId.asStateFlow()

    private val _existingImageUrls = MutableStateFlow<List<String>>(emptyList())
    val existingImageUrls: StateFlow<List<String>> = _existingImageUrls.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val _materials = MutableStateFlow<Set<String>>(emptySet())
    val materials: StateFlow<Set<String>> = _materials.asStateFlow()

    private val _sizes = MutableStateFlow<Set<String>>(emptySet())
    val sizes: StateFlow<Set<String>> = _sizes.asStateFlow()

    private val _priceText = MutableStateFlow("")
    val priceText: StateFlow<String> = _priceText.asStateFlow()

    private val _stockText = MutableStateFlow("")
    val stockText: StateFlow<String> = _stockText.asStateFlow()

    private val _elaborationDays = MutableStateFlow("")
    val elaborationDays: StateFlow<String> = _elaborationDays.asStateFlow()

    private val _isCustomizable = MutableStateFlow(true)
    val isCustomizable: StateFlow<Boolean> = _isCustomizable.asStateFlow()

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris.asStateFlow()

    fun setTitle(v: String) { _title.value = v }
    fun setDescription(v: String) { _description.value = v }
    fun setCategory(v: String?) { _category.value = v }
    fun toggleMaterial(v: String) {
        _materials.value = if (v in _materials.value) _materials.value - v else _materials.value + v
    }
    fun toggleSize(v: String) {
        _sizes.value = if (v in _sizes.value) _sizes.value - v else _sizes.value + v
    }
    fun setPriceText(v: String) { _priceText.value = v.filter { it.isDigit() } }
    fun setStockText(v: String) { _stockText.value = v.filter { it.isDigit() } }
    fun setElaborationDays(v: String) { _elaborationDays.value = v }
    fun setCustomizable(v: Boolean) { _isCustomizable.value = v }
    fun setImages(uris: List<Uri>) { _imageUris.value = uris }
    fun addImage(uri: Uri) {
        val current = _imageUris.value
        if (uri in current) return
        _imageUris.value = (current + uri).takeLast(3)
    }
    fun removeImage(uri: Uri) { _imageUris.value = _imageUris.value - uri }

    fun resetForm() {
        _editingId.value = null
        _existingImageUrls.value = emptyList()
        _title.value = ""
        _description.value = ""
        _category.value = null
        _materials.value = emptySet()
        _sizes.value = emptySet()
        _priceText.value = ""
        _stockText.value = ""
        _elaborationDays.value = ""
        _isCustomizable.value = true
        _imageUris.value = emptyList()
        _state.value = AddProductUiState.Idle
    }
    fun consumeState() { _state.value = AddProductUiState.Idle }

    fun loadForEdit(productId: String) {
        if (productId.isBlank() || _editingId.value == productId) return
        viewModelScope.launch {
            productRepo.getProductById(productId).onSuccess { p ->
                _editingId.value = p.id
                _existingImageUrls.value = p.imageUrls
                _title.value = p.title
                _description.value = p.description
                _category.value = p.category.ifBlank { null }
                _materials.value = p.materials.toSet()
                _sizes.value = p.sizes.toSet()
                _priceText.value = if (p.priceBase > 0) p.priceBase.toString() else ""
                _stockText.value = p.stock.toString()
                _elaborationDays.value = p.elaborationDays
                _isCustomizable.value = p.isCustomizable
                _imageUris.value = emptyList()
            }
        }
    }

    fun publish() {
        val titleValue = _title.value.trim()
        val priceValue = _priceText.value.toLongOrNull() ?: 0L
        val stockValue = _stockText.value.toIntOrNull() ?: 0

        if (titleValue.isBlank()) {
            _state.value = AddProductUiState.Error("El título es obligatorio")
            return
        }
        if (_category.value.isNullOrBlank()) {
            _state.value = AddProductUiState.Error("Selecciona una categoría")
            return
        }
        if (priceValue <= 0L) {
            _state.value = AddProductUiState.Error("Precio inválido")
            return
        }

        val isEditing = _editingId.value != null
        if (!isEditing && _imageUris.value.isEmpty()) {
            _state.value = AddProductUiState.Error("Sube al menos una imagen")
            return
        }

        val auth = FirebaseAuth.getInstance().currentUser
        if (auth == null) {
            _state.value = AddProductUiState.Error("Sesión expirada, inicia sesión nuevamente")
            return
        }
        val sellerId = auth.uid

        viewModelScope.launch {
            _state.value = AddProductUiState.Submitting
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(sellerId)
                    .get()
                    .await()
                val sellerName = userDoc.getString("name") ?: "Artista"

                val productId = _editingId.value ?: UUID.randomUUID().toString()

                val newUrls = _imageUris.value.mapIndexed { index, uri ->
                    storageRepo.uploadProductImage(sellerId, productId, uri, index)
                        .getOrThrow()
                }
                val finalImageUrls = if (isEditing && newUrls.isEmpty())
                    _existingImageUrls.value
                else
                    newUrls

                val product = Product(
                    id = productId,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    title = titleValue,
                    description = _description.value.trim(),
                    category = _category.value!!,
                    materials = _materials.value.toList(),
                    sizes = _sizes.value.toList(),
                    priceBase = priceValue,
                    imageUrls = finalImageUrls,
                    rating = 0.0,
                    reviewCount = 0,
                    stock = stockValue,
                    elaborationDays = _elaborationDays.value.trim().ifBlank { "7-10 días" },
                    isCustomizable = _isCustomizable.value,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )

                val result = if (isEditing)
                    productRepo.updateProduct(product).map { product.id }
                else
                    productRepo.addProduct(product)

                result.fold(
                    onSuccess = { _state.value = AddProductUiState.Success(it) },
                    onFailure = {
                        _state.value = AddProductUiState.Error(it.message ?: "Error al guardar")
                    }
                )
            } catch (e: Exception) {
                _state.value = AddProductUiState.Error(e.message ?: "Error al subir imagen")
            }
        }
    }
}
