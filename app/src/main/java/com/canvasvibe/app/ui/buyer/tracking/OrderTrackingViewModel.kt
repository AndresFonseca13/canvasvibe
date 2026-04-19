package com.canvasvibe.app.ui.buyer.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Order
import com.canvasvibe.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderTrackingUiState {
    object Loading : OrderTrackingUiState
    data class Ready(val order: Order) : OrderTrackingUiState
    data class Error(val message: String) : OrderTrackingUiState
}

class OrderTrackingViewModel(
    private val repo: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<OrderTrackingUiState>(OrderTrackingUiState.Loading)
    val state: StateFlow<OrderTrackingUiState> = _state.asStateFlow()

    fun load(orderId: String) {
        if (orderId.isBlank()) {
            _state.value = OrderTrackingUiState.Error("Pedido inválido")
            return
        }
        viewModelScope.launch {
            _state.value = OrderTrackingUiState.Loading
            repo.getOrderById(orderId).fold(
                onSuccess = { _state.value = OrderTrackingUiState.Ready(it) },
                onFailure = { _state.value = OrderTrackingUiState.Error(it.message ?: "Error") }
            )
        }
    }
}
