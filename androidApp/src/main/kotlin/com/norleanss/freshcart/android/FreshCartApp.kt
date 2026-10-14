package com.norleanss.freshcart.android

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.norleanss.freshcart.di.SharedModule

class FreshCartApp : Application() {
    /** Зависимости из общего KMP-модуля живут столько же, сколько процесс. */
    val shared: SharedModule by lazy { SharedModule() }
}

/** Достаёт SharedModule внутри viewModelFactory { initializer { ... } }. */
val CreationExtras.shared: SharedModule
    get() = (checkNotNull(this[APPLICATION_KEY]) as FreshCartApp).shared
