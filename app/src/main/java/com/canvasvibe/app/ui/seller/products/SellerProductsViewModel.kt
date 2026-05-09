package com.canvasvibe.app.ui.seller.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.data.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SellerProductsUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val pendingActionId: String? = null
)

class SellerProductsViewModel(
    private val productRepo: ProductRepository = ProductRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SellerProductsUiState())
    val state: StateFlow<SellerProductsUiState> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        val uid = authRepo.currentUser()?.uid
        if (uid == null) {
            _state.value = _state.value.copy(isLoading = false, errorMessage = "Sesión inválida")
            return
        }
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("sellerId", uid)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.toObjects(Product::class.java)
                        ?.sortedByDescending { it.createdAt }
                        .orEmpty()
                    _state.value = _state.value.copy(isLoading = false, products = list)
                }
        }
    }

    fun toggleActive(product: Product) {
        viewModelScope.launch {
            _state.value = _state.value.copy(pendingActionId = product.id)
            val updated = product.copy().apply { isActive = !product.isActive }
            productRepo.updateProduct(updated)
            _state.value = _state.value.copy(pendingActionId = null)
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch {
            _state.value = _state.value.copy(pendingActionId = product.id)
            FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.id)
                .delete()
                .await()
            _state.value = _state.value.copy(pendingActionId = null)
        }
    }

    fun updateStock(product: Product, delta: Int) {
        val newStock = (product.stock + delta).coerceAtLeast(0)
        if (newStock == product.stock) return
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.id)
                .update("stock", newStock)
                .await()
        }
    }
}
