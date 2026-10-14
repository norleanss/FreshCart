package com.norleanss.freshcart.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponseDto(
    val products: List<ProductDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: String = "",
    val brand: String? = null,
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val thumbnail: String = "",
    val images: List<String> = emptyList(),
)

@Serializable
data class CategoryDto(
    val slug: String,
    val name: String,
)
