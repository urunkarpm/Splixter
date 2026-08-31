package com.example.splixter.ui

import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

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

        val item1 = BillItem(id = "i1", name = "Paneer Tikka", price = 300.0, category = ItemCategory.FOOD, assignedPersonIds = setOf(p1.id))
        val item2 = BillItem(id = "i2", name = "Cold Coffee", price = 100.0, category = ItemCategory.FOOD, assignedPersonIds = setOf(p2.id))

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

        // Total sum matches exact bill
        assertEquals(480.0, breakdowns.sumOf { it.grandTotal }, 0.01)
    }

    @Test
    fun testCalculateBreakdown_foodGstAndLiquorVatAndDiscount() {
        viewModel.addPerson("Alice")
        viewModel.addPerson("Bob")
        val people = viewModel.uiState.value.people
        val p1 = people[people.size - 2]
        val p2 = people[people.size - 1]

        // p1 has Food (₹500), p2 has Liquor (₹500)
        val item1 = BillItem(id = "i1", name = "Biryani", price = 500.0, category = ItemCategory.FOOD, assignedPersonIds = setOf(p1.id))
        val item2 = BillItem(id = "i2", name = "Cocktail", price = 500.0, category = ItemCategory.LIQUOR, assignedPersonIds = setOf(p2.id))

        viewModel.setItems(listOf(item1, item2))
        // 5% Food GST = ₹25, 10% Liquor VAT = ₹50, 10% Discount = ₹107.5
        viewModel.updateTaxAndTip(
            taxAmount = 25.0,
            vatAmount = 50.0,
            discountAmount = 100.0,
            tipAmount = 50.0
        )

        val breakdowns = viewModel.calculateBreakdown()
        val b1 = breakdowns.first { it.person.id == p1.id }
        val b2 = breakdowns.first { it.person.id == p2.id }

        // Food GST must apply ONLY to Alice (b1)
        assertEquals(25.0, b1.taxShare, 0.01)
        assertEquals(0.0, b2.taxShare, 0.01)

        // Liquor VAT must apply ONLY to Bob (b2)
        assertEquals(0.0, b1.vatShare, 0.01)
        assertEquals(50.0, b2.vatShare, 0.01)

        // Discount split proportionally (50% each = ₹50 each)
        assertEquals(50.0, b1.discountShare, 0.01)
        assertEquals(50.0, b2.discountShare, 0.01)

        // Total bill: 1000 - 100 + 25 + 50 + 50 = 1025.0
        val totalExpected = 1025.0
        assertEquals(totalExpected, breakdowns.sumOf { it.grandTotal }, 0.01)
    }

    @Test
    fun testTripBalances_zeroSumConservation() {
        viewModel.addPerson("A")
        viewModel.addPerson("B")
        viewModel.addPerson("C")
        viewModel.addPerson("D")
        val people = viewModel.uiState.value.people
        val (a, b, c, d) = people.takeLast(4)

        // A pays ₹1200 for everyone (₹300 each)
        viewModel.addTripExpense("Hotel", 1200.0, a.id, people.map { it.id }.toSet())
        // B pays ₹600 for B and C only (₹300 each)
        viewModel.addTripExpense("Taxi", 600.0, b.id, setOf(b.id, c.id))
        // C pays ₹400 for A, B, C, D (₹100 each)
        viewModel.addTripExpense("Lunch", 400.0, c.id, people.map { it.id }.toSet())

        val balances = viewModel.calculateTripBalances()

        // Zero-sum conservation check
        val netSum = balances.sumOf { it.netBalance }
        assertEquals(0.0, netSum, 0.0001)

        // A paid 1200, owed 300 + 100 = 400 -> net = +800
        val balA = balances.first { it.person.id == a.id }
        assertEquals(800.0, balA.netBalance, 0.01)

        // B paid 600, owed 300 + 300 + 100 = 700 -> net = -100
        val balB = balances.first { it.person.id == b.id }
        assertEquals(-100.0, balB.netBalance, 0.01)

        // C paid 400, owed 300 + 300 + 100 = 700 -> net = -300
        val balC = balances.first { it.person.id == c.id }
        assertEquals(-300.0, balC.netBalance, 0.01)

        // D paid 0, owed 300 + 100 = 400 -> net = -400
        val balD = balances.first { it.person.id == d.id }
        assertEquals(-400.0, balD.netBalance, 0.01)
    }

    @Test
    fun testTripSettlements_optimalMinTransfersAndZeroLeftover() {
        viewModel.addPerson("A")
        viewModel.addPerson("B")
        viewModel.addPerson("C")
        viewModel.addPerson("D")
        val people = viewModel.uiState.value.people
        val (a, b, c, d) = people.takeLast(4)

        viewModel.addTripExpense("Hotel", 1200.0, a.id, people.map { it.id }.toSet())
        viewModel.addTripExpense("Taxi", 600.0, b.id, setOf(b.id, c.id))
        viewModel.addTripExpense("Lunch", 400.0, c.id, people.map { it.id }.toSet())

        val settlements = viewModel.calculateTripSettlements()

        // Total settlement amount must exactly equal total debt (+800)
        val totalSettled = settlements.sumOf { it.amount }
        assertEquals(800.0, totalSettled, 0.01)

        // All debts go to A (the only creditor)
        for (s in settlements) {
            assertEquals(a.id, s.toPerson.id)
        }

        // D owes 400, C owes 300, B owes 100 -> exactly 3 transfers
        assertEquals(3, settlements.size)
        assertTrue(settlements.any { it.fromPerson.id == d.id && abs(it.amount - 400.0) < 0.01 })
        assertTrue(settlements.any { it.fromPerson.id == c.id && abs(it.amount - 300.0) < 0.01 })
        assertTrue(settlements.any { it.fromPerson.id == b.id && abs(it.amount - 100.0) < 0.01 })
    }

    @Test
    fun testOddDivision_paisaExactness() {
        viewModel.addPerson("P1")
        viewModel.addPerson("P2")
        viewModel.addPerson("P3")
        val people = viewModel.uiState.value.people
        val (p1, p2, p3) = people.takeLast(3)

        // ₹100 split among 3 people: exactly 33.34 + 33.33 + 33.33 = 100.00
        viewModel.addTripExpense("Dinner", 100.0, p1.id, setOf(p1.id, p2.id, p3.id))

        val balances = viewModel.calculateTripBalances()
        val totalOwed = balances.sumOf { it.totalOwed }
        assertEquals(100.0, totalOwed, 0.0001)

        val netSum = balances.sumOf { it.netBalance }
        assertEquals(0.0, netSum, 0.0001)

        val settlements = viewModel.calculateTripSettlements()
        val totalSettled = settlements.sumOf { it.amount }
        // p1 gets 66.66 (33.33 from p2 + 33.33 from p3; 33.34 is p1's own consumed share)
        assertEquals(66.66, totalSettled, 0.001)
    }
}
