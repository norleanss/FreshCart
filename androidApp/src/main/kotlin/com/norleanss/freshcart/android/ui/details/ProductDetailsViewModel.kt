package com.norleanss.freshcart.android.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.norleanss.freshcart.android.Routes
import com.norleanss.freshcart.android.shared
import com.norleanss.freshcart.android.ui.components.toUserMessage
import com.norleanss.freshcart.cart.CartRepository
import com.norleanss.freshcart.data.ProductRepository
import com.norleanss.freshcart.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductLoadState {
    data object Loading : ProductLoadState
    data class Error(val message: String) : ProductLoadState
    data class Content(val product: Product) : ProductLoadState
}

data class ProductDetailsUiState(
    val load: ProductLoadState = ProductLoadState.Loading,
    val quantityInCart: Int = 0,
    val cartCount: Int = 0,
)

class ProductDetailsViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val load = MutableStateFlow<ProductLoadState>(ProductLoadState.Loading)

    val uiState: StateFlow<ProductDetailsUiState> =
        combine(load, cartRepository.cart) { load, cart ->
            ProductDetailsUiState(
                load = load,
                quantityInCart = cart.quantityOf(productId),
                cartCount = cart.itemsCount,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ProductDetailsUiState())

    init {
        loadProduct()
    }

    fun retry() = loadProduct()

    fun addToCart() {
        (load.value as? ProductLoadState.Content)?.let { cartRepository.add(it.product) }
    }

    fun increment() = cartRepository.increment(productId)
    fun decrement() = cartRepository.decrement(productId)

    private fun loadProduct() {
        load.value = ProductLoadState.Loading
        viewModelScope.launch {
            load.value = productRepository.getProduct(productId).fold(
                onSuccess = { ProductLoadState.Content(it) },
                onFailure = { ProductLoadState.Error(it.toUserMessage()) },
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val handle: SavedStateHandle = createSavedStateHandle()
                ProductDetailsViewModel(
                    productId = checkNotNull(handle.get<Int>(Routes.PRODUCT_ID_ARG)),
                    productRepository = shared.productRepository,
                    cartRepository = shared.cartRepository,
                )
            }
        }
    }
}
