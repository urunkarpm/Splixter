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
                "beer", "lager", "ale", "stout", "cider", "draught", "pint", "corona", "budweiser", "bira", "tuborg", "carlsberg", "heineken",
                "wine", "red wine", "white wine", "champagne", "prosecco", "cabernet", "merlot", "chardonnay", "shiraz", "sauvignon",
                "whiskey", "whisky", "scotch", "bourbon", "single malt", "jack daniel", "jim beam", "glenfiddich", "jameson", "ballantine", "chivas", "black label", "red label",
                "vodka", "gin", "rum", "tequila", "brandy", "cognac", "liqueur", "absinthe", "smirnoff", "bacardi", "absolut", "grey goose",
                "cocktail", "margarita", "martini", "mojito", "daiquiri", "negroni", "mimosa", "sangria", "cosmopolitan", "liit", "long island", "gin tonic", "tonic",
                "shot", "shots", "liquor", "alcohol", "booze", "breezer", "toddy", "old monk", "kingfisher", "jagermeister", "jager", "baileys", "kahlua", "aperol", "campari", "sake"
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
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long = 0xFF6C5CE7,
    val emoji: String = "😎",
    val phoneNumber: String? = null,
    val upiId: String? = null
)

@Immutable
data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long, // ARGB color format
    val emoji: String = "🍕",
    val phoneNumber: String? = null,
    val upiId: String? = null,
    val isCurrentUser: Boolean = false
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

enum class CalculationMode {
    SINGLE_BILL,
    TRIP_EXPENSE
}

enum class AppStep {
    SPLASH,
    USER_PROFILE_SETUP,
    MODE_SELECTION,
    LOBBY_HUB,
    PEOPLE,
    SCAN,
    ASSIGN,
    RECEIPT,
    TRIP_EXPENSES,
    TRIP_SUMMARY
}

@Immutable
data class LobbySession(
    val code: String, // e.g. "SPLIX-4892"
    val name: String, // e.g. "Goa Vacation 2026"
    val hostPersonId: String,
    val members: List<Person> = emptyList(),
    val expenses: List<TripExpense> = emptyList(),
    val settlements: List<TripSettlementRecord> = emptyList(),
    val activities: List<TripActivity> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
data class TripExpense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val paidByPersonId: String,
    val splitWithPersonIds: Set<String> = emptySet(), // Empty set means split equally among all trip participants
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class TripSettlementRecord(
    val id: String = UUID.randomUUID().toString(),
    val lobbyCode: String,
    val fromPersonId: String,
    val toPersonId: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionRef: String? = null
)

@Immutable
data class TripActivity(
    val id: String = UUID.randomUUID().toString(),
    val lobbyCode: String,
    val actorPersonId: String,
    val actorName: String,
    val actionType: String, // EXPENSE_ADDED, EXPENSE_DELETED, SETTLEMENT_PAID, MEMBER_JOINED
    val description: String,
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class TripPersonBalance(
    val person: Person,
    val totalPaid: Double,
    val totalOwed: Double,
    val netBalance: Double // totalPaid - totalOwed. Positive = gets back, Negative = owes
)

data class TripSettlement(
    val fromPerson: Person, // Debtor
    val toPerson: Person,   // Creditor
    val amount: Double       // Net settled amount after deducting mutual debts
)

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


