package com.canvasvibe.app.ui.buyer.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.data.repository.CartRepository
import com.canvasvibe.app.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductDetailUiState {
    object Loading : ProductDetailUiState
    data class Ready(val product: Product) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val productRepo: ProductRepository = ProductRepository(),
    private val cartRepo: CartRepository = CartRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<String?>(null)
    val selectedMaterial: StateFlow<String?> = _selectedMaterial.asStateFlow()

    private val _selectedSize = MutableStateFlow<String?>(null)
    val selectedSize: StateFlow<String?> = _selectedSize.asStateFlow()

    private val _addedToCart = MutableStateFlow(false)
    val addedToCart: StateFlow<Boolean> = _addedToCart.asStateFlow()

    fun load(productId: String) {
        if (productId.isBlank()) {
            _state.value = ProductDetailUiState.Error("Producto inválido")
            return
        }
        viewModelScope.launch {
            _state.value = ProductDetailUiState.Loading
            val result = productRepo.getProductById(productId)
            result.fold(
                onSuccess = { product ->
                    _state.value = ProductDetailUiState.Ready(product)
                    _selectedMaterial.value = product.materials.firstOrNull()
                    _selectedSize.value = product.sizes.firstOrNull()
                },
                onFailure = { err ->
                    _state.value = ProductDetailUiState.Error(err.message ?: "Error desconocido")
                }
            )
        }
    }

    fun selectMaterial(value: String) { _selectedMaterial.value = value }
    fun selectSize(value: String) { _selectedSize.value = value }

    fun addToCart() {
        val ready = _state.value as? ProductDetailUiState.Ready ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val item = CartItem(
            productId = ready.product.id,
            title = ready.product.title,
            sellerName = ready.product.sellerName,
            material = _selectedMaterial.value.orEmpty(),
            size = _selectedSize.value.orEmpty(),
            quantity = 1,
            unitPrice = ready.product.priceBase,
            imageUrl = ready.product.imageUrls.firstOrNull().orEmpty()
        )
        viewModelScope.launch {
            cartRepo.addToCart(uid, item).onSuccess {
                _addedToCart.value = true
            }
        }
    }

    fun clearAddedFlag() { _addedToCart.value = false }
}