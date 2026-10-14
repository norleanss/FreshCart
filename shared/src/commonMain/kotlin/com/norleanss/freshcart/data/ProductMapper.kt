package com.norleanss.freshcart.data

import com.norleanss.freshcart.data.remote.CategoryDto
import com.norleanss.freshcart.data.remote.ProductDto
import com.norleanss.freshcart.model.Category
import com.norleanss.freshcart.model.Money
import com.norleanss.freshcart.model.Product

internal fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand?.takeIf { it.isNotBlank() },
    price = Money.of(price),
    discountPercent = discountPercentage,
    rating = rating,
    stock = stock,
    thumbnailUrl = thumbnail,
    imageUrls = images.ifEmpty { listOfNotNull(thumbnail.takeIf { it.isNotBlank() }) },
)

internal fun CategoryDto.toDomain(): Category = Category(slug = slug, name = name)
