package com.norleanss.freshcart.di

import com.norleanss.freshcart.cart.CartRepository
import com.norleanss.freshcart.cart.InMemoryCartRepository
import com.norleanss.freshcart.data.ProductRepository
import com.norleanss.freshcart.data.ProductRepositoryImpl
import com.norleanss.freshcart.data.remote.ProductApi
import com.norleanss.freshcart.data.remote.createHttpClient

/**
 * Простой ручной DI для общего модуля. Создаётся один раз на процесс
 * (на Android — в Application, на iOS — в AppDelegate).
 */
class SharedModule {
    private val api: ProductApi by lazy { ProductApi(createHttpClient()) }

    val productRepository: ProductRepository by lazy { ProductRepositoryImpl(api) }

    val cartRepository: CartRepository by lazy { InMemoryCartRepository() }
}
