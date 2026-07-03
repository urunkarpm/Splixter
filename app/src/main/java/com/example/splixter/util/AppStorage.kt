package com.example.splixter.util

import android.content.Context
import android.content.SharedPreferences
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillHistoryRecord
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.Person
import com.example.splixter.data.SavedGroup
import com.example.splixter.data.TaxAndTip
import com.example.splixter.ui.SplitterUiState
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("splixter_prefs", Context.MODE_PRIVATE)

    fun saveState(state: SplitterUiState) {
        val editor = prefs.edit()
        editor.putString("current_step", state.currentStep.name)

        // Save People
        val peopleArray = JSONArray()
        for (p in state.people) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("color", p.color)
            obj.put("emoji", p.emoji)
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
                            emoji = obj.optString("emoji", "😎")
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
                    isTaxPercentage = obj.optBoolean("isTaxPercentage", false),
                    taxPercentage = obj.optDouble("taxPercentage", 0.0),
                    isTipPercentage = obj.optBoolean("isTipPercentage", false),
                    tipPercentage = obj.optDouble("tipPercentage", 0.0),
                    isDiscountPercentage = obj.optBoolean("isDiscountPercentage", false),
                    discountPercentage = obj.optDouble("discountPercentage", 0.0),
                    vatAmount = obj.optDouble("vatAmount", 0.0),
                    isVatPercentage = obj.optBoolean("isVatPercentage", false),
                    vatPercentage = obj.optDouble("vatPercentage", 0.0)
                )
            }

            val paidByPersonId = prefs.getString("paid_by_person_id", null)
            val isDarkMode = prefs.getBoolean("is_dark_mode", false)

            return SplitterUiState(
                currentStep = step,
                people = people,
                items = items,
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
                            emoji = pObj.optString("emoji", "😎")
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
                            emoji = pObj.optString("emoji", "😎")
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
}
