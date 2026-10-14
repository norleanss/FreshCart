package com.norleanss.freshcart.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Платформенный HTTP-движок: OkHttp на Android, Darwin на iOS. */
internal expect fun platformHttpEngine(): HttpClientEngine

internal fun createHttpClient(
    engine: HttpClientEngine = platformHttpEngine(),
    baseUrl: String = ProductApi.BASE_URL,
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
    defaultRequest { url(baseUrl) }
}

/** REST-клиент для публичного API https://dummyjson.com. */
class ProductApi internal constructor(private val client: HttpClient) {

    suspend fun getProducts(skip: Int, limit: Int): ProductsResponseDto =
        client.get("products") {
            parameter("skip", skip)
            parameter("limit", limit)
        }.body()

    suspend fun searchProducts(query: String, skip: Int, limit: Int): ProductsResponseDto =
        client.get("products/search") {
            parameter("q", query)
            parameter("skip", skip)
            parameter("limit", limit)
        }.body()

    suspend fun getProductsByCategory(slug: String, skip: Int, limit: Int): ProductsResponseDto =
        client.get("products/category/$slug") {
            parameter("skip", skip)
            parameter("limit", limit)
        }.body()

    suspend fun getProduct(id: Int): ProductDto = client.get("products/$id").body()

    suspend fun getCategories(): List<CategoryDto> = client.get("products/categories").body()

    companion object {
        const val BASE_URL = "https://dummyjson.com/"
    }
}
