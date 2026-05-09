package com.canvasvibe.app.ui.admin.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class ArtistTab { TODOS, VERIFICADOS, PENDIENTES, SUSPENDIDOS }
enum class ArtistStatus { VERIFICADO, PENDIENTE, SUSPENDIDO }

data class AdminArtist(
    val uid: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val sales: Int,
    val totalRevenueCop: Long,
    val status: ArtistStatus
)

data class AdminArtistsUiState(
    val isLoading: Boolean = true,
    val tab: ArtistTab = ArtistTab.TODOS,
    val query: String = "",
    val artists: List<AdminArtist> = emptyList()
)

class AdminArtistViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdminArtistsUiState())
    val state: StateFlow<AdminArtistsUiState> = _state.asStateFlow()

    init { load() }

    fun setQuery(v: String) { _state.value = _state.value.copy(query = v) }
    fun selectTab(t: ArtistTab) { _state.value = _state.value.copy(tab = t) }

    fun changeStatus(uid: String, status: ArtistStatus) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("artistStatus", status.name)
                .await()
            _state.value = _state.value.copy(
                artists = _state.value.artists.map {
                    if (it.uid == uid) it.copy(status = status) else it
                }
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                val db = FirebaseFirestore.getInstance()
                val sellersSnap = db.collection("users")
                    .whereEqualTo("role", "ROLE_SELLER")
                    .get().await()

                val productsSnap = db.collection("products").get().await()
                val ordersSnap = db.collection("orders").get().await()

                val artists = sellersSnap.documents.map { doc ->
                    val uid = doc.id
                    val sellerProducts = productsSnap.documents.filter {
                        it.getString("sellerId") == uid
                    }
                    val ratings = sellerProducts.mapNotNull { it.getDouble("rating") }
                        .filter { it > 0 }
                    val sellerOrders = ordersSnap.documents.filter {
                        it.getString("sellerId") == uid &&
                            (it.getString("status") ?: "") != "CANCELLED"
                    }
                    val totalRevenue = sellerOrders.sumOf { it.getLong("totalPrice") ?: 0L }
                    val statusRaw = doc.getString("artistStatus")
                    val status = runCatching { ArtistStatus.valueOf(statusRaw ?: "PENDIENTE") }
                        .getOrDefault(ArtistStatus.PENDIENTE)

                    AdminArtist(
                        uid = uid,
                        name = doc.getString("name").orEmpty().ifBlank { "Sin nombre" },
                        specialty = sellerProducts.firstOrNull()
                            ?.getString("category").orEmpty()
                            .ifBlank { "Sin categoría" },
                        rating = if (ratings.isEmpty()) 0.0 else ratings.average(),
                        sales = sellerOrders.size,
                        totalRevenueCop = totalRevenue,
                        status = status
                    )
                }
                _state.value = _state.value.copy(isLoading = false, artists = artists)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

fun List<AdminArtist>.applyFilters(tab: ArtistTab, query: String): List<AdminArtist> {
    val tabFiltered = when (tab) {
        ArtistTab.TODOS        -> this
        ArtistTab.VERIFICADOS  -> filter { it.status == ArtistStatus.VERIFICADO }
        ArtistTab.PENDIENTES   -> filter { it.status == ArtistStatus.PENDIENTE }
        ArtistTab.SUSPENDIDOS  -> filter { it.status == ArtistStatus.SUSPENDIDO }
    }
    if (query.isBlank()) return tabFiltered
    return tabFiltered.filter { it.name.contains(query, ignoreCase = true) }
}
