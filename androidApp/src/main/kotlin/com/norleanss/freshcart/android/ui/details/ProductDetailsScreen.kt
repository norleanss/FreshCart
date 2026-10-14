@file:OptIn(ExperimentalMaterial3Api::class)

package com.norleanss.freshcart.android.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.norleanss.freshcart.android.ui.components.AddToCartControl
import com.norleanss.freshcart.android.ui.components.CartIconButton
import com.norleanss.freshcart.android.ui.components.ErrorState
import com.norleanss.freshcart.android.ui.components.PriceText
import com.norleanss.freshcart.model.Product

@Composable
fun ProductDetailsScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailsViewModel = viewModel(factory = ProductDetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Товар") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = { CartIconButton(count = state.cartCount, onClick = onCartClick) },
            )
        },
        bottomBar = {
            val content = state.load as? ProductLoadState.Content
            if (content != null) {
                Surface(tonalElevation = 3.dp) {
                    AddToCartControl(
                        quantity = state.quantityInCart,
                        inStock = content.product.inStock,
                        onAdd = viewModel::addToCart,
                        onIncrement = viewModel::increment,
                        onDecrement = viewModel::decrement,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val load = state.load) {
                ProductLoadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ProductLoadState.Error -> ErrorState(load.message, onRetry = viewModel::retry)
                is ProductLoadState.Content -> ProductContent(load.product)
            }
        }
    }
}

@Composable
private fun ProductContent(product: Product) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val pagerState = rememberPagerState { product.imageUrls.size }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(300.dp)) { page ->
            AsyncImage(
                model = product.imageUrls[page],
                contentDescription = product.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (product.imageUrls.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1} / ${product.imageUrls.size}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.title, style = MaterialTheme.typography.headlineSmall)
            product.brand?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PriceText(price = product.price, finalPrice = product.finalPrice, large = true)
            Text(
                text = "★ ${"%.1f".format(product.rating)} · В наличии: ${product.stock} шт.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(product.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
