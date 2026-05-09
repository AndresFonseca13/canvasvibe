package com.canvasvibe.app.ui.seller.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.AuthRepository
import com.canvasvibe.app.data.repository.OrderRepository
import com.canvasvibe.app.data.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

data class SellerDashboardUiState(
    val sellerName: String = "Artista",
    val isLoading: Boolean = true,
    val salesTodayCop: Long = 0L,
    val ordersToday: Int = 0,
    val activeProducts: Int = 0,
    val readyToShip: Int = 0,
    val pendingPayments: Int = 0,
    val recentOrders: List<Order> = emptyList()
)

class SellerDashboardViewModel(
    private val productRepo: ProductRepository = ProductRepository(),
    private val orderRepo: OrderRepository = OrderRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SellerDashboardUiState())
    val state: StateFlow<SellerDashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val uid = authRepo.currentUser()?.uid ?: return
        viewModelScope.launch {
            val name = runCatching {
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .get().await()
                    .getString("name").orEmpty()
            }.getOrDefault("Artista")

            _state.value = _state.value.copy(sellerName = name.ifBlank { "Artista" })
        }
        viewModelScope.launch {
            orderRepo.getOrdersBySeller(uid)
                .catch { _state.value = _state.value.copy(isLoading = false) }
                .collect { orders ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        salesTodayCop = orders.salesToday(),
                        ordersToday = orders.countToday(),
                        readyToShip = orders.count { it.status == "PREPARING" || it.status == "SHIPPED" },
                        pendingPayments = orders.count { it.status == "PENDING" },
                        recentOrders = orders.take(3)
                    )
                }
        }
        viewModelScope.launch {
            productRepo.getProducts()
                .catch { /* swallow */ }
                .collect { products ->
                    val mine = products.count { it.sellerId == uid }
                    _state.value = _state.value.copy(activeProducts = mine)
                }
        }
    }
}

private fun List<Order>.salesToday(): Long {
    val start = startOfDayMillis()
    return filter { it.createdAt >= start && it.status != "CANCELLED" }
        .sumOf { it.totalPrice }
}

private fun List<Order>.countToday(): Int {
    val start = startOfDayMillis()
    return count { it.createdAt >= start && it.status != "CANCELLED" }
}

private fun startOfDayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
