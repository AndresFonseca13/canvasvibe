package com.canvasvibe.app.ui.admin.buyers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class BuyerTab { TODOS, ACTIVOS, BLOQUEADOS }
enum class BuyerStatus { ACTIVO, BLOQUEADO }

data class AdminBuyer(
    val uid: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val orderCount: Int,
    val totalSpentCop: Long,
    val lastOrderAt: Long,
    val status: BuyerStatus
)

data class AdminBuyersUiState(
    val isLoading: Boolean = true,
    val tab: BuyerTab = BuyerTab.TODOS,
    val query: String = "",
    val buyers: List<AdminBuyer> = emptyList(),
    val errorMessage: String? = null
)

class AdminBuyersViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdminBuyersUiState())
    val state: StateFlow<AdminBuyersUiState> = _state.asStateFlow()

    init { load() }

    fun setQuery(v: String) { _state.value = _state.value.copy(query = v) }
    fun selectTab(t: BuyerTab) { _state.value = _state.value.copy(tab = t) }
    fun dismissError() { _state.value = _state.value.copy(errorMessage = null) }

    fun changeStatus(uid: String, status: BuyerStatus) {
        viewModelScope.launch {
            runCatching {
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .update("buyerStatus", status.name)
                    .await()
            }.onSuccess {
                _state.value = _state.value.copy(
                    buyers = _state.value.buyers.map {
                        if (it.uid == uid) it.copy(status = status) else it
                    }
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(errorMessage = friendly(e))
            }
        }
    }

    fun reload() = load()

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val db = FirebaseFirestore.getInstance()
                val buyersSnap = db.collection("users")
                    .whereEqualTo("role", "ROLE_BUYER")
                    .get().await()
                val ordersSnap = db.collection("orders").get().await()

                val buyers = buyersSnap.documents.map { doc ->
                    val uid = doc.id
                    val orders = ordersSnap.documents.filter {
                        it.getString("buyerId") == uid &&
                            (it.getString("status") ?: "") != "CANCELLED"
                    }
                    val totalSpent = orders.sumOf { it.getLong("totalPrice") ?: 0L }
                    val lastOrderAt = orders.maxOfOrNull { it.getLong("createdAt") ?: 0L } ?: 0L
                    val statusRaw = doc.getString("buyerStatus")
                    val status = runCatching { BuyerStatus.valueOf(statusRaw ?: "ACTIVO") }
                        .getOrDefault(BuyerStatus.ACTIVO)

                    AdminBuyer(
                        uid = uid,
                        name = doc.getString("name").orEmpty().ifBlank { "Sin nombre" },
                        email = doc.getString("email").orEmpty(),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        orderCount = orders.size,
                        totalSpentCop = totalSpent,
                        lastOrderAt = lastOrderAt,
                        status = status
                    )
                }.sortedByDescending { it.totalSpentCop }
                _state.value = _state.value.copy(isLoading = false, buyers = buyers)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = friendly(e)
                )
            }
        }
    }

    private fun friendly(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "Firestore rechazó la operación. Revisa las reglas de la colección 'users'."
            raw.contains("UNAUTHENTICATED", true) ->
                "No estás autenticado. Inicia sesión nuevamente."
            raw.isBlank() -> "Error desconocido"
            else -> raw
        }
    }
}

fun List<AdminBuyer>.applyFilters(tab: BuyerTab, query: String): List<AdminBuyer> {
    val tabFiltered = when (tab) {
        BuyerTab.TODOS      -> this
        BuyerTab.ACTIVOS    -> filter { it.status == BuyerStatus.ACTIVO }
        BuyerTab.BLOQUEADOS -> filter { it.status == BuyerStatus.BLOQUEADO }
    }
    if (query.isBlank()) return tabFiltered
    return tabFiltered.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
    }
}
