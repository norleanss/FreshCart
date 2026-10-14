package com.norleanss.freshcart.android

import com.norleanss.freshcart.android.ui.catalog.CatalogViewModel
import com.norleanss.freshcart.cart.InMemoryCartRepository
import com.norleanss.freshcart.data.ProductPage
import com.norleanss.freshcart.data.ProductQuery
import com.norleanss.freshcart.data.ProductRepository
import com.norleanss.freshcart.model.Category
import com.norleanss.freshcart.model.Money
import com.norleanss.freshcart.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val repository = FakeProductRepository()
    private val cart = InMemoryCartRepository()

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads first page and categories on start`() = runTest {
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(20, state.products.size)
        assertTrue(state.canLoadMore)
        assertEquals(listOf("groceries"), state.categories.map { it.slug })
    }

    @Test
    fun `load more appends next page`() = runTest {
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(25, state.products.size)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `search is debounced and only last query is sent`() = runTest {
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()
        repository.queries.clear()

        viewModel.onQueryChange("m")
        advanceTimeBy(100)
        viewModel.onQueryChange("mi")
        advanceTimeBy(100)
        viewModel.onQueryChange("milk")
        advanceUntilIdle()

        assertEquals(listOf("milk"), repository.queries.map { it.search })
    }

    @Test
    fun `selecting category clears search and toggles off on second click`() = runTest {
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()

        viewModel.onQueryChange("milk")
        viewModel.onCategoryClick("groceries")
        advanceUntilIdle()
        assertEquals("groceries", viewModel.uiState.value.selectedCategory)
        assertEquals("", viewModel.uiState.value.query)

        viewModel.onCategoryClick("groceries")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `network error is shown and retry recovers`() = runTest {
        repository.failNext = true
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.retry()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)
        assertEquals(20, viewModel.uiState.value.products.size)
    }

    @Test
    fun `cart quantities are reflected in ui state`() = runTest {
        val viewModel = CatalogViewModel(repository, cart)
        advanceUntilIdle()
        val product = viewModel.uiState.value.products.first()

        viewModel.addToCart(product)
        viewModel.increment(product.id)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.cartQuantities[product.id])
        assertEquals(2, viewModel.uiState.value.cartCount)
    }
}

private class FakeProductRepository : ProductRepository {
    private val all = (1..25).map { id ->
        Product(
            id = id, title = "Product $id", description = "", category = "groceries", brand = null,
            price = Money.of(1.0), discountPercent = 0.0, rating = 4.0, stock = 5,
            thumbnailUrl = "", imageUrls = emptyList(),
        )
    }
    val queries = mutableListOf<ProductQuery>()
    var failNext = false

    override suspend fun getProducts(query: ProductQuery, skip: Int, limit: Int): Result<ProductPage> {
        queries += query
        if (failNext) {
            failNext = false
            return Result.failure(IOException("offline"))
        }
        return Result.success(ProductPage(all.drop(skip).take(limit), total = all.size, skip = skip))
    }

    override suspend fun getProduct(id: Int): Result<Product> = Result.success(all.first { it.id == id })

    override suspend fun getCategories(): Result<List<Category>> =
        Result.success(listOf(Category("groceries", "Groceries")))
}
