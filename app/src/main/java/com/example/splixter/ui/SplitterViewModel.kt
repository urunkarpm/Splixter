package com.example.splixter.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillHistoryRecord
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.Person
import com.example.splixter.data.PersonBreakdown
import com.example.splixter.data.SavedGroup
import com.example.splixter.data.TaxAndTip
import com.example.splixter.util.AppStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SplitterUiState(
    val currentBillId: String = UUID.randomUUID().toString(),
    val currentStep: AppStep = AppStep.SPLASH,
    val people: List<Person> = emptyList(),
    val items: List<BillItem> = emptyList(),
    val taxAndTip: TaxAndTip = TaxAndTip(),
    val isScanning: Boolean = false,
    val paidByPersonId: String? = null,
    val history: List<BillHistoryRecord> = emptyList(),
    val savedGroups: List<SavedGroup> = emptyList(),
    val isDarkMode: Boolean = false
)

class SplitterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplitterUiState())
    val uiState: StateFlow<SplitterUiState> = _uiState.asStateFlow()

    private var appStorage: AppStorage? = null
    private var saveJob: Job? = null

    /** Update state immediately on the main thread, then debounce-save to disk. */
    private fun updateState(immediate: Boolean = false, transform: (SplitterUiState) -> SplitterUiState) {
        _uiState.update { transform(it) }
        if (immediate) {
            saveJob?.cancel()
            viewModelScope.launch(Dispatchers.IO) {
                appStorage?.saveState(_uiState.value)
            }
        } else {
            saveJob?.cancel()
            saveJob = viewModelScope.launch(Dispatchers.IO) {
                delay(400) // debounce: only write after 400ms of inactivity
                appStorage?.saveState(_uiState.value)
            }
        }
    }

    fun initStorage(context: Context) {
        if (appStorage == null) {
            val storage = AppStorage(context.applicationContext)
            appStorage = storage
            viewModelScope.launch(Dispatchers.IO) {
                val history = storage.loadHistory()
                val savedGroups = storage.loadGroups()
                val savedState = storage.loadState()
                _uiState.update { current ->
                    if (savedState != null) {
                        savedState.copy(history = history, savedGroups = savedGroups)
                    } else {
                        current.copy(history = history, savedGroups = savedGroups)
                    }
                }
            }
        }
    }

    fun toggleDarkMode() {
        updateState { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setStep(step: AppStep) {
        if (step == AppStep.RECEIPT) {
            saveCurrentBillToHistory()
        }
        updateState(immediate = true) { it.copy(currentStep = step) }
    }

    fun saveCurrentBillToHistory() {
        val state = _uiState.value
        if (state.people.isEmpty() || state.items.isEmpty()) return
        val totalAmount = state.items.sumOf { it.price } + state.taxAndTip.taxAmount + state.taxAndTip.tipAmount
        val recordId = state.currentBillId
        val newRecord = BillHistoryRecord(
            id = recordId,
            timestamp = System.currentTimeMillis(),
            people = state.people,
            items = state.items,
            taxAndTip = state.taxAndTip,
            paidByPersonId = state.paidByPersonId,
            totalAmount = totalAmount
        )
        _uiState.update { s ->
            val updatedHistory = listOf(newRecord) + s.history.filterNot { it.id == recordId }
            s.copy(history = updatedHistory)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveHistory(_uiState.value.history)
        }
    }

    fun loadBillFromHistory(recordId: String) {
        val record = _uiState.value.history.find { it.id == recordId } ?: return
        updateState { s ->
            s.copy(
                currentBillId = record.id,
                currentStep = AppStep.PEOPLE,
                people = record.people,
                items = record.items,
                taxAndTip = record.taxAndTip,
                paidByPersonId = record.paidByPersonId
            )
        }
    }

    fun deleteHistoryRecord(recordId: String) {
        _uiState.update { s ->
            s.copy(history = s.history.filterNot { it.id == recordId })
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveHistory(_uiState.value.history)
        }
    }

    fun setPaidByPerson(personId: String) {
        updateState { it.copy(paidByPersonId = personId) }
    }

    fun addPerson(name: String) {
        if (name.isBlank()) return
        val colors = listOf(
            0xFF6750A4, 0xFF006A60, 0xFF984061, 0xFFB58300, 
            0xFF3B6470, 0xFF825500, 0xFF4A6363, 0xFF6B5778
        )
        val faceEmojis = listOf(
            "😎", "🥳", "🤪", "🤠", "🤓", "🤩", "😈", "🧐", 
            "😜", "😇", "🤖", "👻", "👽", "🦄", "🐶", "🦊", 
            "🦁", "🐼", "🐻", "🐸", "🥸", "🤗", "🤡", "👹"
        )
        val newColor = colors[(_uiState.value.people.size) % colors.size]
        val newEmoji = faceEmojis.random()
        val newPerson = Person(name = name.trim(), color = newColor, emoji = newEmoji)
        updateState { state ->
            val updatedPeople = state.people + newPerson
            val updatedPaidBy = state.paidByPersonId ?: newPerson.id
            state.copy(people = updatedPeople, paidByPersonId = updatedPaidBy)
        }
    }

    fun removePerson(personId: String) {
        updateState { state ->
            val updatedPeople = state.people.filterNot { it.id == personId }
            val updatedItems = state.items.map { item ->
                item.copy(assignedPersonIds = item.assignedPersonIds - personId)
            }
            val updatedPaidBy = if (state.paidByPersonId == personId) {
                updatedPeople.firstOrNull()?.id
            } else {
                state.paidByPersonId
            }
            state.copy(people = updatedPeople, items = updatedItems, paidByPersonId = updatedPaidBy)
        }
    }

    fun setItems(items: List<BillItem>) {
        updateState { it.copy(items = items) }
    }

    fun addItem(name: String, price: Double, category: ItemCategory = ItemCategory.FOOD) {
        if (name.isBlank() || price <= 0.0) return
        val newItem = BillItem(name = name.trim(), price = price, category = category)
        updateState { it.copy(items = it.items + newItem) }
    }

    fun removeItem(itemId: String) {
        updateState { state ->
            state.copy(items = state.items.filterNot { it.id == itemId })
        }
    }

    fun toggleItemAssignment(itemId: String, personId: String) {
        updateState { state ->
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    val currentAssignments = item.assignedPersonIds.toMutableSet()
                    if (currentAssignments.contains(personId)) {
                        currentAssignments.remove(personId)
                    } else {
                        currentAssignments.add(personId)
                    }
                    item.copy(assignedPersonIds = currentAssignments)
                } else {
                    item
                }
            }
            state.copy(items = updatedItems)
        }
    }

    fun updateTaxAndTip(
        taxAmount: Double,
        tipAmount: Double,
        discountAmount: Double = 0.0,
        isTaxPercentage: Boolean = false,
        taxPercentage: Double = 0.0,
        isTipPercentage: Boolean = false,
        tipPercentage: Double = 0.0,
        isDiscountPercentage: Boolean = false,
        discountPercentage: Double = 0.0
    ) {
        updateState { state ->
            state.copy(
                taxAndTip = TaxAndTip(
                    taxAmount = taxAmount,
                    tipAmount = tipAmount,
                    discountAmount = discountAmount,
                    isTaxPercentage = isTaxPercentage,
                    taxPercentage = taxPercentage,
                    isTipPercentage = isTipPercentage,
                    tipPercentage = tipPercentage,
                    isDiscountPercentage = isDiscountPercentage,
                    discountPercentage = discountPercentage
                )
            )
        }
    }

    fun clearAllData() {
        saveCurrentBillToHistory()
        val currentHistory = _uiState.value.history
        val currentSavedGroups = _uiState.value.savedGroups
        val currentDarkMode = _uiState.value.isDarkMode
        viewModelScope.launch(Dispatchers.IO) { appStorage?.clearState() }
        updateState(immediate = true) {
            SplitterUiState(
                currentBillId = UUID.randomUUID().toString(),
                currentStep = AppStep.PEOPLE,
                history = currentHistory,
                savedGroups = currentSavedGroups,
                isDarkMode = currentDarkMode
            )
        }
    }

    fun saveCurrentGroup(groupName: String) {
        if (groupName.isBlank() || _uiState.value.people.isEmpty()) return
        val newGroup = SavedGroup(name = groupName.trim(), members = _uiState.value.people)
        _uiState.update { s -> s.copy(savedGroups = s.savedGroups + newGroup) }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveGroups(_uiState.value.savedGroups)
        }
    }

    fun loadSavedGroup(groupId: String) {
        val group = _uiState.value.savedGroups.find { it.id == groupId } ?: return
        updateState(immediate = true) { s ->
            s.copy(
                people = group.members,
                paidByPersonId = group.members.firstOrNull()?.id
            )
        }
    }

    fun deleteSavedGroup(groupId: String) {
        updateState { s ->
            val updated = s.savedGroups.filterNot { it.id == groupId }
            appStorage?.saveGroups(updated)
            s.copy(savedGroups = updated)
        }
    }

    fun calculateBreakdown(): List<PersonBreakdown> {
        val state = _uiState.value
        val people = state.people
        val items = state.items
        if (people.isEmpty()) return emptyList()

        val personShares = people.associate { it.id to mutableListOf<Pair<BillItem, Double>>() }

        for (item in items) {
            val assignees = if (item.assignedPersonIds.isNotEmpty()) {
                item.assignedPersonIds
            } else {
                people.map { it.id }.toSet()
            }
            if (assignees.isNotEmpty()) {
                val perPersonCost = item.price / assignees.size
                for (personId in assignees) {
                    personShares[personId]?.add(item to perPersonCost)
                }
            }
        }

        val totalSubtotal = personShares.values.sumOf { list -> list.sumOf { it.second } }
        val effectiveSubtotal = if (totalSubtotal > 0) totalSubtotal else 1.0

        val taxAmount = if (state.taxAndTip.isTaxPercentage) {
            (state.taxAndTip.taxPercentage / 100.0) * totalSubtotal
        } else {
            state.taxAndTip.taxAmount
        }

        val discount = if (state.taxAndTip.isDiscountPercentage) {
            (state.taxAndTip.discountPercentage / 100.0) * totalSubtotal
        } else {
            state.taxAndTip.discountAmount
        }

        val tipAmount = if (state.taxAndTip.isTipPercentage) {
            (state.taxAndTip.tipPercentage / 100.0) * totalSubtotal
        } else {
            state.taxAndTip.tipAmount
        }

        return people.map { person ->
            val personItems = personShares[person.id] ?: emptyList()
            val personSubtotal = personItems.sumOf { it.second }
            val proportion = if (totalSubtotal > 0) personSubtotal / effectiveSubtotal else (1.0 / people.size)

            val personDiscountShare = discount * proportion
            val netSubtotal = personSubtotal - personDiscountShare

            val personTaxShare = taxAmount * proportion
            val personTipShare = if (people.isNotEmpty()) tipAmount / people.size else 0.0
            val grandTotal = netSubtotal + personTaxShare + personTipShare

            PersonBreakdown(
                person = person,
                items = personItems,
                subtotal = personSubtotal,
                discountShare = personDiscountShare,
                taxShare = personTaxShare,
                tipShare = personTipShare,
                grandTotal = grandTotal
            )
        }
    }
}
