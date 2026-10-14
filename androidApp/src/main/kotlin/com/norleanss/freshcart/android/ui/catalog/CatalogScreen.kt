@file:OptIn(ExperimentalMaterial3Api::class)

package com.norleanss.freshcart.android.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.norleanss.freshcart.android.ui.components.AddToCartControl
import com.norleanss.freshcart.android.ui.components.CartIconButton
import com.norleanss.freshcart.android.ui.components.EmptyState
import com.norleanss.freshcart.android.ui.components.ErrorState
import com.norleanss.freshcart.android.ui.components.PriceText
import com.norleanss.freshcart.model.Category
import com.norleanss.freshcart.model.Product

@Composable
fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог") },
                actions = { CartIconButton(count = state.cartCount, onClick = onCartClick) },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
            if (state.categories.isNotEmpty()) {
                CategoryChips(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onClick = viewModel::onCategoryClick,
                )
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> ErrorState(message = state.error!!, onRetry = viewModel::retry)
                state.isEmptyResult -> EmptyState("Ничего не нашлось. Попробуйте изменить запрос.")
                else -> ProductGrid(
                    state = state,
                    onProductClick = onProductClick,
                    onAdd = viewModel::addToCart,
                    onIncrement = viewModel::increment,
                    onDecrement = viewModel::decrement,
                    onLoadMore = viewModel::loadMore,
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Поиск товаров") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Очистить поиск")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun CategoryChips(categories: List<Category>, selected: String?, onClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.slug }) { category ->
            FilterChip(
                selected = category.slug == selected,
                onClick = { onClick(category.slug) },
                label = { Text(category.name) },
            )
        }
    }
}

@Composable
private fun ProductGrid(
    state: CatalogUiState,
    onProductClick: (Int) -> Unit,
    onAdd: (Product) -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    // Догружаем следующую страницу, когда до конца списка остаётся несколько карточек.
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= gridState.layoutInfo.totalItemsCount - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadMore, state.products.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                quantity = state.cartQuantities[product.id] ?: 0,
                onClick = { onProductClick(product.id) },
                onAdd = { onAdd(product) },
                onIncrement = { onIncrement(product.id) },
                onDecrement = { onDecrement(product.id) },
            )
        }
        if (state.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Card(Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box {
                AsyncImage(
                    model = product.thumbnailUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)),
                )
                if (product.hasDiscount) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(6.dp),
                    ) {
                        Text(
                            text = "-${product.discountPercent.toInt()}%",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            PriceText(price = product.price, finalPrice = product.finalPrice)
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = " ${"%.1f".format(product.rating)}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            AddToCartControl(
                quantity = quantity,
                inStock = product.inStock,
                onAdd = onAdd,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.fillMaxWidth().height(40.dp),
            )
        }
    }
}

private const val PREFETCH_DISTANCE = 4
