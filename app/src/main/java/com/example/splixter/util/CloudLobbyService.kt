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

class CloudLobbyService {

    private val baseUrl = "https://splixter-lobby-default-rtdb.firebaseio.com/lobbies/"

    suspend fun publishLobbySession(session: LobbySession): Boolean = withContext(Dispatchers.IO) {
        try {
            val code = session.code.uppercase()
            val url = URL("$baseUrl$code.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

            val json = serializeSession(session)
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(json.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            conn.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchLobbySession(code: String): LobbySession? = withContext(Dispatchers.IO) {
        try {
            val cleanCode = code.trim().uppercase()
            val url = URL("$baseUrl$cleanCode.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                conn.disconnect()

                val responseStr = sb.toString().trim()
                if (responseStr.isEmpty() || responseStr == "null") return@withContext null

                val obj = JSONObject(responseStr)
                return@withContext deserializeSession(obj)
            }
            conn.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun observeLobby(code: String, pollIntervalMs: Long = 3000): Flow<LobbySession?> = flow {
        while (true) {
            val session = fetchLobbySession(code)
            emit(session)
            delay(pollIntervalMs)
        }
    }.flowOn(Dispatchers.IO)

    private fun serializeSession(session: LobbySession): JSONObject {
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

        return obj
    }

    private fun deserializeSession(obj: JSONObject): LobbySession {
        val code = obj.optString("code", "")
        val name = obj.optString("name", "Trip")
        val hostPersonId = obj.optString("hostPersonId", "")
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

        val members = mutableListOf<Person>()
        val membersArr = obj.optJSONArray("members")
        if (membersArr != null) {
            for (i in 0 until membersArr.length()) {
                val pObj = membersArr.getJSONObject(i)
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
        }

        val expenses = mutableListOf<TripExpense>()
        val expensesArr = obj.optJSONArray("expenses")
        if (expensesArr != null) {
            for (i in 0 until expensesArr.length()) {
                val eObj = expensesArr.getJSONObject(i)
                val splittersSet = mutableSetOf<String>()
                val splittersArray = eObj.optJSONArray("splitWithPersonIds")
                if (splittersArray != null) {
                    for (j in 0 until splittersArray.length()) {
                        splittersSet.add(splittersArray.getString(j))
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

        return LobbySession(
            code = code,
            name = name,
            hostPersonId = hostPersonId,
            members = members,
            expenses = expenses,
            createdAt = createdAt
        )
    }
}
