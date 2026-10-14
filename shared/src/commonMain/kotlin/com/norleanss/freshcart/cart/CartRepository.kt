package com.norleanss.freshcart.cart

import com.norleanss.freshcart.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface CartRepository {
    val cart: StateFlow<Cart>
    fun add(product: Product)
    fun increment(productId: Int)
    fun decrement(productId: Int)
    fun remove(productId: Int)
    fun clear()
}

/** Корзина в памяти. Общая для всех экранов и платформ, потокобезопасна за счёт атомарного update. */
class InMemoryCartRepository : CartRepository {

    private val state = MutableStateFlow(Cart())
    override val cart: StateFlow<Cart> = state.asStateFlow()

    override fun add(product: Product) = state.update { it.add(product) }

    override fun increment(productId: Int) = state.update { cart ->
        val item = cart.items.firstOrNull { it.product.id == productId } ?: return@update cart
        cart.setQuantity(item.product, item.quantity + 1)
    }

    override fun decrement(productId: Int) = state.update { cart ->
        val item = cart.items.firstOrNull { it.product.id == productId } ?: return@update cart
        cart.setQuantity(item.product, item.quantity - 1)
    }

    override fun remove(productId: Int) = state.update { it.remove(productId) }

    override fun clear() = state.update { Cart() }
}
