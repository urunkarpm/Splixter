package com.example.splixter.ui

import com.example.splixter.data.BillItem
import com.example.splixter.data.Person
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SplitterViewModelTest {

    private lateinit var viewModel: SplitterViewModel

    @Before
    fun setUp() {
        viewModel = SplitterViewModel()
    }

    @Test
    fun testAddAndRemovePerson() {
        val initialCount = viewModel.uiState.value.people.size
        viewModel.addPerson("Dave")
        assertEquals(initialCount + 1, viewModel.uiState.value.people.size)

        val dave = viewModel.uiState.value.people.last()
        assertEquals("Dave", dave.name)

        viewModel.removePerson(dave.id)
        assertEquals(initialCount, viewModel.uiState.value.people.size)
    }

    @Test
    fun testCalculateBreakdown_proportionalTaxAndTip() {
        viewModel.addPerson("Alice")
        viewModel.addPerson("Bob")
        val people = viewModel.uiState.value.people
        val p1 = people[people.size - 2]
        val p2 = people[people.size - 1]

        val item1 = BillItem(id = "i1", name = "Paneer Tikka", price = 300.0, assignedPersonIds = setOf(p1.id))
        val item2 = BillItem(id = "i2", name = "Cold Coffee", price = 100.0, assignedPersonIds = setOf(p2.id))

        viewModel.setItems(listOf(item1, item2))
        viewModel.updateTaxAndTip(taxAmount = 40.0, tipAmount = 40.0)

        val breakdowns = viewModel.calculateBreakdown()
        val b1 = breakdowns.first { it.person.id == p1.id }
        val b2 = breakdowns.first { it.person.id == p2.id }

        // Subtotals: p1 = 300, p2 = 100. Total subtotal = 400.
        assertEquals(300.0, b1.subtotal, 0.01)
        assertEquals(100.0, b2.subtotal, 0.01)

        // Tax share: p1 = 300/400 * 40 = 30.0, p2 = 100/400 * 40 = 10.0
        assertEquals(30.0, b1.taxShare, 0.01)
        assertEquals(10.0, b2.taxShare, 0.01)

        // Tip share: total tip = 40.0. p1 = 30.0, p2 = 10.0
        assertEquals(30.0, b1.tipShare, 0.01)
        assertEquals(10.0, b2.tipShare, 0.01)

        // Grand totals: p1 = 300 + 30 + 30 = 360, p2 = 100 + 10 + 10 = 120
        assertEquals(360.0, b1.grandTotal, 0.01)
        assertEquals(120.0, b2.grandTotal, 0.01)
    }

    @Test
    fun testCalculateBreakdown_sharedItemAndUnassignedFallback() {
        viewModel.addPerson("Charlie")
        viewModel.addPerson("David")
        val people = viewModel.uiState.value.people
        val p1 = people[people.size - 2]
        val p2 = people[people.size - 1]

        // Shared item between both (200 / 2 = 100 each)
        val sharedItem = BillItem(id = "i1", name = "Garlic Bread", price = 200.0, assignedPersonIds = setOf(p1.id, p2.id))
        // Unassigned item (100 -> fallback split equally 50 each)
        val unassignedItem = BillItem(id = "i2", name = "Water Bottle", price = 100.0, assignedPersonIds = emptySet())

        viewModel.setItems(listOf(sharedItem, unassignedItem))
        viewModel.updateTaxAndTip(taxAmount = 30.0, tipAmount = 15.0)

        val breakdowns = viewModel.calculateBreakdown()
        val b1 = breakdowns.first { it.person.id == p1.id }
        val b2 = breakdowns.first { it.person.id == p2.id }

        // Expected subtotals: p1 = 100 + 50 = 150, p2 = 100 + 50 = 150
        assertEquals(150.0, b1.subtotal, 0.01)
        assertEquals(150.0, b2.subtotal, 0.01)

        // Total grand sum across all breakdowns must equal total bill (200 + 100 + 30 + 15 = 345.0)
        val totalSum = breakdowns.sumOf { it.grandTotal }
        assertEquals(345.0, totalSum, 0.01)
    }
}
