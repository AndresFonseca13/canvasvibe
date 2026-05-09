package com.canvasvibe.app.ui.buyer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvasvibe.app.data.model.Category
import com.canvasvibe.app.data.model.Product
import com.canvasvibe.app.data.repository.CategoryRepository
import com.canvasvibe.app.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BuyerHomeViewModel(
    private val repo: ProductRepository = ProductRepository(),
    private val categoryRepo: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryRepo.observeActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _selectedCategory
        .flatMapLatest { category ->
            if (category.isNullOrBlank()) repo.getProducts()
            else repo.getProductsByCategory(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            categoryRepo.seedDefaultsIfEmpty()
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setQuery(q: String) {
        _query.value = q
    }
}
