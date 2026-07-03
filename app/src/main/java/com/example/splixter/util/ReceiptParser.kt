package com.example.splixter.util

import com.example.splixter.data.BillItem
import java.util.Locale

object ReceiptParser {

    // Flexible regex matching numbers with optional decimals or currency prefixes/suffixes
    private val PRICE_REGEX = Regex("""(?:\$|₹|Rs\.?\s*)?(\d{1,5}(?:\.\d{1,2})?)(?:/-)?\b""", RegexOption.IGNORE_CASE)
    
    private val IGNORE_WORDS = setOf(
        "total", "subtotal", "tax", "gst", "cgst", "sgst", "cash", "change", "upi", "visa", "mastercard", 
        "balance", "due", "gratuity", "receipt", "thank", "you", "date", "time", "table", "server", 
        "guest", "check", "bill", "invoice", "net", "amount", "welcome", "tel", "phone"
    )

    fun parseReceiptText(text: String): List<BillItem> {
        val lines = text.split("\n")
        val items = mutableListOf<BillItem>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length < 2) continue

            val lowerLine = trimmed.lowercase(Locale.ROOT)
            if (IGNORE_WORDS.any { lowerLine.contains(it) }) {
                continue
            }

            // Find all numbers in line
            val matches = PRICE_REGEX.findAll(trimmed).toList()
            if (matches.isNotEmpty()) {
                // Take the last or best price match
                val bestMatch = matches.lastOrNull { 
                    val valD = it.groupValues[1].toDoubleOrNull() ?: 0.0
                    valD in 1.0..50000.0 
                } ?: matches.last()

                val priceStr = bestMatch.groupValues[1]
                val price = priceStr.toDoubleOrNull() ?: continue
                if (price < 1.0 || price > 50000.0) continue

                // Extract item name by stripping out the matched price and currency markers
                var itemName = trimmed.replace(bestMatch.value, "")
                    .replace("₹", "")
                    .replace("$", "")
                    .replace(Regex("""(?i)\brs\.?\b"""), "")
                    .replace(Regex("""^[\d\.xX\*\-]+\s+"""), "")
                    .replace(Regex("""\s+[\d\.xX\*\-]+$"""), "")
                    .trim()

                if (itemName.length < 2) {
                    // Fallback to cleaned original line without numbers
                    itemName = trimmed.replace(Regex("""[\d\.\$₹/\-]+"""), "").trim()
                }

                if (itemName.length >= 2) {
                    items.add(BillItem(name = itemName.capitalizeWords(), price = price, category = com.example.splixter.data.ItemCategory.guessFromName(itemName)))
                }
            }
        }

        return items
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ").joinToString(" ") { word ->
            word.split("-").joinToString("-") { subWord ->
                if (subWord.length > 1 && subWord.all { c -> c.isUpperCase() }) {
                    subWord
                } else {
                    subWord.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            }
        }
    }

    fun getSampleBillItems(): List<BillItem> {
        return listOf(
            BillItem(name = "Paneer Tikka Platter", price = 340.00, category = com.example.splixter.data.ItemCategory.FOOD),
            BillItem(name = "Butter Chicken & Naan", price = 520.00, category = com.example.splixter.data.ItemCategory.FOOD),
            BillItem(name = "Craft Beer Pint", price = 280.00, category = com.example.splixter.data.ItemCategory.LIQUOR),
            BillItem(name = "Red Wine Glass", price = 450.00, category = com.example.splixter.data.ItemCategory.LIQUOR),
            BillItem(name = "Dal Makhani Large", price = 380.00, category = com.example.splixter.data.ItemCategory.FOOD),
            BillItem(name = "Hyderabadi Chicken Biryani", price = 460.00, category = com.example.splixter.data.ItemCategory.FOOD),
            BillItem(name = "Cold Coffee with Ice Cream", price = 180.00, category = com.example.splixter.data.ItemCategory.FOOD),
            BillItem(name = "Gulab Jamun", price = 120.00, category = com.example.splixter.data.ItemCategory.FOOD)
        )
    }
}
