package com.example.splixter.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillHistoryRecord
import com.example.splixter.data.BillItem
import com.example.splixter.data.CalculationMode
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.LobbySession
import com.example.splixter.data.Person
import com.example.splixter.data.PersonBreakdown
import com.example.splixter.data.SavedGroup
import com.example.splixter.data.TaxAndTip
import com.example.splixter.data.TripExpense
import com.example.splixter.data.TripPersonBalance
import com.example.splixter.data.TripSettlement
import com.example.splixter.data.UserProfile
import com.example.splixter.util.AppStorage
import com.example.splixter.util.SupabaseLobbyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class SplitterUiState(
    val currentBillId: String = UUID.randomUUID().toString(),
    val currentStep: AppStep = AppStep.SPLASH,
    val userProfile: UserProfile? = null,
    val calculationMode: CalculationMode = CalculationMode.SINGLE_BILL,
    val tripName: String = "Group Trip",
    val activeLobbyCode: String? = null,
    val currentUserId: String? = null,
    val savedLobbies: List<LobbySession> = emptyList(),
    val people: List<Person> = emptyList(),
    val items: List<BillItem> = emptyList(),
    val tripExpenses: List<TripExpense> = emptyList(),
    val taxAndTip: TaxAndTip = TaxAndTip(),
    val isScanning: Boolean = false,
    val paidByPersonId: String? = null,
    val history: List<BillHistoryRecord> = emptyList(),
    val savedGroups: List<SavedGroup> = emptyList(),
    val tripSettlements: List<com.example.splixter.data.TripSettlementRecord> = emptyList(),
    val tripActivities: List<com.example.splixter.data.TripActivity> = emptyList(),
    val isDarkMode: Boolean = false,
    val isCloudSyncing: Boolean = false,
    val availableAppUpdate: com.example.splixter.util.AppUpdateInfo? = null,
    val isCheckingUpdate: Boolean = false,
    val showUpdateDialog: Boolean = false
)

class SplitterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplitterUiState())
    val uiState: StateFlow<SplitterUiState> = _uiState.asStateFlow()

    private val appUpdateService = com.example.splixter.util.AppUpdateService()

    init {
        // Automatic background update check on app startup
        checkForAppUpdates(isManual = false)
    }

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
                val profile = storage.loadUserProfile()
                val history = storage.loadHistory()
                val savedGroups = storage.loadGroups()
                val savedState = storage.loadState()
                val savedLobbies = storage.loadLobbies()
                _uiState.update { current ->
                    val baseState = savedState ?: current
                    val initialPeople = if (baseState.people.isEmpty() && profile != null) {
                        listOf(
                            Person(
                                id = profile.id,
                                name = profile.name,
                                color = profile.color,
                                emoji = profile.emoji,
                                phoneNumber = profile.phoneNumber,
                                isCurrentUser = true
                            )
                        )
                    } else {
                        baseState.people
                    }
                    baseState.copy(
                        userProfile = profile,
                        people = initialPeople,
                        history = history,
                        savedGroups = savedGroups,
                        savedLobbies = savedLobbies
                    )
                }
            }
        }
    }

    fun getCurrentUserPerson(): Person {
        val profile = _uiState.value.userProfile
        return if (profile != null) {
            Person(
                id = profile.id,
                name = profile.name,
                color = profile.color,
                emoji = profile.emoji,
                phoneNumber = profile.phoneNumber,
                isCurrentUser = true
            )
        } else {
            Person(
                name = "You",
                color = 0xFF6C5CE7L,
                emoji = "😎",
                isCurrentUser = true
            )
        }
    }

    fun saveUserProfile(
        name: String,
        emoji: String = "😎",
        color: Long = 0xFF6C5CE7L,
        phone: String? = null,
        upiId: String? = null,
        returnToStep: AppStep? = null
    ) {
        if (name.isBlank()) return
        val currentId = _uiState.value.userProfile?.id ?: UUID.randomUUID().toString()
        val cleanedPhone = phone?.replace("\\s|-".toRegex(), "")?.ifEmpty { null }
        val cleanedUpi = upiId?.trim()?.ifEmpty { null }
        val newProfile = UserProfile(
            id = currentId,
            name = name.trim(),
            color = color,
            emoji = emoji,
            phoneNumber = cleanedPhone,
            upiId = cleanedUpi
        )
        val userPerson = Person(
            id = currentId,
            name = name.trim(),
            color = color,
            emoji = emoji,
            phoneNumber = cleanedPhone,
            upiId = cleanedUpi,
            isCurrentUser = true
        )

        updateState(immediate = true) { s ->
            // Sync person in list if already exists
            val updatedPeople = if (s.people.any { it.isCurrentUser || it.id == currentId }) {
                s.people.map { p ->
                    if (p.isCurrentUser || p.id == currentId) userPerson else p
                }
            } else if (s.people.isEmpty()) {
                listOf(userPerson)
            } else {
                s.people
            }

            s.copy(
                userProfile = newProfile,
                people = updatedPeople,
                currentUserId = currentId,
                currentStep = returnToStep ?: AppStep.MODE_SELECTION
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveUserProfile(newProfile)
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
        val totalAmount = calculateBreakdown().sumOf { it.grandTotal }
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

    fun addPerson(name: String, phoneNumber: String? = null) {
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
        val cleanedPhone = phoneNumber?.replace("\\s|-".toRegex(), "")
        val newPerson = Person(name = name.trim(), color = newColor, emoji = newEmoji, phoneNumber = cleanedPhone)
        updateState { state ->
            val updatedPeople = state.people + newPerson
            state.copy(people = updatedPeople)
        }
    }

    fun removePerson(personId: String) {
        updateState { state ->
            val updatedPeople = state.people.filterNot { it.id == personId }
            val updatedItems = state.items.map { item ->
                item.copy(assignedPersonIds = item.assignedPersonIds - personId)
            }
            val updatedPaidBy = if (state.paidByPersonId == personId) {
                null
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

    fun addItems(newItems: List<BillItem>) {
        if (newItems.isEmpty()) return
        updateState { it.copy(items = it.items + newItems) }
    }

    fun updateItem(itemId: String, name: String, price: Double, category: ItemCategory) {
        if (name.isBlank() || price <= 0.0) return
        updateState { state ->
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    item.copy(name = name.trim(), price = price, category = category)
                } else {
                    item
                }
            }
            state.copy(items = updatedItems)
        }
    }

    fun duplicateItem(itemId: String) {
        val itemToDuplicate = _uiState.value.items.find { it.id == itemId } ?: return
        val duplicated = itemToDuplicate.copy(
            id = java.util.UUID.randomUUID().toString()
        )
        updateState { state ->
            val index = state.items.indexOfFirst { it.id == itemId }
            val updatedList = if (index != -1) {
                state.items.toMutableList().apply { add(index + 1, duplicated) }
            } else {
                state.items + duplicated
            }
            state.copy(items = updatedList)
        }
    }

    fun clearItems() {
        updateState { it.copy(items = emptyList()) }
    }

    fun removeItem(itemId: String) {
        updateState { state ->
            state.copy(items = state.items.filterNot { it.id == itemId })
        }
    }

    fun toggleItemCategory(itemId: String) {
        updateState { state ->
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    val newCategory = if (item.category == ItemCategory.FOOD) ItemCategory.LIQUOR else ItemCategory.FOOD
                    item.copy(category = newCategory)
                } else {
                    item
                }
            }
            state.copy(items = updatedItems)
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

    fun assignAllToItem(itemId: String) {
        updateState { state ->
            val allPersonIds = state.people.map { it.id }.toSet()
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    item.copy(assignedPersonIds = allPersonIds)
                } else {
                    item
                }
            }
            state.copy(items = updatedItems)
        }
    }

    fun clearItemAssignments(itemId: String) {
        updateState { state ->
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    item.copy(assignedPersonIds = emptySet())
                } else {
                    item
                }
            }
            state.copy(items = updatedItems)
        }
    }

    fun assignAllItemsToEveryone() {
        updateState { state ->
            val allPersonIds = state.people.map { it.id }.toSet()
            val updatedItems = state.items.map { item ->
                item.copy(assignedPersonIds = allPersonIds)
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
        discountPercentage: Double = 0.0,
        vatAmount: Double = 0.0,
        isVatPercentage: Boolean = false,
        vatPercentage: Double = 0.0
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
                    discountPercentage = discountPercentage,
                    vatAmount = vatAmount,
                    isVatPercentage = isVatPercentage,
                    vatPercentage = vatPercentage
                )
            )
        }
    }

    fun clearAllData() {
        saveCurrentBillToHistory()
        val currentHistory = _uiState.value.history
        val currentSavedGroups = _uiState.value.savedGroups
        val currentDarkMode = _uiState.value.isDarkMode
        val currentProfile = _uiState.value.userProfile
        val currentLobbies = _uiState.value.savedLobbies
        val initialPeople = if (currentProfile != null) listOf(getCurrentUserPerson()) else emptyList()
        viewModelScope.launch(Dispatchers.IO) { appStorage?.clearState() }
        updateState(immediate = true) {
            SplitterUiState(
                currentBillId = UUID.randomUUID().toString(),
                currentStep = AppStep.PEOPLE,
                userProfile = currentProfile,
                people = initialPeople,
                history = currentHistory,
                savedGroups = currentSavedGroups,
                savedLobbies = currentLobbies,
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
                paidByPersonId = null
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

        val totalFoodSubtotal = items.filter { it.category == ItemCategory.FOOD }.sumOf { it.price }

        val taxAmount = if (totalFoodSubtotal > 0.0) {
            if (state.taxAndTip.isTaxPercentage) {
                (state.taxAndTip.taxPercentage / 100.0) * totalFoodSubtotal
            } else {
                state.taxAndTip.taxAmount
            }
        } else {
            0.0
        }

        val tipAmount = if (state.taxAndTip.isTipPercentage) {
            (state.taxAndTip.tipPercentage / 100.0) * totalSubtotal
        } else {
            state.taxAndTip.tipAmount
        }

        val totalLiquorSubtotal = items.filter { it.category == ItemCategory.LIQUOR }.sumOf { it.price }

        val totalVatAmount = if (totalLiquorSubtotal > 0.0) {
            if (state.taxAndTip.isVatPercentage) {
                totalLiquorSubtotal * (state.taxAndTip.vatPercentage / 100.0)
            } else {
                state.taxAndTip.vatAmount
            }
        } else {
            0.0
        }

        val discountBase = totalSubtotal + taxAmount + totalVatAmount
        val discount = if (state.taxAndTip.isDiscountPercentage) {
            (state.taxAndTip.discountPercentage / 100.0) * discountBase
        } else {
            state.taxAndTip.discountAmount
        }

        val activePeople = people.filter { p -> (personShares[p.id] ?: emptyList()).isNotEmpty() }
        val activePeopleCount = activePeople.size

        return people.map { person ->
            val personItems = personShares[person.id] ?: emptyList()
            val personSubtotal = personItems.sumOf { pair -> pair.second }
            val personFoodSubtotal = personItems.filter { pair -> pair.first.category == ItemCategory.FOOD }.sumOf { pair -> pair.second }
            val personLiquorSubtotal = personItems.filter { pair -> pair.first.category == ItemCategory.LIQUOR }.sumOf { pair -> pair.second }

            val isActive = personItems.isNotEmpty()

            val foodProportion = if (totalFoodSubtotal > 0.0) personFoodSubtotal / totalFoodSubtotal else 0.0

            val subtotalProportion = if (totalSubtotal > 0.0) personSubtotal / totalSubtotal else 0.0
            val personDiscountShare = if (isActive) discount * subtotalProportion else 0.0
            val netSubtotal = if (isActive) personSubtotal - personDiscountShare else 0.0

            val personTaxShare = if (isActive) taxAmount * foodProportion else 0.0
            val personTipShare = if (isActive) tipAmount * subtotalProportion else 0.0

            val personVatShare = if (isActive && totalLiquorSubtotal > 0.0) {
                if (state.taxAndTip.isVatPercentage) {
                    personLiquorSubtotal * (state.taxAndTip.vatPercentage / 100.0)
                } else {
                    totalVatAmount * (personLiquorSubtotal / totalLiquorSubtotal)
                }
            } else {
                0.0
            }

            val grandTotal = if (isActive) {
                netSubtotal + personTaxShare + personTipShare + personVatShare
            } else {
                0.0
            }

            PersonBreakdown(
                person = person,
                items = personItems,
                subtotal = personSubtotal,
                discountShare = personDiscountShare,
                taxShare = personTaxShare,
                tipShare = personTipShare,
                vatShare = personVatShare,
                grandTotal = grandTotal
            )
        }
    }

    // --- Trip Expense Splitter Methods ---

    fun setCalculationMode(mode: CalculationMode) {
        updateState(immediate = true) { it.copy(calculationMode = mode) }
    }

    fun setTripName(name: String) {
        if (name.isNotBlank()) {
            updateState { it.copy(tripName = name.trim()) }
        }
    }

    fun addTripExpense(
        title: String,
        amount: Double,
        paidByPersonId: String,
        splitWithPersonIds: Set<String> = emptySet(),
        category: String = "General"
    ) {
        if (title.isBlank() || amount <= 0.0 || paidByPersonId.isBlank()) return
        val newExpense = TripExpense(
            title = title.trim(),
            amount = amount,
            paidByPersonId = paidByPersonId,
            splitWithPersonIds = splitWithPersonIds,
            category = category
        )
        val activeCode = _uiState.value.activeLobbyCode
        val payer = _uiState.value.people.find { it.id == paidByPersonId }
        val payerName = payer?.name ?: "Someone"
        val activity = if (activeCode != null) {
            com.example.splixter.data.TripActivity(
                lobbyCode = activeCode,
                actorPersonId = paidByPersonId,
                actorName = payerName,
                actionType = "EXPENSE_ADDED",
                description = "$payerName added '${title.trim()}' (₹${String.format(java.util.Locale.US, "%.0f", amount)})",
                amount = amount
            )
        } else null

        updateState { s ->
            val newExpenses = listOf(newExpense) + s.tripExpenses.filterNot { it.id == newExpense.id }
            val newActivities = if (activity != null) listOf(activity) + s.tripActivities else s.tripActivities
            val updatedLobbies = if (activeCode != null) {
                s.savedLobbies.map { lob ->
                    if (lob.code.equals(activeCode, ignoreCase = true)) lob.copy(expenses = newExpenses, members = s.people, activities = newActivities) else lob
                }
            } else s.savedLobbies
            s.copy(tripExpenses = newExpenses, tripActivities = newActivities, savedLobbies = updatedLobbies)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            if (activeCode != null) {
                supabaseLobbyService.addExpense(activeCode, newExpense)
                if (activity != null) {
                    supabaseLobbyService.recordActivity(activity)
                }
            }
        }
    }

    fun removeTripExpense(expenseId: String) {
        val activeCode = _uiState.value.activeLobbyCode
        val targetExpense = _uiState.value.tripExpenses.find { it.id == expenseId }
        val currentUserId = _uiState.value.currentUserId ?: ""
        val currentUser = _uiState.value.people.find { it.id == currentUserId }
        val actorName = currentUser?.name ?: "Someone"

        val activity = if (activeCode != null && targetExpense != null) {
            com.example.splixter.data.TripActivity(
                lobbyCode = activeCode,
                actorPersonId = currentUserId,
                actorName = actorName,
                actionType = "EXPENSE_DELETED",
                description = "$actorName deleted '${targetExpense.title}'",
                amount = targetExpense.amount
            )
        } else null

        updateState { state ->
            val newExpenses = state.tripExpenses.filterNot { it.id == expenseId }
            val newActivities = if (activity != null) listOf(activity) + state.tripActivities else state.tripActivities
            val updatedLobbies = if (activeCode != null) {
                state.savedLobbies.map { lob ->
                    if (lob.code.equals(activeCode, ignoreCase = true)) lob.copy(expenses = newExpenses, activities = newActivities) else lob
                }
            } else state.savedLobbies
            state.copy(tripExpenses = newExpenses, tripActivities = newActivities, savedLobbies = updatedLobbies)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            if (activeCode != null) {
                supabaseLobbyService.deleteExpense(expenseId)
                if (activity != null) {
                    supabaseLobbyService.recordActivity(activity)
                }
            }
        }
    }

    fun recordSettlementPayment(settlement: TripSettlement, txRef: String? = null) {
        val activeCode = _uiState.value.activeLobbyCode
        val newRecord = com.example.splixter.data.TripSettlementRecord(
            lobbyCode = activeCode ?: "",
            fromPersonId = settlement.fromPerson.id,
            toPersonId = settlement.toPerson.id,
            amount = settlement.amount,
            transactionRef = txRef
        )
        val activity = if (activeCode != null) {
            com.example.splixter.data.TripActivity(
                lobbyCode = activeCode,
                actorPersonId = settlement.fromPerson.id,
                actorName = settlement.fromPerson.name,
                actionType = "SETTLEMENT_PAID",
                description = "${settlement.fromPerson.name} settled ₹${String.format(java.util.Locale.US, "%.2f", settlement.amount)} with ${settlement.toPerson.name}",
                amount = settlement.amount
            )
        } else null

        updateState { s ->
            val newSettlements = listOf(newRecord) + s.tripSettlements
            val newActivities = if (activity != null) listOf(activity) + s.tripActivities else s.tripActivities
            val updatedLobbies = if (activeCode != null) {
                s.savedLobbies.map { lob ->
                    if (lob.code.equals(activeCode, ignoreCase = true)) lob.copy(settlements = newSettlements, activities = newActivities) else lob
                }
            } else s.savedLobbies
            s.copy(tripSettlements = newSettlements, tripActivities = newActivities, savedLobbies = updatedLobbies)
        }

        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            if (activeCode != null) {
                supabaseLobbyService.recordSettlement(
                    lobbyCode = activeCode,
                    fromPersonId = settlement.fromPerson.id,
                    toPersonId = settlement.toPerson.id,
                    amount = settlement.amount,
                    txRef = txRef
                )
                if (activity != null) {
                    supabaseLobbyService.recordActivity(activity)
                }
            }
        }
    }

    fun clearTripExpenses() {
        updateState { it.copy(tripExpenses = emptyList(), tripSettlements = emptyList()) }
    }

    fun calculateTripBalances(): List<TripPersonBalance> {
        val state = _uiState.value
        val people = state.people
        if (people.isEmpty()) return emptyList()

        val paidMap: MutableMap<String, Double> = mutableMapOf()
        val owedMap: MutableMap<String, Double> = mutableMapOf()

        for (p in people) {
            paidMap[p.id] = 0.0
            owedMap[p.id] = 0.0
        }

        for (exp in state.tripExpenses) {
            paidMap[exp.paidByPersonId] = (paidMap[exp.paidByPersonId] ?: 0.0) + exp.amount

            val splitters: List<String> = if (exp.splitWithPersonIds.isNotEmpty()) {
                exp.splitWithPersonIds.filter { pid -> people.any { p -> p.id == pid } }
            } else {
                people.map { p -> p.id }
            }

            if (splitters.isNotEmpty()) {
                // Exact integer arithmetic in paisa to avoid floating-point remainder leaks
                val totalPaisa = Math.round(exp.amount * 100)
                val baseShare = totalPaisa / splitters.size
                val remainderPaisa = (totalPaisa % splitters.size).toInt()

                for ((idx, pid) in splitters.withIndex()) {
                    val shareForPerson = baseShare + (if (idx < remainderPaisa) 1 else 0)
                    owedMap[pid] = (owedMap[pid] ?: 0.0) + (shareForPerson / 100.0)
                }
            }
        }

        // Apply completed settlements to dynamically adjust paid / received balances
        for (s in state.tripSettlements) {
            paidMap[s.fromPersonId] = (paidMap[s.fromPersonId] ?: 0.0) + s.amount
            paidMap[s.toPersonId] = (paidMap[s.toPersonId] ?: 0.0) - s.amount
        }

        return people.map { person ->
            val totalPaid = paidMap[person.id] ?: 0.0
            val totalOwed = owedMap[person.id] ?: 0.0
            val net = totalPaid - totalOwed
            TripPersonBalance(
                person = person,
                totalPaid = totalPaid,
                totalOwed = totalOwed,
                netBalance = net
            )
        }
    }

    fun calculateTripSettlements(): List<TripSettlement> {
        val balances = calculateTripBalances()
        if (balances.isEmpty()) return emptyList()

        class Account(val person: Person, var balancePaisa: Long)

        val creditors = mutableListOf<Account>()
        val debtors = mutableListOf<Account>()

        for (b in balances) {
            val netPaisa = Math.round(b.netBalance * 100.0)
            if (netPaisa > 0) {
                creditors.add(Account(b.person, netPaisa))
            } else if (netPaisa < 0) {
                debtors.add(Account(b.person, -netPaisa)) // store positive debt amount in paisa
            }
        }

        creditors.sortByDescending { it.balancePaisa }
        debtors.sortByDescending { it.balancePaisa }

        val settlements = mutableListOf<TripSettlement>()
        var cIdx = 0
        var dIdx = 0

        while (cIdx < creditors.size && dIdx < debtors.size) {
            val creditor = creditors[cIdx]
            val debtor = debtors[dIdx]

            val settlePaisa = Math.min(creditor.balancePaisa, debtor.balancePaisa)
            if (settlePaisa > 0) {
                settlements.add(
                    TripSettlement(
                        fromPerson = debtor.person,
                        toPerson = creditor.person,
                        amount = settlePaisa / 100.0
                    )
                )
            }

            creditor.balancePaisa -= settlePaisa
            debtor.balancePaisa -= settlePaisa

            if (creditor.balancePaisa == 0L) cIdx++
            if (debtor.balancePaisa == 0L) dIdx++
        }

        return settlements
    }

    // --- Supabase Lobby & Real-Time Sync Methods ---

    private val supabaseLobbyService = SupabaseLobbyService()
    private var cloudSyncJob: Job? = null

    fun startCloudSync(code: String) {
        cloudSyncJob?.cancel()
        cloudSyncJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isCloudSyncing = true) }
            supabaseLobbyService.observeLobby(code).collect { remote ->
                if (remote != null) {
                    _uiState.update { s ->
                        if (!s.activeLobbyCode.equals(code, ignoreCase = true)) return@update s
                        
                        val updatedSavedLobbies = s.savedLobbies.map { lob ->
                            if (lob.code.equals(code, ignoreCase = true)) remote else lob
                        }
                        s.copy(
                            tripName = remote.name,
                            people = remote.members,
                            tripExpenses = remote.expenses,
                            tripSettlements = remote.settlements,
                            tripActivities = remote.activities,
                            savedLobbies = updatedSavedLobbies,
                            isCloudSyncing = true
                        )
                    }
                }
            }
        }
    }

    fun extractLobbyCode(input: String): String {
        val clean = input.trim()
        if (clean.contains("splixter://lobby/")) {
            val after = clean.substringAfter("splixter://lobby/").trim().take(6)
            return after.replace("-", "").uppercase()
        }
        if (clean.contains("/lobby/")) {
            val after = clean.substringAfter("/lobby/").trim().take(6)
            return after.replace("-", "").uppercase()
        }
        val match = Regex("""\b[A-Z0-9]{6}\b""").find(clean.uppercase())
        if (match != null) {
            return match.value
        }
        return clean.replace("-", "").replace(" ", "").take(6).uppercase()
    }

    fun createLobby(tripName: String, hostName: String, initialMembers: List<Person> = emptyList()) {
        if (tripName.isBlank() || hostName.isBlank()) return
        val code = (100000..999999).random().toString()
        val profile = _uiState.value.userProfile
        val hostId = profile?.id ?: UUID.randomUUID().toString()
        val hostColor = profile?.color ?: 0xFF6C5CE7L
        val hostEmoji = profile?.emoji ?: "😎"
        val host = Person(
            id = hostId,
            name = hostName.trim(),
            color = hostColor,
            emoji = hostEmoji,
            phoneNumber = profile?.phoneNumber,
            isCurrentUser = true
        )
        
        val combinedMembers = mutableListOf(host)
        for (m in initialMembers) {
            if (!combinedMembers.any { it.name.equals(m.name, ignoreCase = true) }) {
                combinedMembers.add(m)
            }
        }

        val initialActivity = com.example.splixter.data.TripActivity(
            lobbyCode = code,
            actorPersonId = host.id,
            actorName = host.name,
            actionType = "MEMBER_JOINED",
            description = "${host.name} created the trip lobby"
        )

        val session = LobbySession(
            code = code,
            name = tripName.trim(),
            hostPersonId = host.id,
            members = combinedMembers,
            activities = listOf(initialActivity)
        )
        updateState(immediate = true) { s ->
            s.copy(
                activeLobbyCode = code,
                currentUserId = host.id,
                tripName = tripName.trim(),
                people = combinedMembers,
                tripExpenses = emptyList(),
                tripSettlements = emptyList(),
                tripActivities = listOf(initialActivity),
                calculationMode = CalculationMode.TRIP_EXPENSE,
                currentStep = AppStep.TRIP_EXPENSES,
                savedLobbies = listOf(session) + s.savedLobbies.filterNot { it.code == code }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            supabaseLobbyService.createOrUpdateLobby(session)
            supabaseLobbyService.recordActivity(initialActivity)
        }
        startCloudSync(code)
    }

    fun fetchLobbyForJoin(lobbyCode: String, onResult: (LobbySession?) -> Unit) {
        val code = extractLobbyCode(lobbyCode)
        if (code.isBlank()) {
            onResult(null)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val session = supabaseLobbyService.fetchLobbySession(code) ?: _uiState.value.savedLobbies.find { it.code.equals(code, ignoreCase = true) }
            withContext(Dispatchers.Main) {
                onResult(session)
            }
        }
    }

    fun joinLobby(lobbyCode: String, memberName: String, claimPersonId: String? = null) {
        if (lobbyCode.isBlank()) return
        val code = extractLobbyCode(lobbyCode)

        viewModelScope.launch(Dispatchers.IO) {
            val remoteSession = supabaseLobbyService.fetchLobbySession(code)
            val existingSession = _uiState.value.savedLobbies.find { it.code.equals(code, ignoreCase = true) }
            val profile = _uiState.value.userProfile
            val colors = listOf(
                0xFF6750A4L, 0xFF006A60L, 0xFF984061L, 0xFFB58300L,
                0xFF3B6470L, 0xFF825500L, 0xFF4A6363L, 0xFF6B5778L
            )
            val currentMembers = remoteSession?.members ?: existingSession?.members ?: _uiState.value.people
            
            val activeUser: Person
            val updatedMembers: List<Person>
            val joinActivity: com.example.splixter.data.TripActivity

            if (claimPersonId != null) {
                val existingTarget = currentMembers.find { it.id == claimPersonId }
                val targetName = existingTarget?.name ?: memberName.trim()
                activeUser = (existingTarget ?: Person(id = claimPersonId, name = targetName, color = colors[0])).copy(
                    isCurrentUser = true,
                    upiId = profile?.upiId ?: existingTarget?.upiId,
                    phoneNumber = profile?.phoneNumber ?: existingTarget?.phoneNumber,
                    emoji = profile?.emoji ?: existingTarget?.emoji ?: "😎",
                    color = profile?.color ?: existingTarget?.color ?: colors[0]
                )
                updatedMembers = currentMembers.map { if (it.id == claimPersonId) activeUser else it.copy(isCurrentUser = false) }
                joinActivity = com.example.splixter.data.TripActivity(
                    lobbyCode = code,
                    actorPersonId = activeUser.id,
                    actorName = activeUser.name,
                    actionType = "MEMBER_CLAIMED",
                    description = "${profile?.name ?: memberName.trim()} joined and claimed profile '$targetName'"
                )
            } else {
                val existingMember = currentMembers.find { it.name.equals(memberName.trim(), ignoreCase = true) }
                val isUser = profile != null && memberName.trim().equals(profile.name, ignoreCase = true)
                activeUser = existingMember?.copy(isCurrentUser = true) ?: Person(
                    id = if (isUser) profile.id else UUID.randomUUID().toString(),
                    name = memberName.trim(),
                    color = if (isUser) profile.color else colors[currentMembers.size % colors.size],
                    emoji = if (isUser) profile.emoji else "😎",
                    phoneNumber = if (isUser) profile.phoneNumber else null,
                    upiId = if (isUser) profile.upiId else null,
                    isCurrentUser = true
                )
                updatedMembers = if (existingMember != null) {
                    currentMembers.map { if (it.id == activeUser.id) activeUser else it.copy(isCurrentUser = false) }
                } else {
                    currentMembers.map { it.copy(isCurrentUser = false) } + activeUser
                }
                joinActivity = com.example.splixter.data.TripActivity(
                    lobbyCode = code,
                    actorPersonId = activeUser.id,
                    actorName = activeUser.name,
                    actionType = "MEMBER_JOINED",
                    description = "${activeUser.name} joined the trip"
                )
            }

            val updatedActivities = listOf(joinActivity) + (remoteSession?.activities ?: existingSession?.activities ?: _uiState.value.tripActivities)

            val updatedSession = LobbySession(
                code = code,
                name = remoteSession?.name ?: existingSession?.name ?: if (_uiState.value.tripName.isBlank()) "Joined Trip" else _uiState.value.tripName,
                hostPersonId = remoteSession?.hostPersonId ?: currentMembers.firstOrNull()?.id ?: activeUser.id,
                members = updatedMembers,
                expenses = remoteSession?.expenses ?: existingSession?.expenses ?: _uiState.value.tripExpenses,
                settlements = remoteSession?.settlements ?: existingSession?.settlements ?: _uiState.value.tripSettlements,
                activities = updatedActivities
            )

            updateState(immediate = true) { s ->
                s.copy(
                    activeLobbyCode = code,
                    currentUserId = activeUser.id,
                    tripName = updatedSession.name,
                    people = updatedMembers,
                    tripExpenses = updatedSession.expenses,
                    tripSettlements = updatedSession.settlements,
                    tripActivities = updatedActivities,
                    calculationMode = CalculationMode.TRIP_EXPENSE,
                    currentStep = AppStep.TRIP_EXPENSES,
                    savedLobbies = listOf(updatedSession) + s.savedLobbies.filterNot { it.code == code }
                )
            }
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            supabaseLobbyService.addMember(code, activeUser)
            supabaseLobbyService.recordActivity(joinActivity)
            startCloudSync(code)
        }
    }

    fun setCurrentUserId(userId: String) {
        updateState(immediate = true) { s ->
            s.copy(currentUserId = userId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveState(_uiState.value)
        }
    }

    fun addMemberToCurrentLobby(name: String) {
        if (name.isBlank()) return
        addPerson(name)
        val code = _uiState.value.activeLobbyCode ?: return
        val currentPerson = _uiState.value.people.lastOrNull { it.name.equals(name.trim(), ignoreCase = true) }
        val joinActivity = if (currentPerson != null) {
            com.example.splixter.data.TripActivity(
                lobbyCode = code,
                actorPersonId = currentPerson.id,
                actorName = currentPerson.name,
                actionType = "MEMBER_JOINED",
                description = "${currentPerson.name} was added to the group"
            )
        } else null

        _uiState.update { s ->
            val newActs = if (joinActivity != null) listOf(joinActivity) + s.tripActivities else s.tripActivities
            val updatedLobbies = s.savedLobbies.map { lob ->
                if (lob.code == code) lob.copy(members = s.people, activities = newActs) else lob
            }
            s.copy(savedLobbies = updatedLobbies, tripActivities = newActs)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
            if (currentPerson != null) {
                supabaseLobbyService.addMember(code, currentPerson)
                if (joinActivity != null) {
                    supabaseLobbyService.recordActivity(joinActivity)
                }
            }
        }
    }

    fun loadLobby(code: String) {
        val session = _uiState.value.savedLobbies.find { it.code.equals(code, ignoreCase = true) } ?: return
        updateState(immediate = true) { s ->
            s.copy(
                activeLobbyCode = session.code,
                currentUserId = session.hostPersonId,
                tripName = session.name,
                people = session.members,
                tripExpenses = session.expenses,
                tripSettlements = session.settlements,
                tripActivities = session.activities,
                calculationMode = CalculationMode.TRIP_EXPENSE,
                currentStep = AppStep.TRIP_EXPENSES
            )
        }
        startCloudSync(code)
    }

    fun deleteLobby(code: String) {
        updateState(immediate = true) { s ->
            val updated = s.savedLobbies.filterNot { it.code.equals(code, ignoreCase = true) }
            val active = if (s.activeLobbyCode.equals(code, ignoreCase = true)) null else s.activeLobbyCode
            s.copy(savedLobbies = updated, activeLobbyCode = active)
        }
        viewModelScope.launch(Dispatchers.IO) {
            appStorage?.saveLobbies(_uiState.value.savedLobbies)
        }
    }

    // --- App Updates from GitHub ---

    fun checkForAppUpdates(isManual: Boolean = false, onComplete: ((com.example.splixter.util.AppUpdateResult) -> Unit)? = null) {
        _uiState.update { it.copy(isCheckingUpdate = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = appUpdateService.checkForUpdates()
            withContext(Dispatchers.Main) {
                _uiState.update { s ->
                    when (result) {
                        is com.example.splixter.util.AppUpdateResult.Success -> {
                            s.copy(
                                availableAppUpdate = result.info,
                                isCheckingUpdate = false,
                                showUpdateDialog = true
                            )
                        }
                        else -> {
                            s.copy(
                                isCheckingUpdate = false,
                                showUpdateDialog = isManual && s.availableAppUpdate != null
                            )
                        }
                    }
                }
                onComplete?.invoke(result)
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }
}


