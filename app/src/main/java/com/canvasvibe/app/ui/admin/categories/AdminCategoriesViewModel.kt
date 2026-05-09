package com.canvasvibe.app.ui.admin.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Category
import com.canvasvibe.app.data.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminCategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isSeeding: Boolean = false,
    val errorMessage: String? = null
)

class AdminCategoriesViewModel(
    private val repo: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCategoriesUiState())
    val state: StateFlow<AdminCategoriesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.seedDefaultsIfEmpty().onFailure { e ->
                _state.value = _state.value.copy(errorMessage = friendly(e))
            }
        }
        viewModelScope.launch {
            repo.observeAll()
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = friendly(e)
                    )
                }
                .collect { list ->
                    val withCounts = withProductCounts(list)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        categories = withCounts
                    )
                }
        }
    }

    fun showCreate(show: Boolean) {
        _state.value = _state.value.copy(showCreateDialog = show)
    }

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun seedDefaultsManual() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSeeding = true, errorMessage = null)
            repo.seedDefaults().fold(
                onSuccess = { count ->
                    _state.value = _state.value.copy(
                        isSeeding = false,
                        errorMessage = if (count == 0) "No se sembró nada" else null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSeeding = false,
                        errorMessage = friendly(e)
                    )
                }
            )
        }
    }

    fun toggle(category: Category, active: Boolean) {
        viewModelScope.launch {
            repo.setActive(category.id, active).onFailure { e ->
                _state.value = _state.value.copy(errorMessage = friendly(e))
            }
        }
    }

    fun create(name: String, emoji: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (_state.value.categories.maxOfOrNull { it.order } ?: -1) + 1
            val newCategory = Category(
                slug = name.lowercase().replace(Regex("\\s+"), "-"),
                name = name.trim(),
                emoji = emoji.ifBlank { "🎨" },
                colorHex = colorHex,
                order = nextOrder,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            repo.add(newCategory).fold(
                onSuccess = {
                    _state.value = _state.value.copy(showCreateDialog = false)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        showCreateDialog = false,
                        errorMessage = friendly(e)
                    )
                }
            )
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            repo.delete(category.id).onFailure { e ->
                _state.value = _state.value.copy(errorMessage = friendly(e))
            }
        }
    }

    private suspend fun withProductCounts(categories: List<Category>): List<Category> {
        return try {
            val productsSnap = FirebaseFirestore.getInstance()
                .collection("products")
                .get()
                .await()
            val countByCategory = productsSnap.documents
                .mapNotNull { it.getString("category") }
                .groupingBy { it }
                .eachCount()
            categories.map { it.copy(productCount = countByCategory[it.slug] ?: 0) }
        } catch (e: Exception) {
            categories
        }
    }

    private fun friendly(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "Firestore rechazó la operación. Revisa las reglas de la colección 'categories'."
            raw.contains("UNAUTHENTICATED", true) ->
                "No estás autenticado. Inicia sesión nuevamente."
            raw.isBlank() -> "Error desconocido"
            else -> raw
        }
    }
}
