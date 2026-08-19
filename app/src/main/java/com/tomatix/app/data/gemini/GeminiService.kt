package com.tomatix.app.data.gemini

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiService(
    private val apiKey: String
) {

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"

    suspend fun generateContent(prompt: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext null
        }
        val requestBody = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )
            )
            .toString()

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }

        try {
            connection.outputStream.use { it.write(requestBody.toByteArray(Charsets.UTF_8)) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: return@withContext null

            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text")
                ?.takeIf { it.isNotBlank() }
                ?.trim()
        } finally {
            connection.disconnect()
        }
    }
}