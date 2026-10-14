package com.norleanss.freshcart.cart

import com.norleanss.freshcart.model.Money
import com.norleanss.freshcart.model.Product

data class CartItem(
    val product: Product,
    val quantity: Int,
) {
    val total: Money get() = product.finalPrice * quantity
    val totalWithoutDiscount: Money get() = product.price * quantity
}

/** Неизменяемый снимок корзины; все суммы вычисляются из позиций. */
data class Cart(val items: List<CartItem> = emptyList()) {

    val itemsCount: Int get() = items.sumOf { it.quantity }

    val subtotal: Money get() = items.fold(Money.ZERO) { acc, item -> acc + item.totalWithoutDiscount }

    val total: Money get() = items.fold(Money.ZERO) { acc, item -> acc + item.total }

    val savings: Money get() = subtotal - total

    val isEmpty: Boolean get() = items.isEmpty()

    fun quantityOf(productId: Int): Int = items.firstOrNull { it.product.id == productId }?.quantity ?: 0

    /** Добавляет единицу товара. Количество ограничено остатком на складе. */
    fun add(product: Product): Cart {
        val current = quantityOf(product.id)
        return setQuantity(product, current + 1)
    }

    fun remove(productId: Int): Cart = Cart(items.filterNot { it.product.id == productId })

    /** Устанавливает количество; 0 и меньше удаляет позицию, больше остатка обрезается до остатка. */
    fun setQuantity(product: Product, quantity: Int): Cart {
        val limited = quantity.coerceAtMost(maxOf(product.stock, 0))
        if (limited <= 0) return remove(product.id)

        val index = items.indexOfFirst { it.product.id == product.id }
        val updated = if (index >= 0) {
            items.toMutableList().apply { this[index] = CartItem(product, limited) }
        } else {
            items + CartItem(product, limited)
        }
        return Cart(updated)
    }
}
