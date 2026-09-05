package com.tomatix.app.data.gemini

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** A single chat turn in the format expected by the Gemini REST API. */
data class GeminiChatMessage(
    val role: String,
    val text: String
)

class GeminiService(
    private val apiKey: String
) {

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"

    /**
     * Sends a real multi-turn conversation to Gemini. The live sensor snapshot is placed in
     * the system instruction so the model can use it on every answer without mistaking it for
     * something the farmer typed.
     */
    suspend fun generateContent(
        messages: List<GeminiChatMessage>,
        liveSensorContext: String
    ): AiResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiResult.ConfigurationError
        }
        if (messages.isEmpty()) {
            return@withContext AiResult.ApiError("Please enter a question for Tomi.")
        }

        var connection: HttpURLConnection? = null
        try {
            val requestBody = buildRequestBody(messages, liveSensorContext)
            val requestBytes = requestBody.toByteArray(Charsets.UTF_8)

            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setFixedLengthStreamingMode(requestBytes.size)
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
            }

            connection.outputStream.use { it.write(requestBytes) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (responseCode !in 200..299) {
                return@withContext AiResult.ApiError(apiErrorMessage(responseCode, responseText))
            }

            extractGeneratedText(responseText)?.let(AiResult::Success)
                ?: AiResult.ApiError("Tomi could not generate a response. Please try again.")
        } catch (_: IOException) {
            AiResult.ApiError("Tomi could not connect. Check your internet connection and try again.")
        } catch (_: Exception) {
            AiResult.ApiError("Tomi could not read the AI response. Please try again.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun buildRequestBody(
        messages: List<GeminiChatMessage>,
        liveSensorContext: String
    ): String {
        val contents = JSONArray().apply {
            messages.forEach { message ->
                put(
                    JSONObject()
                        .put("role", message.role)
                        .put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", message.text))
                        )
                )
            }
        }

        return JSONObject()
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            """
                                You are Tomi, the helpful AI assistant inside the Tomatix greenhouse app.
                                Give practical, concise and safe guidance about tomato cultivation and greenhouse management.
                                Converse in English, Filipino, Bisaya (Cebuano), Hiligaynon (Ilonggo), and
                                Kinaray-a (also written Karay-a, Karaya, or Kiniray-a).
                                Choose the reply language again for EVERY new user message. Match the language or
                                dialect of the LATEST user message, even when it suddenly differs from previous turns.
                                Switch immediately and naturally, without requiring a language setting, confirmation,
                                or an announcement that you are switching. Older language requests and your previous
                                replies must not override a clear language change in the latest user message.
                                If the latest message explicitly requests a reply in a particular language, follow
                                that request for this reply. Otherwise use the latest message's own language.
                                For mixed-language messages, match the dominant local language and natural code-switching
                                style; an English farming term alone does not mean the user has switched to English.
                                Only when the latest message has no clear language signal (such as a number, emoji,
                                or a short shared word), use the most recent clear user language as a fallback.
                                For example, a Bisaya question followed by a Hiligaynon question needs a Hiligaynon
                                answer immediately; if the next question is Kinaray-a, answer that one in Kinaray-a.
                                Treat Bisaya, Hiligaynon, and Kinaray-a as distinct languages; do not substitute one
                                for another. If the intended language or a regional word is unclear, ask briefly.
                                Use everyday farming language and explain technical terms simply in that language.
                                Preserve all sensor numbers and measurement units exactly when changing languages.
                                Use the live sensor snapshot below whenever it is relevant. It is the latest synced
                                reading, so do not invent different readings or claim it is a future prediction.
                                You cannot turn equipment on or off, water plants, or perform any physical action.
                                If there is no live data, say so plainly and give general advice instead.

                                LIVE SENSOR SNAPSHOT
                                $liveSensorContext
                                END LIVE SENSOR SNAPSHOT
                            """.trimIndent()
                        )
                    )
                )
            )
            .put("contents", contents)
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.4)
                    .put("maxOutputTokens", 600)
            )
            .toString()
    }

    private fun extractGeneratedText(responseText: String): String? = runCatching {
        val parts = JSONObject(responseText)
            .optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?: return@runCatching null

        buildList {
            for (index in 0 until parts.length()) {
                parts.optJSONObject(index)
                    ?.optString("text")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let(::add)
            }
        }.joinToString("\n").takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun apiErrorMessage(responseCode: Int, responseText: String): String {
        val apiMessage = runCatching {
            JSONObject(responseText)
                .optJSONObject("error")
                ?.optString("message")
                ?.lowercase()
                .orEmpty()
        }.getOrDefault("")

        return when (responseCode) {
            400 -> "Tomi could not process that question. Please try wording it another way."
            401, 403 -> "Tomi needs a valid Google AI Studio API key."
            404 -> "Tomi's AI model is unavailable right now. Please try again later."
            429 -> "Tomi has reached the AI request limit. Please try again shortly."
            in 500..599 -> "Tomi is temporarily unavailable. Please try again shortly."
            else -> if (apiMessage.contains("api key")) {
                "Tomi needs a valid Google AI Studio API key."
            } else {
                "Tomi is temporarily unavailable. Please try again."
            }
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 60_000
    }
}

sealed interface AiResult {
    data class Success(val text: String) : AiResult
    data object ConfigurationError : AiResult
    data class ApiError(val message: String) : AiResult
}
