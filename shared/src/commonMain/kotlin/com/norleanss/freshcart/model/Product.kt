package com.norleanss.freshcart.model

import kotlin.math.roundToLong

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val price: Money,
    val discountPercent: Double,
    val rating: Double,
    val stock: Int,
    val thumbnailUrl: String,
    val imageUrls: List<String>,
) {
    /** Цена с учётом скидки, округлённая до цента. */
    val finalPrice: Money
        get() {
            val percent = discountPercent.coerceIn(0.0, 100.0)
            return Money((price.cents * (100.0 - percent) / 100.0).roundToLong())
        }

    val hasDiscount: Boolean get() = finalPrice < price

    val inStock: Boolean get() = stock > 0
}

data class Category(
    val slug: String,
    val name: String,
)
