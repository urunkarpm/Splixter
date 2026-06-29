package com.example.splixter.data

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class ItemCategory {
    FOOD,
    LIQUOR
}

@Immutable
data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long, // ARGB color format
    val emoji: String = "🍕"
)

@Immutable
data class BillItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val assignedPersonIds: Set<String> = emptySet(),
    val category: ItemCategory = ItemCategory.FOOD
)

data class TaxAndTip(
    val taxAmount: Double = 0.0,
    val tipAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val isTaxPercentage: Boolean = false,
    val taxPercentage: Double = 0.0,
    val isTipPercentage: Boolean = false,
    val tipPercentage: Double = 0.0,
    val isDiscountPercentage: Boolean = false,
    val discountPercentage: Double = 0.0
)

data class PersonBreakdown(
    val person: Person,
    val items: List<Pair<BillItem, Double>>,
    val subtotal: Double,
    val discountShare: Double = 0.0,
    val taxShare: Double,
    val tipShare: Double,
    val grandTotal: Double
)

enum class AppStep {
    SPLASH,
    PEOPLE,
    SCAN,
    ASSIGN,
    RECEIPT
}

data class BillHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val people: List<Person>,
    val items: List<BillItem>,
    val taxAndTip: TaxAndTip,
    val paidByPersonId: String?,
    val totalAmount: Double
)

data class SavedGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val members: List<Person>
)
