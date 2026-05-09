package com.canvasvibe.app.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalUsers: Int = 0,
    val totalSalesCop: Long = 0L,
    val activeOrders: Int = 0,
    val verifiedArtists: Int = 0
)

class AdminDashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            runCatching {
                val usersSnap = db.collection("users").get().await()
                val ordersSnap = db.collection("orders").get().await()
                val totalSales = ordersSnap.documents.sumOf {
                    (it.getLong("totalPrice") ?: 0L).takeIf { _ ->
                        (it.getString("status") ?: "") != "CANCELLED"
                    } ?: 0L
                }
                val active = ordersSnap.documents.count {
                    val status = it.getString("status") ?: ""
                    status in setOf("PENDING", "PREPARING", "SHIPPED")
                }
                val sellers = usersSnap.documents.count {
                    it.getString("role") == "ROLE_SELLER"
                }
                _state.value = AdminDashboardUiState(
                    isLoading = false,
                    totalUsers = usersSnap.size(),
                    totalSalesCop = totalSales,
                    activeOrders = active,
                    verifiedArtists = sellers
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
