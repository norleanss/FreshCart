package com.norleanss.freshcart

import com.norleanss.freshcart.model.Money
import com.norleanss.freshcart.model.Product

fun testProduct(
    id: Int = 1,
    price: Double = 10.0,
    discount: Double = 0.0,
    stock: Int = 10,
) = Product(
    id = id,
    title = "Product $id",
    description = "",
    category = "groceries",
    brand = null,
    price = Money.of(price),
    discountPercent = discount,
    rating = 4.5,
    stock = stock,
    thumbnailUrl = "",
    imageUrls = emptyList(),
)
