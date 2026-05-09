package com.canvasvibe.app.ui.seller.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OrdersTab { ACTIVOS, ENTREGADOS }

data class SellerOrdersUiState(
    val isLoading: Boolean = true,
    val tab: OrdersTab = OrdersTab.ACTIVOS,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null
)

class SellerOrdersViewModel(
    private val orderRepo: OrderRepository = OrderRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SellerOrdersUiState())
    val state: StateFlow<SellerOrdersUiState> = _state.asStateFlow()

    init {
        observeOrders()
    }

    fun selectTab(tab: OrdersTab) {
        _state.value = _state.value.copy(tab = tab)
    }

    fun advanceStatus(order: Order) {
        val next = nextStatus(order.status) ?: return
        viewModelScope.launch {
            orderRepo.updateOrderStatus(order.id, next)
        }
    }

    private fun observeOrders() {
        val uid = authRepo.currentUser()?.uid
        if (uid == null) {
            _state.value = _state.value.copy(isLoading = false, errorMessage = "Sesión inválida")
            return
        }
        viewModelScope.launch {
            orderRepo.getOrdersBySeller(uid).collect { list ->
                _state.value = _state.value.copy(isLoading = false, orders = list)
            }
        }
    }

    private fun nextStatus(current: String): String? = when (current) {
        "PENDING"   -> "PREPARING"
        "PREPARING" -> "SHIPPED"
        "SHIPPED"   -> "DELIVERED"
        else        -> null
    }
}

fun List<Order>.byTab(tab: OrdersTab): List<Order> = when (tab) {
    OrdersTab.ACTIVOS    -> filter { it.status in setOf("PENDING", "PREPARING", "SHIPPED") }
    OrdersTab.ENTREGADOS -> filter { it.status == "DELIVERED" || it.status == "CANCELLED" }
}
