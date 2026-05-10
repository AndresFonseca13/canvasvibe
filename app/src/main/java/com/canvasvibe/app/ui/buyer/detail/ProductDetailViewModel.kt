package com.canvasvibe.app.ui.buyer.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.data.repository.CartRepository
import com.canvasvibe.app.data.repository.FavoriteRepository
import com.canvasvibe.app.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductDetailUiState {
    object Loading : ProductDetailUiState
    data class Ready(val product: Product) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val productRepo: ProductRepository = ProductRepository(),
    private val cartRepo: CartRepository = CartRepository(),
    private val favoriteRepo: FavoriteRepository = FavoriteRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<String?>(null)
    val selectedMaterial: StateFlow<String?> = _selectedMaterial.asStateFlow()

    private val _selectedSize = MutableStateFlow<String?>(null)
    val selectedSize: StateFlow<String?> = _selectedSize.asStateFlow()

    private val _productId = MutableStateFlow("")

    private val _isAddingToCart = MutableStateFlow(false)
    val isAddingToCart: StateFlow<Boolean> = _isAddingToCart.asStateFlow()

    private val _toggleInProgress = MutableStateFlow(false)
    val toggleInProgress: StateFlow<Boolean> = _toggleInProgress.asStateFlow()

    private val _feedback = MutableStateFlow<Feedback?>(null)
    val feedback: StateFlow<Feedback?> = _feedback.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = _productId
        .flatMapLatest { id ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            if (uid.isBlank() || id.isBlank()) flowOf(false)
            else favoriteRepo.observeIsFavorite(uid, id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun load(productId: String) {
        if (productId.isBlank()) {
            _state.value = ProductDetailUiState.Error("Producto inválido")
            return
        }
        _productId.value = productId
        viewModelScope.launch {
            _state.value = ProductDetailUiState.Loading
            productRepo.getProductById(productId).fold(
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _feedback.value = Feedback.Error("Sesión inválida. Inicia sesión nuevamente.")
            return
        }
        if (_isAddingToCart.value) return
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
            _isAddingToCart.value = true
            cartRepo.addToCart(uid, item).fold(
                onSuccess = {
                    _feedback.value = Feedback.Success("Agregado al carrito")
                },
                onFailure = { e ->
                    _feedback.value = Feedback.Error(friendly(e))
                }
            )
            _isAddingToCart.value = false
        }
    }

    fun toggleFavorite() {
        val productId = _productId.value
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank() || productId.isBlank()) {
            _feedback.value = Feedback.Error("Sesión inválida")
            return
        }
        if (_toggleInProgress.value) return
        viewModelScope.launch {
            _toggleInProgress.value = true
            favoriteRepo.toggle(uid, productId).fold(
                onSuccess = { isNowFav ->
                    _feedback.value = Feedback.Success(
                        if (isNowFav) "Agregado a favoritos" else "Quitado de favoritos"
                    )
                },
                onFailure = { e ->
                    _feedback.value = Feedback.Error(friendly(e))
                }
            )
            _toggleInProgress.value = false
        }
    }

    fun consumeFeedback() { _feedback.value = null }

    private fun friendly(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "Firestore rechazó la operación. Revisa las reglas."
            raw.contains("UNAUTHENTICATED", true) -> "Sesión expirada. Inicia sesión nuevamente."
            raw.isBlank() -> "No se pudo completar la operación"
            else -> raw
        }
    }
}

sealed interface Feedback {
    data class Success(val message: String) : Feedback
    data class Error(val message: String) : Feedback
}
