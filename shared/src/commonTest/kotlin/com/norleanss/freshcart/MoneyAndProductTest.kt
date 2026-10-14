package com.norleanss.freshcart

import com.norleanss.freshcart.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoneyAndProductTest {

    @Test
    fun moneyIsConvertedToCentsWithoutFloatingPointDrift() {
        assertEquals(30L, (Money.of(0.1) + Money.of(0.2)).cents)
        assertEquals(1999L, Money.of(19.99).cents)
    }

    @Test
    fun moneyIsFormattedWithTwoFractionDigits() {
        assertEquals("$12.05", Money(1205).format())
        assertEquals("$0.00", Money.ZERO.format())
        assertEquals("-$1.50", Money(-150).format())
    }

    @Test
    fun finalPriceAppliesDiscountAndRoundsToCents() {
        val product = testProduct(price = 9.99, discount = 10.0)
        assertEquals(899L, product.finalPrice.cents) // 8.991 -> 8.99
        assertTrue(product.hasDiscount)
    }

    @Test
    fun invalidDiscountIsClamped() {
        assertEquals(1000L, testProduct(price = 10.0, discount = -5.0).finalPrice.cents)
        assertEquals(0L, testProduct(price = 10.0, discount = 150.0).finalPrice.cents)
    }

    @Test
    fun productWithoutDiscountHasNoDiscountFlag() {
        assertFalse(testProduct(discount = 0.0).hasDiscount)
    }
}
