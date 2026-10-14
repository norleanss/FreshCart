package com.norleanss.freshcart

import com.norleanss.freshcart.cart.Cart
import com.norleanss.freshcart.cart.InMemoryCartRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartTest {

    @Test
    fun addingTheSameProductIncreasesQuantityInsteadOfDuplicating() {
        val product = testProduct(id = 1)
        val cart = Cart().add(product).add(product)

        assertEquals(1, cart.items.size)
        assertEquals(2, cart.quantityOf(1))
        assertEquals(2, cart.itemsCount)
    }

    @Test
    fun quantityIsLimitedByStock() {
        val product = testProduct(stock = 2)
        val cart = Cart().add(product).add(product).add(product)

        assertEquals(2, cart.quantityOf(product.id))
    }

    @Test
    fun outOfStockProductIsNotAdded() {
        val cart = Cart().add(testProduct(stock = 0))
        assertTrue(cart.isEmpty)
    }

    @Test
    fun settingZeroQuantityRemovesItem() {
        val product = testProduct()
        val cart = Cart().add(product).setQuantity(product, 0)
        assertTrue(cart.isEmpty)
    }

    @Test
    fun totalsAndSavingsAreCalculatedFromItems() {
        val discounted = testProduct(id = 1, price = 10.0, discount = 20.0) // 8.00
        val regular = testProduct(id = 2, price = 2.5)                        // 2.50
        val cart = Cart()
            .setQuantity(discounted, 2)
            .setQuantity(regular, 3)

        assertEquals(2750L, cart.subtotal.cents) // 20.00 + 7.50
        assertEquals(2350L, cart.total.cents)    // 16.00 + 7.50
        assertEquals(400L, cart.savings.cents)
        assertEquals(5, cart.itemsCount)
    }

    @Test
    fun repositoryIncrementsDecrementsAndClearsCart() {
        val repository = InMemoryCartRepository()
        val product = testProduct(id = 7)

        repository.add(product)
        repository.increment(7)
        assertEquals(2, repository.cart.value.quantityOf(7))

        repository.decrement(7)
        repository.decrement(7)
        assertTrue(repository.cart.value.isEmpty)

        repository.add(product)
        repository.clear()
        assertTrue(repository.cart.value.isEmpty)
    }

    @Test
    fun incrementOfUnknownProductDoesNothing() {
        val repository = InMemoryCartRepository()
        repository.increment(42)
        assertTrue(repository.cart.value.isEmpty)
    }
}
