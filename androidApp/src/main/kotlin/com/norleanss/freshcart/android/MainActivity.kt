package com.norleanss.freshcart.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.norleanss.freshcart.android.ui.cart.CartScreen
import com.norleanss.freshcart.android.ui.catalog.CatalogScreen
import com.norleanss.freshcart.android.ui.details.ProductDetailsScreen
import com.norleanss.freshcart.android.ui.theme.FreshCartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshCartTheme {
                AppNavHost()
            }
        }
    }
}

object Routes {
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PRODUCT_ID_ARG = "productId"
    const val PRODUCT = "product/{$PRODUCT_ID_ARG}"

    fun product(id: Int) = "product/$id"
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.CATALOG) {
        composable(Routes.CATALOG) {
            CatalogScreen(
                onProductClick = { navController.navigate(Routes.product(it)) },
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }
        composable(
            route = Routes.PRODUCT,
            arguments = listOf(navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.IntType }),
        ) {
            ProductDetailsScreen(
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }
        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { navController.navigate(Routes.product(it)) },
            )
        }
    }
}
