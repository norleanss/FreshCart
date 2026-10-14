@file:OptIn(ExperimentalMaterial3Api::class)

package com.norleanss.freshcart.android.ui.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.norleanss.freshcart.android.ui.components.EmptyState
import com.norleanss.freshcart.cart.Cart
import com.norleanss.freshcart.cart.CartItem
import com.norleanss.freshcart.model.Money

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: CartViewModel = viewModel(factory = CartViewModel.Factory),
) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    var orderPlaced by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        bottomBar = {
            if (!cart.isEmpty) {
                CartSummary(cart = cart, onCheckout = {
                    viewModel.checkout()
                    orderPlaced = true
                })
            }
        },
    ) { padding ->
        if (cart.isEmpty) {
            EmptyState("Корзина пуста. Добавьте товары из каталога.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(cart.items, key = { it.product.id }) { item ->
                    CartRow(
                        item = item,
                        onClick = { onProductClick(item.product.id) },
                        onIncrement = { viewModel.increment(item.product.id) },
                        onDecrement = { viewModel.decrement(item.product.id) },
                        onRemove = { viewModel.remove(item.product.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (orderPlaced) {
        AlertDialog(
            onDismissRequest = { orderPlaced = false },
            confirmButton = { TextButton(onClick = { orderPlaced = false }) { Text("Отлично") } },
            title = { Text("Заказ оформлен") },
            text = { Text("Это демо-приложение: заказ никуда не отправлен, корзина очищена.") },
        )
    }
}

@Composable
private fun CartRow(
    item: CartItem,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.product.thumbnailUrl,
            contentDescription = item.product.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.product.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${item.product.finalPrice.format()} × ${item.quantity} = ${item.total.format()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AddToCartControl(
                quantity = item.quantity,
                inStock = item.product.inStock,
                onAdd = onIncrement,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.width(140.dp).height(40.dp),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить из корзины")
        }
    }
}

@Composable
private fun CartSummary(cart: Cart, onCheckout: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryLine("Товары (${cart.itemsCount} шт.)", cart.subtotal)
            if (cart.savings > Money.ZERO) {
                SummaryLine("Скидка", Money(-cart.savings.cents), highlight = true)
            }
            SummaryLine("Итого", cart.total, bold = true)
            Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Оформить заказ")
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: Money, bold: Boolean = false, highlight: Boolean = false) {
    val style = if (bold) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
    val color = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, color = color)
        Text(value.format(), style = style, color = color)
    }
}
