package com.norleanss.freshcart.model

import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Денежная сумма в центах. Целочисленная арифметика исключает ошибки округления Double
 * при сложении позиций в корзине.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun minus(other: Money): Money = Money(cents - other.cents)
    operator fun times(quantity: Int): Money = Money(cents * quantity)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    /** Форматирует сумму как "$12.05" без зависимости от платформенных форматтеров. */
    fun format(): String {
        val sign = if (cents < 0) "-" else ""
        val absolute = abs(cents)
        val whole = absolute / 100
        val fraction = (absolute % 100).toString().padStart(2, '0')
        return "${sign}\$${whole}.${fraction}"
    }

    companion object {
        val ZERO = Money(0)

        fun of(amount: Double): Money = Money((amount * 100).roundToLong())
    }
}
