package com.norleanss.freshcart.android.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.norleanss.freshcart.android.shared
import com.norleanss.freshcart.cart.Cart
import com.norleanss.freshcart.cart.CartRepository
import kotlinx.coroutines.flow.StateFlow

/** Вся логика корзины (суммы, скидки, лимиты остатка) живёт в общем KMP-модуле. */
class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val cart: StateFlow<Cart> = cartRepository.cart

    fun increment(productId: Int) = cartRepository.increment(productId)
    fun decrement(productId: Int) = cartRepository.decrement(productId)
    fun remove(productId: Int) = cartRepository.remove(productId)
    fun checkout() = cartRepository.clear()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CartViewModel(shared.cartRepository) }
        }
    }
}
