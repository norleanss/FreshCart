package com.norleanss.freshcart.data

import com.norleanss.freshcart.data.remote.ProductApi
import com.norleanss.freshcart.model.Category
import com.norleanss.freshcart.model.Product
import kotlin.coroutines.cancellation.CancellationException

/** Страница выдачи каталога. */
data class ProductPage(
    val items: List<Product>,
    val total: Int,
    val skip: Int,
) {
    val hasMore: Boolean get() = skip + items.size < total
}

/** Параметры выборки: поиск по тексту и фильтр по категории взаимоисключающие (ограничение API). */
data class ProductQuery(
    val search: String = "",
    val categorySlug: String? = null,
)

interface ProductRepository {
    suspend fun getProducts(query: ProductQuery, skip: Int, limit: Int = PAGE_SIZE): Result<ProductPage>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun getCategories(): Result<List<Category>>

    companion object {
        const val PAGE_SIZE = 20
    }
}

class ProductRepositoryImpl(private val api: ProductApi) : ProductRepository {

    private var cachedCategories: List<Category>? = null

    override suspend fun getProducts(query: ProductQuery, skip: Int, limit: Int): Result<ProductPage> =
        safeCall {
            val search = query.search.trim()
            val response = when {
                search.isNotEmpty() -> api.searchProducts(search, skip, limit)
                query.categorySlug != null -> api.getProductsByCategory(query.categorySlug, skip, limit)
                else -> api.getProducts(skip, limit)
            }
            ProductPage(
                items = response.products.map { it.toDomain() },
                total = response.total,
                skip = response.skip,
            )
        }

    override suspend fun getProduct(id: Int): Result<Product> =
        safeCall { api.getProduct(id).toDomain() }

    override suspend fun getCategories(): Result<List<Category>> =
        safeCall {
            cachedCategories ?: api.getCategories()
                .map { it.toDomain() }
                .also { cachedCategories = it }
        }
}

/**
 * Аналог runCatching, который не глотает отмену корутины:
 * CancellationException должен пробрасываться дальше.
 */
internal suspend inline fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
