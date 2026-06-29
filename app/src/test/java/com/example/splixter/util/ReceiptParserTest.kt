package com.example.splixter.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptParserTest {

    @Test
    fun testParseReceiptText_extractsINRItemsAndPricesCorrectly() {
        val rawOcrText = """
            ROYAL SPICE RESTAURANT
            MG ROAD, BENGALURU
            TABLE 4 GUESTS 3
            ------------------------
            Paneer Tikka       ₹340.00
            Butter Chicken     ₹520.00
            Dal Makhani        ₹380.00
            Cold Coffee        ₹180.00
            ------------------------
            SUBTOTAL          ₹1420.00
            CGST               ₹35.50
            SGST               ₹35.50
            TOTAL             ₹1491.00
            THANK YOU FOR VISITING!
        """.trimIndent()

        val items = ReceiptParser.parseReceiptText(rawOcrText)

        assertEquals(4, items.size)
        assertEquals("Paneer Tikka", items[0].name)
        assertEquals(340.00, items[0].price, 0.01)

        assertEquals("Butter Chicken", items[1].name)
        assertEquals(520.00, items[1].price, 0.01)

        assertEquals("Dal Makhani", items[2].name)
        assertEquals(380.00, items[2].price, 0.01)

        assertEquals("Cold Coffee", items[3].name)
        assertEquals(180.00, items[3].price, 0.01)
    }

    @Test
    fun testGetSampleBillItems_returnsNonEmptyList() {
        val sampleItems = ReceiptParser.getSampleBillItems()
        assertTrue(sampleItems.isNotEmpty())
        assertTrue(sampleItems.any { it.name.contains("Paneer") || it.name.contains("Biryani") })
    }
}
