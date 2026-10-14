package com.norleanss.freshcart

import com.norleanss.freshcart.data.ProductQuery
import com.norleanss.freshcart.data.ProductRepositoryImpl
import com.norleanss.freshcart.data.remote.ProductApi
import com.norleanss.freshcart.data.remote.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductRepositoryTest {

    private val requestedPaths = mutableListOf<String>()

    private fun repository(status: HttpStatusCode = HttpStatusCode.OK, body: String = PRODUCTS_JSON): ProductRepositoryImpl {
        val engine = MockEngine { request ->
            requestedPaths += request.url.encodedPath + "?" + request.url.encodedQuery
            if (status == HttpStatusCode.OK) {
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                respondError(status)
            }
        }
        return ProductRepositoryImpl(ProductApi(createHttpClient(engine, "https://test.local/")))
    }

    @Test
    fun mapsDtoToDomainAndComputesPagination() = runTest {
        val page = repository().getProducts(ProductQuery(), skip = 0, limit = 1).getOrThrow()

        val product = page.items.single()
        assertEquals("Apple", product.title)
        assertEquals(199L, product.price.cents)
        assertNull(product.brand) // пустой бренд превращается в null
        assertTrue(page.hasMore)
    }

    @Test
    fun searchQueryHasPriorityOverCategory() = runTest {
        repository().getProducts(ProductQuery(search = "  milk ", categorySlug = "groceries"), skip = 0, limit = 20)

        val path = requestedPaths.single()
        assertTrue(path.startsWith("/products/search"), path)
        assertTrue(path.contains("q=milk"), path)
    }

    @Test
    fun categoryFilterUsesCategoryEndpoint() = runTest {
        repository().getProducts(ProductQuery(categorySlug = "groceries"), skip = 20, limit = 20)

        val path = requestedPaths.single()
        assertTrue(path.startsWith("/products/category/groceries"), path)
        assertTrue(path.contains("skip=20"), path)
    }

    @Test
    fun serverErrorIsReturnedAsFailure() = runTest {
        val result = repository(status = HttpStatusCode.InternalServerError).getProduct(1)
        assertTrue(result.isFailure)
    }

    @Test
    fun categoriesAreCachedAfterFirstRequest() = runTest {
        val repo = repository(body = CATEGORIES_JSON)
        repo.getCategories().getOrThrow()
        val second = repo.getCategories().getOrThrow()

        assertEquals(1, requestedPaths.size)
        assertEquals("Groceries", second.single().name)
    }

    private companion object {
        const val PRODUCTS_JSON = """
            {
              "products": [
                {
                  "id": 1, "title": "Apple", "description": "Fresh", "category": "groceries",
                  "brand": "", "price": 1.99, "discountPercentage": 5.0, "rating": 4.6,
                  "stock": 50, "thumbnail": "https://img/1.png", "images": [], "unknownField": true
                }
              ],
              "total": 30, "skip": 0, "limit": 1
            }
        """

        const val CATEGORIES_JSON = """
            [ { "slug": "groceries", "name": "Groceries", "url": "https://dummyjson.com/products/category/groceries" } ]
        """
    }
}
