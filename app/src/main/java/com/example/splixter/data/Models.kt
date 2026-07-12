package com.example.splixter.data

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class ItemCategory {
    FOOD,
    LIQUOR;

    companion object {
        fun guessFromName(name: String): ItemCategory {
            val lower = name.lowercase(java.util.Locale.US).trim()
            val liquorKeywords = listOf(
                "beer", "lager", "ale", "stout", "cider", "draught", "pint", "corona", "budweiser",
                "wine", "red wine", "white wine", "champagne", "prosecco", "cabernet", "merlot", "chardonnay", "shiraz", "sauvignon",
                "whiskey", "whisky", "scotch", "bourbon", "single malt", "jack daniel", "jim beam", "glenfiddich", "jameson",
                "vodka", "gin", "rum", "tequila", "brandy", "cognac", "liqueur", "absinthe", "smirnoff", "bacardi",
                "cocktail", "margarita", "martini", "mojito", "daiquiri", "negroni", "mimosa", "sangria", "cosmopolitan",
                "shot", "shots", "liquor", "alcohol", "booze", "breezer", "toddy", "old monk", "kingfisher"
            )
            for (keyword in liquorKeywords) {
                if (lower.contains(keyword)) {
                    return LIQUOR
                }
            }
            return FOOD
        }
    }
}

@Immutable
data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long, // ARGB color format
    val emoji: String = "🍕",
    val phoneNumber: String? = null
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
    val isTaxPercentage: Boolean = true,
    val taxPercentage: Double = 0.0,
    val isTipPercentage: Boolean = false,
    val tipPercentage: Double = 0.0,
    val isDiscountPercentage: Boolean = false,
    val discountPercentage: Double = 0.0,
    val vatAmount: Double = 0.0,
    val isVatPercentage: Boolean = true,
    val vatPercentage: Double = 0.0
)

data class PersonBreakdown(
    val person: Person,
    val items: List<Pair<BillItem, Double>>,
    val subtotal: Double,
    val discountShare: Double = 0.0,
    val taxShare: Double,
    val tipShare: Double,
    val vatShare: Double = 0.0,
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
