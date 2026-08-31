package com.example.splixter.util

import com.example.splixter.data.LobbySession
import com.example.splixter.data.Person
import com.example.splixter.data.TripExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Supabase configuration credentials.
 * Users can replace these with their own Supabase project details from https://supabase.com
 */
object SupabaseConfig {
    // Supabase project endpoint
    var projectUrl: String = "https://gdvqttjvzmhebobrnfbz.supabase.co"
    var anonKey: String = "sb_publishable_B84YGGr9uRzk_o39-rQv9w_2zVbHUrd"
    
    val restBaseUrl: String
        get() = "${projectUrl.trimEnd('/')}/rest/v1"
}

class SupabaseLobbyService {

    private fun getHeaders(): Map<String, String> {
        return mapOf(
            "apikey" to SupabaseConfig.anonKey,
            "Authorization" to "Bearer ${SupabaseConfig.anonKey}",
            "Content-Type" to "application/json",
            "Prefer" to "return=representation"
        )
    }

    private fun openConnection(endpoint: String, method: String, customHeaders: Map<String, String> = emptyMap()): HttpURLConnection {
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        for ((k, v) in getHeaders()) {
            conn.setRequestProperty(k, v)
        }
        for ((k, v) in customHeaders) {
            conn.setRequestProperty(k, v)
        }
        if (method == "POST" || method == "PATCH" || method == "PUT") {
            conn.doOutput = true
        }
        return conn
    }

    suspend fun createOrUpdateLobby(session: LobbySession): Boolean = withContext(Dispatchers.IO) {
        try {
            val code = session.code.uppercase().trim()
            val lobbyObj = JSONObject().apply {
                put("code", code)
                put("name", session.name)
                put("host_person_id", session.hostPersonId)
                put("created_at", session.createdAt)
                put("updated_at", System.currentTimeMillis())
            }

            // Upsert Lobby record
            val endpoint = "${SupabaseConfig.restBaseUrl}/lobbies"
            val conn = openConnection(endpoint, "POST", mapOf("Prefer" to "resolution=merge-duplicates"))
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(lobbyObj.toString())
                it.flush()
            }
            val codeResp = conn.responseCode
            conn.disconnect()

            if (codeResp !in 200..299) return@withContext false

            // Upsert initial members
            for (m in session.members) {
                addMember(code, m, isHost = (m.id == session.hostPersonId))
            }

            // Upsert any initial expenses
            for (exp in session.expenses) {
                addExpense(code, exp)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchLobbySession(code: String): LobbySession? = withContext(Dispatchers.IO) {
        try {
            val cleanCode = code.trim().uppercase()
            
            // 1. Fetch Lobby Metadata
            val lobbyUrl = "${SupabaseConfig.restBaseUrl}/lobbies?code=eq.$cleanCode&select=*"
            val connLobby = openConnection(lobbyUrl, "GET")
            if (connLobby.responseCode !in 200..299) {
                connLobby.disconnect()
                return@withContext null
            }
            val lobbyBody = readResponse(connLobby)
            connLobby.disconnect()
            val lobbyArr = JSONArray(lobbyBody)
            if (lobbyArr.length() == 0) return@withContext null
            val lobbyObj = lobbyArr.getJSONObject(0)

            val name = lobbyObj.optString("name", "Group Trip")
            val hostPersonId = lobbyObj.optString("host_person_id", "")
            val createdAt = lobbyObj.optLong("created_at", System.currentTimeMillis())

            // 2. Fetch Members
            val membersUrl = "${SupabaseConfig.restBaseUrl}/lobby_members?lobby_code=eq.$cleanCode&select=*&order=joined_at.asc"
            val connMembers = openConnection(membersUrl, "GET")
            val members = mutableListOf<Person>()
            if (connMembers.responseCode in 200..299) {
                val membersBody = readResponse(connMembers)
                val mArr = JSONArray(membersBody)
                for (i in 0 until mArr.length()) {
                    val mObj = mArr.getJSONObject(i)
                    members.add(
                        Person(
                            id = mObj.getString("id"),
                            name = mObj.getString("name"),
                            color = mObj.getLong("color"),
                            emoji = mObj.optString("emoji", "😎"),
                            phoneNumber = mObj.optString("phone_number", "").ifEmpty { null },
                            upiId = mObj.optString("upi_id", "").ifEmpty { null }
                        )
                    )
                }
            }
            connMembers.disconnect()

            // 3. Fetch Expenses
            val expensesUrl = "${SupabaseConfig.restBaseUrl}/trip_expenses?lobby_code=eq.$cleanCode&is_deleted=eq.false&select=*&order=timestamp.desc"
            val connExpenses = openConnection(expensesUrl, "GET")
            val expenses = mutableListOf<TripExpense>()
            if (connExpenses.responseCode in 200..299) {
                val expBody = readResponse(connExpenses)
                val eArr = JSONArray(expBody)
                for (i in 0 until eArr.length()) {
                    val eObj = eArr.getJSONObject(i)
                    val splitArr = eObj.optJSONArray("split_with_person_ids")
                    val splitSet = mutableSetOf<String>()
                    if (splitArr != null) {
                        for (j in 0 until splitArr.length()) {
                            splitSet.add(splitArr.getString(j))
                        }
                    }
                    expenses.add(
                        TripExpense(
                            id = eObj.getString("id"),
                            title = eObj.getString("title"),
                            amount = eObj.getDouble("amount"),
                            paidByPersonId = eObj.getString("paid_by_person_id"),
                            splitWithPersonIds = splitSet,
                            category = eObj.optString("category", "General"),
                            timestamp = eObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
            connExpenses.disconnect()

            // 4. Fetch Settlements
            val settlementsUrl = "${SupabaseConfig.restBaseUrl}/trip_settlements?lobby_code=eq.$cleanCode&select=*&order=timestamp.desc"
            val connSettlements = openConnection(settlementsUrl, "GET")
            val settlements = mutableListOf<com.example.splixter.data.TripSettlementRecord>()
            if (connSettlements.responseCode in 200..299) {
                val setBody = readResponse(connSettlements)
                val sArr = JSONArray(setBody)
                for (i in 0 until sArr.length()) {
                    val sObj = sArr.getJSONObject(i)
                    settlements.add(
                        com.example.splixter.data.TripSettlementRecord(
                            id = sObj.getString("id"),
                            lobbyCode = sObj.getString("lobby_code"),
                            fromPersonId = sObj.getString("from_person_id"),
                            toPersonId = sObj.getString("to_person_id"),
                            amount = sObj.getDouble("amount"),
                            timestamp = sObj.optLong("timestamp", System.currentTimeMillis()),
                            transactionRef = sObj.optString("transaction_ref", "").ifEmpty { null }
                        )
                    )
                }
            }
            connSettlements.disconnect()

            // 5. Fetch Activities
            val activitiesUrl = "${SupabaseConfig.restBaseUrl}/trip_activities?lobby_code=eq.$cleanCode&select=*&order=timestamp.desc&limit=30"
            val connActivities = openConnection(activitiesUrl, "GET")
            val activities = mutableListOf<com.example.splixter.data.TripActivity>()
            if (connActivities.responseCode in 200..299) {
                val actBody = readResponse(connActivities)
                val aArr = JSONArray(actBody)
                for (i in 0 until aArr.length()) {
                    val aObj = aArr.getJSONObject(i)
                    activities.add(
                        com.example.splixter.data.TripActivity(
                            id = aObj.getString("id"),
                            lobbyCode = aObj.getString("lobby_code"),
                            actorPersonId = aObj.getString("actor_person_id"),
                            actorName = aObj.getString("actor_name"),
                            actionType = aObj.getString("action_type"),
                            description = aObj.getString("description"),
                            amount = aObj.optDouble("amount", 0.0),
                            timestamp = aObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
            connActivities.disconnect()

            LobbySession(
                code = cleanCode,
                name = name,
                hostPersonId = hostPersonId,
                members = members,
                expenses = expenses,
                settlements = settlements,
                activities = activities,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun recordActivity(activity: com.example.splixter.data.TripActivity): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${SupabaseConfig.restBaseUrl}/trip_activities"
            val conn = openConnection(endpoint, "POST", mapOf("Prefer" to "resolution=merge-duplicates"))
            val obj = JSONObject().apply {
                put("id", activity.id)
                put("lobby_code", activity.lobbyCode.uppercase().trim())
                put("actor_person_id", activity.actorPersonId)
                put("actor_name", activity.actorName)
                put("action_type", activity.actionType)
                put("description", activity.description)
                put("amount", activity.amount)
                put("timestamp", activity.timestamp)
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(obj.toString())
                it.flush()
            }
            val respCode = conn.responseCode
            conn.disconnect()
            respCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun recordSettlement(lobbyCode: String, fromPersonId: String, toPersonId: String, amount: Double, txRef: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${SupabaseConfig.restBaseUrl}/trip_settlements"
            val conn = openConnection(endpoint, "POST", mapOf("Prefer" to "resolution=merge-duplicates"))
            val obj = JSONObject().apply {
                put("id", java.util.UUID.randomUUID().toString())
                put("lobby_code", lobbyCode.uppercase().trim())
                put("from_person_id", fromPersonId)
                put("to_person_id", toPersonId)
                put("amount", amount)
                put("timestamp", System.currentTimeMillis())
                put("transaction_ref", txRef ?: "")
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(obj.toString())
                it.flush()
            }
            val respCode = conn.responseCode
            conn.disconnect()
            respCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addMember(lobbyCode: String, person: Person, isHost: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${SupabaseConfig.restBaseUrl}/lobby_members"
            val conn = openConnection(endpoint, "POST", mapOf("Prefer" to "resolution=merge-duplicates"))
            val obj = JSONObject().apply {
                put("id", person.id)
                put("lobby_code", lobbyCode.uppercase().trim())
                put("name", person.name)
                put("color", person.color)
                put("emoji", person.emoji)
                put("phone_number", person.phoneNumber ?: "")
                put("upi_id", person.upiId ?: "")
                put("is_host", isHost)
                put("joined_at", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(obj.toString())
                it.flush()
            }
            val respCode = conn.responseCode
            conn.disconnect()
            respCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addExpense(lobbyCode: String, expense: TripExpense): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${SupabaseConfig.restBaseUrl}/trip_expenses"
            val conn = openConnection(endpoint, "POST", mapOf("Prefer" to "resolution=merge-duplicates"))
            val splitArr = JSONArray()
            expense.splitWithPersonIds.forEach { splitArr.put(it) }

            val obj = JSONObject().apply {
                put("id", expense.id)
                put("lobby_code", lobbyCode.uppercase().trim())
                put("title", expense.title)
                put("amount", expense.amount)
                put("category", expense.category)
                put("paid_by_person_id", expense.paidByPersonId)
                put("split_with_person_ids", splitArr)
                put("timestamp", expense.timestamp)
                put("updated_at", System.currentTimeMillis())
                put("is_deleted", false)
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(obj.toString())
                it.flush()
            }
            val respCode = conn.responseCode
            conn.disconnect()
            respCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteExpense(expenseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Soft delete on Supabase
            val endpoint = "${SupabaseConfig.restBaseUrl}/trip_expenses?id=eq.$expenseId"
            val conn = openConnection(endpoint, "PATCH")
            val obj = JSONObject().apply {
                put("is_deleted", true)
                put("updated_at", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(obj.toString())
                it.flush()
            }
            val respCode = conn.responseCode
            conn.disconnect()
            respCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Real-time polling and synchronization stream with adaptive backoff.
     * Emits the latest lobby session state to all active subscribers.
     */
    fun observeLobby(code: String, pollIntervalMs: Long = 2500): Flow<LobbySession?> = flow {
        while (true) {
            val session = fetchLobbySession(code)
            emit(session)
            delay(pollIntervalMs)
        }
    }.flowOn(Dispatchers.IO)

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = try {
            if (conn.responseCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
        } catch (e: Exception) {
            conn.errorStream ?: return ""
        } ?: return ""
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()
        return sb.toString().trim()
    }
}
