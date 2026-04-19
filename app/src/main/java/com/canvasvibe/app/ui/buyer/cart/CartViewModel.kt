package com.canvasvibe.app.ui.buyer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.CartItem
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.CartRepository
import com.canvasvibe.app.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepo: CartRepository = CartRepository(),
    private val orderRepo: OrderRepository = OrderRepository()
) : ViewModel() {

    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val uidFlow = MutableStateFlow(uid)

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<CartItem>> = uidFlow
        .flatMapLatest { currentUid ->
            if (currentUid.isNullOrBlank()) flowOf(emptyList())
            else cartRepo.getCart(currentUid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _checkoutOrderId = MutableStateFlow<String?>(null)
    val checkoutOrderId: StateFlow<String?> = _checkoutOrderId.asStateFlow()

    fun updateQuantity(productId: String, qty: Int) {
        val u = uid ?: return
        if (qty <= 0) {
            remove(productId)
            return
        }
        viewModelScope.launch {
            cartRepo.updateQuantity(u, productId, qty)
        }
    }

    fun remove(productId: String) {
        val u = uid ?: return
        viewModelScope.launch {
            cartRepo.removeFromCart(u, productId)
        }
    }

    fun checkout() {
        val u = uid ?: return
        val list = items.value
        if (list.isEmpty()) return
        viewModelScope.launch {
            val first = list.first()
            val order = Order(
                buyerId = u,
                productId = first.productId,
                productTitle = first.title,
                productImageUrl = first.imageUrl,
                sellerName = first.sellerName,
                material = first.material,
                size = first.size,
                quantity = first.quantity,
                unitPrice = first.unitPrice,
                totalPrice = list.sumOf { it.unitPrice * it.quantity }
            )
            orderRepo.createOrder(order).onSuccess { orderId ->
                cartRepo.clearCart(u)
                _checkoutOrderId.value = orderId
            }
        }
    }

    fun clearCheckoutFlag() { _checkoutOrderId.value = null }
}
