package com.norleanss.freshcart.android.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.norleanss.freshcart.android.shared
import com.norleanss.freshcart.android.ui.components.toUserMessage
import com.norleanss.freshcart.cart.CartRepository
import com.norleanss.freshcart.data.ProductQuery
import com.norleanss.freshcart.data.ProductRepository
import com.norleanss.freshcart.model.Category
import com.norleanss.freshcart.model.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val query: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategory: String? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val canLoadMore: Boolean = false,
    val cartQuantities: Map<Int, Int> = emptyMap(),
    val cartCount: Int = 0,
) {
    val isEmptyResult: Boolean get() = !isLoading && error == null && products.isEmpty()
}

class CatalogViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val state = MutableStateFlow(CatalogUiState())

    val uiState: StateFlow<CatalogUiState> =
        combine(state, cartRepository.cart) { ui, cart ->
            ui.copy(
                cartQuantities = cart.items.associate { it.product.id to it.quantity },
                cartCount = cart.itemsCount,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadCategories()
        reload()
    }

    fun onQueryChange(query: String) {
        // Поиск и фильтр по категории в API взаимоисключающие, поэтому ввод текста сбрасывает категорию.
        state.update { it.copy(query = query, selectedCategory = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            reload()
        }
    }

    fun onCategoryClick(slug: String) {
        searchJob?.cancel()
        state.update {
            it.copy(
                selectedCategory = if (it.selectedCategory == slug) null else slug,
                query = "",
            )
        }
        reload()
    }

    fun retry() {
        if (state.value.categories.isEmpty()) loadCategories()
        reload()
    }

    fun loadMore() {
        val current = state.value
        if (current.isLoading || current.isLoadingMore || !current.canLoadMore) return

        state.update { it.copy(isLoadingMore = true) }
        loadJob = viewModelScope.launch {
            productRepository.getProducts(current.toQuery(), skip = current.products.size)
                .onSuccess { page ->
                    state.update {
                        it.copy(
                            products = (it.products + page.items).distinctBy(Product::id),
                            canLoadMore = page.hasMore,
                            isLoadingMore = false,
                        )
                    }
                }
                .onFailure {
                    // Ошибку догрузки не показываем на весь экран: список уже есть, пользователь доскроллит ещё раз.
                    state.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun addToCart(product: Product) = cartRepository.add(product)
    fun increment(productId: Int) = cartRepository.increment(productId)
    fun decrement(productId: Int) = cartRepository.decrement(productId)

    private fun reload() {
        loadJob?.cancel()
        state.update { it.copy(isLoading = true, isLoadingMore = false, error = null) }
        loadJob = viewModelScope.launch {
            productRepository.getProducts(state.value.toQuery(), skip = 0)
                .onSuccess { page ->
                    state.update {
                        it.copy(products = page.items, canLoadMore = page.hasMore, isLoading = false)
                    }
                }
                .onFailure { error ->
                    state.update {
                        it.copy(products = emptyList(), isLoading = false, error = error.toUserMessage())
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            productRepository.getCategories().onSuccess { categories ->
                state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun CatalogUiState.toQuery() = ProductQuery(search = query, categorySlug = selectedCategory)

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CatalogViewModel(shared.productRepository, shared.cartRepository)
            }
        }
    }
}
