package com.example.splixter.util

import android.content.Context
import android.content.SharedPreferences
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillHistoryRecord
import com.example.splixter.data.BillItem
import com.example.splixter.data.CalculationMode
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.LobbySession
import com.example.splixter.data.Person
import com.example.splixter.data.SavedGroup
import com.example.splixter.data.TaxAndTip
import com.example.splixter.data.TripExpense
import com.example.splixter.data.UserProfile
import com.example.splixter.ui.SplitterUiState
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("splixter_prefs", Context.MODE_PRIVATE)

    fun saveState(state: SplitterUiState) {
        val editor = prefs.edit()
        editor.putString("current_step", state.currentStep.name)
        editor.putString("calculation_mode", state.calculationMode.name)
        editor.putString("trip_name", state.tripName)
        editor.putString("active_lobby_code", state.activeLobbyCode)
        editor.putString("current_user_id", state.currentUserId)

        // Save People
        val peopleArray = JSONArray()
        for (p in state.people) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("color", p.color)
            obj.put("emoji", p.emoji)
            obj.put("phoneNumber", p.phoneNumber ?: "")
            obj.put("upiId", p.upiId ?: "")
            obj.put("isCurrentUser", p.isCurrentUser)
            peopleArray.put(obj)
        }
        editor.putString("people_json", peopleArray.toString())

        // Save Items
        val itemsArray = JSONArray()
        for (item in state.items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("price", item.price)
            obj.put("category", item.category.name)
            val assignees = JSONArray()
            for (pid in item.assignedPersonIds) {
                assignees.put(pid)
            }
            obj.put("assignedPersonIds", assignees)
            itemsArray.put(obj)
        }
        editor.putString("items_json", itemsArray.toString())

        // Save Trip Expenses
        val tripArray = JSONArray()
        for (exp in state.tripExpenses) {
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("title", exp.title)
            obj.put("amount", exp.amount)
            obj.put("paidByPersonId", exp.paidByPersonId)
            obj.put("category", exp.category)
            obj.put("timestamp", exp.timestamp)
            val splitters = JSONArray()
            for (pid in exp.splitWithPersonIds) {
                splitters.put(pid)
            }
            obj.put("splitWithPersonIds", splitters)
            tripArray.put(obj)
        }
        editor.putString("trip_expenses_json", tripArray.toString())

        // Save Tax, Tip, and Discount
        val ttObj = JSONObject()
        ttObj.put("taxAmount", state.taxAndTip.taxAmount)
        ttObj.put("tipAmount", state.taxAndTip.tipAmount)
        ttObj.put("discountAmount", state.taxAndTip.discountAmount)
        ttObj.put("isTaxPercentage", state.taxAndTip.isTaxPercentage)
        ttObj.put("taxPercentage", state.taxAndTip.taxPercentage)
        ttObj.put("isTipPercentage", state.taxAndTip.isTipPercentage)
        ttObj.put("tipPercentage", state.taxAndTip.tipPercentage)
        ttObj.put("isDiscountPercentage", state.taxAndTip.isDiscountPercentage)
        ttObj.put("discountPercentage", state.taxAndTip.discountPercentage)
        ttObj.put("vatAmount", state.taxAndTip.vatAmount)
        ttObj.put("isVatPercentage", state.taxAndTip.isVatPercentage)
        ttObj.put("vatPercentage", state.taxAndTip.vatPercentage)
        editor.putString("tax_tip_json", ttObj.toString())

        editor.putString("paid_by_person_id", state.paidByPersonId)
        editor.putBoolean("is_dark_mode", state.isDarkMode)

        editor.apply()
    }

    fun loadState(): SplitterUiState? {
        try {
            val stepStr = prefs.getString("current_step", null) ?: return null
            val step = try { AppStep.valueOf(stepStr) } catch (e: Exception) { AppStep.PEOPLE }

            val peopleJson = prefs.getString("people_json", null)
            val people = mutableListOf<Person>()
            if (!peopleJson.isNullOrEmpty()) {
                val array = JSONArray(peopleJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    people.add(
                        Person(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            color = obj.getLong("color"),
                            emoji = obj.optString("emoji", "😎"),
                            phoneNumber = obj.optString("phoneNumber", "").ifEmpty { null },
                            upiId = obj.optString("upiId", "").ifEmpty { null }
                        )
                    )
                }
            }

            val itemsJson = prefs.getString("items_json", null)
            val items = mutableListOf<BillItem>()
            if (!itemsJson.isNullOrEmpty()) {
                val array = JSONArray(itemsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val assigneesSet = mutableSetOf<String>()
                    val assigneesArray = obj.optJSONArray("assignedPersonIds")
                    if (assigneesArray != null) {
                        for (j in 0 until assigneesArray.length()) {
                            assigneesSet.add(assigneesArray.getString(j))
                        }
                    }
                    val catStr = obj.optString("category", "FOOD")
                    val category = try { ItemCategory.valueOf(catStr) } catch (e: Exception) { ItemCategory.FOOD }
                    items.add(
                        BillItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            price = obj.getDouble("price"),
                            assignedPersonIds = assigneesSet,
                            category = category
                        )
                    )
                }
            }

            var taxAndTip = TaxAndTip()
            val ttJson = prefs.getString("tax_tip_json", null)
            if (!ttJson.isNullOrEmpty()) {
                val obj = JSONObject(ttJson)
                val storedTax = obj.optDouble("taxAmount", 0.0)
                val storedTip = obj.optDouble("tipAmount", 0.0)
                // Migrate away from old dummy defaults (120/50) → treat as 0
                taxAndTip = TaxAndTip(
                    taxAmount = if (storedTax == 120.0) 0.0 else storedTax,
                    tipAmount = if (storedTip == 50.0) 0.0 else storedTip,
                    discountAmount = obj.optDouble("discountAmount", 0.0),
                    isTaxPercentage = obj.optBoolean("isTaxPercentage", true),
                    taxPercentage = obj.optDouble("taxPercentage", 0.0),
                    isTipPercentage = obj.optBoolean("isTipPercentage", false),
                    tipPercentage = obj.optDouble("tipPercentage", 0.0),
                    isDiscountPercentage = obj.optBoolean("isDiscountPercentage", false),
                    discountPercentage = obj.optDouble("discountPercentage", 0.0),
                    vatAmount = obj.optDouble("vatAmount", 0.0),
                    isVatPercentage = obj.optBoolean("isVatPercentage", true),
                    vatPercentage = obj.optDouble("vatPercentage", 0.0)
                )
            }

            val modeStr = prefs.getString("calculation_mode", null)
            val mode = try { CalculationMode.valueOf(modeStr ?: "") } catch (e: Exception) { CalculationMode.SINGLE_BILL }
            val tripName = prefs.getString("trip_name", "Group Trip") ?: "Group Trip"

            val tripExpensesJson = prefs.getString("trip_expenses_json", null)
            val tripExpenses = mutableListOf<TripExpense>()
            if (!tripExpensesJson.isNullOrEmpty()) {
                val array = JSONArray(tripExpensesJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val splittersSet = mutableSetOf<String>()
                    val splittersArray = obj.optJSONArray("splitWithPersonIds")
                    if (splittersArray != null) {
                        for (j in 0 until splittersArray.length()) {
                            splittersSet.add(splittersArray.getString(j))
                        }
                    }
                    tripExpenses.add(
                        TripExpense(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            paidByPersonId = obj.getString("paidByPersonId"),
                            splitWithPersonIds = splittersSet,
                            category = obj.optString("category", "General"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val activeLobbyCode = prefs.getString("active_lobby_code", null)
            val currentUserId = prefs.getString("current_user_id", null)
            val paidByPersonId = prefs.getString("paid_by_person_id", null)
            val isDarkMode = prefs.getBoolean("is_dark_mode", false)

            return SplitterUiState(
                currentStep = step,
                calculationMode = mode,
                tripName = tripName,
                activeLobbyCode = activeLobbyCode,
                currentUserId = currentUserId,
                people = people,
                items = items,
                tripExpenses = tripExpenses,
                taxAndTip = taxAndTip,
                isScanning = false,
                paidByPersonId = paidByPersonId,
                isDarkMode = isDarkMode
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun clearState() {
        val historyJson = prefs.getString("history_json", null)
        val savedGroupsJson = prefs.getString("saved_groups_json", null)
        val isDarkMode = prefs.getBoolean("is_dark_mode", false)
        prefs.edit().clear().apply()
        val editor = prefs.edit()
        if (historyJson != null) {
            editor.putString("history_json", historyJson)
        }
        if (savedGroupsJson != null) {
            editor.putString("saved_groups_json", savedGroupsJson)
        }
        editor.putBoolean("is_dark_mode", isDarkMode)
        editor.apply()
    }

    fun saveHistory(records: List<BillHistoryRecord>) {
        val array = JSONArray()
        for (rec in records) {
            val obj = JSONObject()
            obj.put("id", rec.id)
            obj.put("timestamp", rec.timestamp)
            obj.put("paidByPersonId", rec.paidByPersonId ?: "")
            obj.put("totalAmount", rec.totalAmount)

            val peopleArray = JSONArray()
            for (p in rec.people) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("color", p.color)
                pObj.put("emoji", p.emoji)
                pObj.put("phoneNumber", p.phoneNumber ?: "")
                peopleArray.put(pObj)
            }
            obj.put("people", peopleArray)

            val itemsArray = JSONArray()
            for (item in rec.items) {
                val iObj = JSONObject()
                iObj.put("id", item.id)
                iObj.put("name", item.name)
                iObj.put("price", item.price)
                iObj.put("category", item.category.name)
                val assignees = JSONArray()
                for (pid in item.assignedPersonIds) {
                    assignees.put(pid)
                }
                iObj.put("assignedPersonIds", assignees)
                itemsArray.put(iObj)
            }
            obj.put("items", itemsArray)

            val ttObj = JSONObject()
            ttObj.put("taxAmount", rec.taxAndTip.taxAmount)
            ttObj.put("tipAmount", rec.taxAndTip.tipAmount)
            ttObj.put("discountAmount", rec.taxAndTip.discountAmount)
            ttObj.put("isTaxPercentage", rec.taxAndTip.isTaxPercentage)
            ttObj.put("taxPercentage", rec.taxAndTip.taxPercentage)
            ttObj.put("isTipPercentage", rec.taxAndTip.isTipPercentage)
            ttObj.put("tipPercentage", rec.taxAndTip.tipPercentage)
            ttObj.put("isDiscountPercentage", rec.taxAndTip.isDiscountPercentage)
            ttObj.put("discountPercentage", rec.taxAndTip.discountPercentage)
            ttObj.put("vatAmount", rec.taxAndTip.vatAmount)
            ttObj.put("isVatPercentage", rec.taxAndTip.isVatPercentage)
            ttObj.put("vatPercentage", rec.taxAndTip.vatPercentage)
            obj.put("taxAndTip", ttObj)

            array.put(obj)
        }
        prefs.edit().putString("history_json", array.toString()).apply()
    }

    fun loadHistory(): List<BillHistoryRecord> {
        val historyJson = prefs.getString("history_json", null) ?: return emptyList()
        val list = mutableListOf<BillHistoryRecord>()
        try {
            val array = JSONArray(historyJson)
            for (idx in 0 until array.length()) {
                val obj = array.getJSONObject(idx)
                val id = obj.getString("id")
                val timestamp = obj.getLong("timestamp")
                val paidByPersonId = obj.optString("paidByPersonId").ifEmpty { null }
                val totalAmount = obj.getDouble("totalAmount")

                val peopleArray = obj.getJSONArray("people")
                val people = mutableListOf<Person>()
                for (i in 0 until peopleArray.length()) {
                    val pObj = peopleArray.getJSONObject(i)
                    people.add(
                        Person(
                            id = pObj.getString("id"),
                            name = pObj.getString("name"),
                            color = pObj.getLong("color"),
                            emoji = pObj.optString("emoji", "😎"),
                            phoneNumber = pObj.optString("phoneNumber", "").ifEmpty { null }
                        )
                    )
                }

                val itemsArray = obj.getJSONArray("items")
                val items = mutableListOf<BillItem>()
                for (i in 0 until itemsArray.length()) {
                    val iObj = itemsArray.getJSONObject(i)
                    val assigneesSet = mutableSetOf<String>()
                    val assigneesArray = iObj.optJSONArray("assignedPersonIds")
                    if (assigneesArray != null) {
                        for (j in 0 until assigneesArray.length()) {
                            assigneesSet.add(assigneesArray.getString(j))
                        }
                    }
                    val catStr = iObj.optString("category", "FOOD")
                    val category = try { ItemCategory.valueOf(catStr) } catch (e: Exception) { ItemCategory.FOOD }
                    items.add(
                        BillItem(
                            id = iObj.getString("id"),
                            name = iObj.getString("name"),
                            price = iObj.getDouble("price"),
                            assignedPersonIds = assigneesSet,
                            category = category
                        )
                    )
                }

                val ttObj = obj.getJSONObject("taxAndTip")
                val taxAndTip = TaxAndTip(
                    taxAmount = ttObj.optDouble("taxAmount", 0.0),
                    tipAmount = ttObj.optDouble("tipAmount", 0.0),
                    discountAmount = ttObj.optDouble("discountAmount", 0.0),
                    isTaxPercentage = ttObj.optBoolean("isTaxPercentage", false),
                    taxPercentage = ttObj.optDouble("taxPercentage", 0.0),
                    isTipPercentage = ttObj.optBoolean("isTipPercentage", false),
                    tipPercentage = ttObj.optDouble("tipPercentage", 0.0),
                    isDiscountPercentage = ttObj.optBoolean("isDiscountPercentage", false),
                    discountPercentage = ttObj.optDouble("discountPercentage", 0.0),
                    vatAmount = ttObj.optDouble("vatAmount", 0.0),
                    isVatPercentage = ttObj.optBoolean("isVatPercentage", false),
                    vatPercentage = ttObj.optDouble("vatPercentage", 0.0)
                )

                list.add(
                    BillHistoryRecord(
                        id = id,
                        timestamp = timestamp,
                        people = people,
                        items = items,
                        taxAndTip = taxAndTip,
                        paidByPersonId = paidByPersonId,
                        totalAmount = totalAmount
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveGroups(groups: List<SavedGroup>) {
        val array = JSONArray()
        for (g in groups) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            val peopleArray = JSONArray()
            for (p in g.members) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("color", p.color)
                pObj.put("emoji", p.emoji)
                pObj.put("phoneNumber", p.phoneNumber ?: "")
                peopleArray.put(pObj)
            }
            obj.put("members", peopleArray)
            array.put(obj)
        }
        prefs.edit().putString("saved_groups_json", array.toString()).apply()
    }

    fun loadGroups(): List<SavedGroup> {
        val json = prefs.getString("saved_groups_json", null) ?: return emptyList()
        val list = mutableListOf<SavedGroup>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val peopleArray = obj.getJSONArray("members")
                val members = mutableListOf<Person>()
                for (j in 0 until peopleArray.length()) {
                    val pObj = peopleArray.getJSONObject(j)
                    members.add(
                        Person(
                            id = pObj.getString("id"),
                            name = pObj.getString("name"),
                            color = pObj.getLong("color"),
                            emoji = pObj.optString("emoji", "😎"),
                            phoneNumber = pObj.optString("phoneNumber", "").ifEmpty { null }
                        )
                    )
                }
                list.add(SavedGroup(id = id, name = name, members = members))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveLobbies(lobbies: List<LobbySession>) {
        val array = JSONArray()
        for (session in lobbies) {
            val obj = JSONObject()
            obj.put("code", session.code)
            obj.put("name", session.name)
            obj.put("hostPersonId", session.hostPersonId)
            obj.put("createdAt", session.createdAt)

            val membersArr = JSONArray()
            for (p in session.members) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("color", p.color)
                pObj.put("emoji", p.emoji)
                pObj.put("phoneNumber", p.phoneNumber ?: "")
                pObj.put("upiId", p.upiId ?: "")
                membersArr.put(pObj)
            }
            obj.put("members", membersArr)

            val expensesArr = JSONArray()
            for (exp in session.expenses) {
                val eObj = JSONObject()
                eObj.put("id", exp.id)
                eObj.put("title", exp.title)
                eObj.put("amount", exp.amount)
                eObj.put("paidByPersonId", exp.paidByPersonId)
                eObj.put("category", exp.category)
                eObj.put("timestamp", exp.timestamp)
                val splitters = JSONArray()
                for (pid in exp.splitWithPersonIds) {
                    splitters.put(pid)
                }
                eObj.put("splitWithPersonIds", splitters)
                expensesArr.put(eObj)
            }
            obj.put("expenses", expensesArr)

            val settlementsArr = JSONArray()
            for (set in session.settlements) {
                val sObj = JSONObject()
                sObj.put("id", set.id)
                sObj.put("lobbyCode", set.lobbyCode)
                sObj.put("fromPersonId", set.fromPersonId)
                sObj.put("toPersonId", set.toPersonId)
                sObj.put("amount", set.amount)
                sObj.put("timestamp", set.timestamp)
                sObj.put("transactionRef", set.transactionRef ?: "")
                settlementsArr.put(sObj)
            }
            obj.put("settlements", settlementsArr)

            val activitiesArr = JSONArray()
            for (act in session.activities) {
                val aObj = JSONObject()
                aObj.put("id", act.id)
                aObj.put("lobbyCode", act.lobbyCode)
                aObj.put("actorPersonId", act.actorPersonId)
                aObj.put("actorName", act.actorName)
                aObj.put("actionType", act.actionType)
                aObj.put("description", act.description)
                aObj.put("amount", act.amount)
                aObj.put("timestamp", act.timestamp)
                activitiesArr.put(aObj)
            }
            obj.put("activities", activitiesArr)

            array.put(obj)
        }
        prefs.edit().putString("saved_lobbies_json", array.toString()).apply()
    }

    fun loadLobbies(): List<LobbySession> {
        val json = prefs.getString("saved_lobbies_json", null) ?: return emptyList()
        val list = mutableListOf<LobbySession>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val code = obj.getString("code")
                val name = obj.getString("name")
                val hostPersonId = obj.getString("hostPersonId")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                val membersArr = obj.getJSONArray("members")
                val members = mutableListOf<Person>()
                for (mIdx in 0 until membersArr.length()) {
                    val pObj = membersArr.getJSONObject(mIdx)
                    members.add(
                        Person(
                            id = pObj.getString("id"),
                            name = pObj.getString("name"),
                            color = pObj.getLong("color"),
                            emoji = pObj.optString("emoji", "😎"),
                            phoneNumber = pObj.optString("phoneNumber", "").ifEmpty { null },
                            upiId = pObj.optString("upiId", "").ifEmpty { null }
                        )
                    )
                }

                val expensesArr = obj.optJSONArray("expenses")
                val expenses = mutableListOf<TripExpense>()
                if (expensesArr != null) {
                    for (eIdx in 0 until expensesArr.length()) {
                        val eObj = expensesArr.getJSONObject(eIdx)
                        val splittersSet = mutableSetOf<String>()
                        val splittersArray = eObj.optJSONArray("splitWithPersonIds")
                        if (splittersArray != null) {
                            for (sIdx in 0 until splittersArray.length()) {
                                splittersSet.add(splittersArray.getString(sIdx))
                            }
                        }
                        expenses.add(
                            TripExpense(
                                id = eObj.getString("id"),
                                title = eObj.getString("title"),
                                amount = eObj.getDouble("amount"),
                                paidByPersonId = eObj.getString("paidByPersonId"),
                                splitWithPersonIds = splittersSet,
                                category = eObj.optString("category", "General"),
                                timestamp = eObj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val settlementsArr = obj.optJSONArray("settlements")
                val settlements = mutableListOf<com.example.splixter.data.TripSettlementRecord>()
                if (settlementsArr != null) {
                    for (sIdx in 0 until settlementsArr.length()) {
                        val sObj = settlementsArr.getJSONObject(sIdx)
                        settlements.add(
                            com.example.splixter.data.TripSettlementRecord(
                                id = sObj.getString("id"),
                                lobbyCode = sObj.getString("lobbyCode"),
                                fromPersonId = sObj.getString("fromPersonId"),
                                toPersonId = sObj.getString("toPersonId"),
                                amount = sObj.getDouble("amount"),
                                timestamp = sObj.optLong("timestamp", System.currentTimeMillis()),
                                transactionRef = sObj.optString("transactionRef", "").ifEmpty { null }
                            )
                        )
                    }
                }

                val activitiesArr = obj.optJSONArray("activities")
                val activities = mutableListOf<com.example.splixter.data.TripActivity>()
                if (activitiesArr != null) {
                    for (aIdx in 0 until activitiesArr.length()) {
                        val aObj = activitiesArr.getJSONObject(aIdx)
                        activities.add(
                            com.example.splixter.data.TripActivity(
                                id = aObj.getString("id"),
                                lobbyCode = aObj.getString("lobbyCode"),
                                actorPersonId = aObj.getString("actorPersonId"),
                                actorName = aObj.getString("actorName"),
                                actionType = aObj.getString("actionType"),
                                description = aObj.getString("description"),
                                amount = aObj.optDouble("amount", 0.0),
                                timestamp = aObj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                list.add(
                    LobbySession(
                        code = code,
                        name = name,
                        hostPersonId = hostPersonId,
                        members = members,
                        expenses = expenses,
                        settlements = settlements,
                        activities = activities,
                        createdAt = createdAt
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveUserProfile(profile: UserProfile) {
        val obj = JSONObject().apply {
            put("id", profile.id)
            put("name", profile.name)
            put("color", profile.color)
            put("emoji", profile.emoji)
            put("phoneNumber", profile.phoneNumber ?: "")
            put("upiId", profile.upiId ?: "")
        }
        prefs.edit().putString("user_profile_json", obj.toString()).apply()
    }

    fun loadUserProfile(): UserProfile? {
        val json = prefs.getString("user_profile_json", null) ?: return null
        return try {
            val obj = JSONObject(json)
            UserProfile(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                name = obj.getString("name"),
                color = obj.optLong("color", 0xFF6C5CE7),
                emoji = obj.optString("emoji", "😎"),
                phoneNumber = obj.optString("phoneNumber", "").ifEmpty { null },
                upiId = obj.optString("upiId", "").ifEmpty { null }
            )
        } catch (e: Exception) {
            null
        }
    }
}
