package com.example.splixter.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseName: String,
    val releaseNotes: String,
    val htmlUrl: String,
    val apkDownloadUrl: String?
)

sealed class AppUpdateResult {
    data class Success(val info: AppUpdateInfo) : AppUpdateResult()
    data class NoUpdate(val currentVersion: String) : AppUpdateResult()
    data class Error(val message: String, val fallbackUrl: String = "https://github.com/urunkarpm/Splixter/releases") : AppUpdateResult()
}

class AppUpdateService {

    companion object {
        const val GITHUB_REPO_API = "https://api.github.com/repos/urunkarpm/Splixter/releases/latest"
        const val GITHUB_RELEASES_WEB = "https://github.com/urunkarpm/Splixter/releases"
        const val CURRENT_APP_VERSION = "2.0.0"
    }

    suspend fun checkForUpdates(currentVersion: String = CURRENT_APP_VERSION): AppUpdateResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_REPO_API)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Splixter-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)

                val tagName = json.optString("tag_name", "").trim()
                val releaseName = json.optString("name", tagName)
                val releaseNotes = json.optString("body", "Bug fixes and performance improvements.")
                val htmlUrl = json.optString("html_url", GITHUB_RELEASES_WEB)

                var apkDownloadUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            val url = asset.optString("browser_download_url", "")
                            if (url.isNotBlank()) {
                                apkDownloadUrl = url
                            }
                            break
                        }
                    }
                }

                val cleanLatest = tagName.removePrefix("v").removePrefix("V").trim()
                val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").trim()

                val isNewer = isVersionNewer(cleanLatest, cleanCurrent)

                val updateInfo = AppUpdateInfo(
                    isUpdateAvailable = isNewer,
                    latestVersion = if (tagName.startsWith("v", ignoreCase = true)) tagName else "v$cleanLatest",
                    currentVersion = if (currentVersion.startsWith("v", ignoreCase = true)) currentVersion else "v$cleanCurrent",
                    releaseName = releaseName,
                    releaseNotes = releaseNotes,
                    htmlUrl = htmlUrl,
                    apkDownloadUrl = apkDownloadUrl ?: htmlUrl
                )

                if (isNewer) {
                    AppUpdateResult.Success(updateInfo)
                } else {
                    AppUpdateResult.NoUpdate(currentVersion)
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // Repo or release not yet published on GitHub
                AppUpdateResult.NoUpdate(currentVersion)
            } else {
                AppUpdateResult.Error("HTTP error $responseCode while checking for updates.")
            }
        } catch (e: Exception) {
            AppUpdateResult.Error(e.localizedMessage ?: "Failed to check for updates")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Compare semantic versions e.g. "1.2.0" vs "1.0.0".
     * Returns true if latest > current.
     */
    fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank()) return false
        if (current.isBlank()) return true
        if (latest == current) return false

        try {
            val latestParts = latest.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        } catch (e: Exception) {
            return latest > current
        }
    }
}
