package com.norleanss.freshcart.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.norleanss.freshcart.model.Money

@Composable
fun PriceText(price: Money, finalPrice: Money, modifier: Modifier = Modifier, large: Boolean = false) {
    val style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = finalPrice.format(),
            style = style,
            fontWeight = FontWeight.Bold,
            color = if (finalPrice < price) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        if (finalPrice < price) {
            Text(
                text = price.format(),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Кнопка «В корзину», которая после добавления превращается в счётчик количества. */
@Composable
fun AddToCartControl(
    quantity: Int,
    inStock: Boolean,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (quantity == 0) {
        Button(onClick = onAdd, enabled = inStock, modifier = modifier) {
            Text(if (inStock) "В корзину" else "Нет в наличии")
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledTonalIconButton(onClick = onDecrement) {
                Icon(Icons.Default.Remove, contentDescription = "Убрать одну штуку")
            }
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 32.dp),
            )
            FilledTonalIconButton(onClick = onIncrement) {
                Icon(Icons.Default.Add, contentDescription = "Добавить ещё одну")
            }
        }
    }
}

@Composable
fun CartIconButton(count: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(badge = { if (count > 0) Badge { Text(count.toString()) } }) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Корзина, товаров: $count")
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Человекочитаемое сообщение об ошибке без технических деталей. */
fun Throwable.toUserMessage(): String = when (this) {
    is java.io.IOException -> "Нет соединения с сервером. Проверьте интернет."
    else -> "Что-то пошло не так. Попробуйте ещё раз."
}
