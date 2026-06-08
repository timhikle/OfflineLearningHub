package com.offlinelearninghub.data.repository

import com.offlinelearninghub.BuildConfig
import com.offlinelearninghub.data.local.AppDatabase
import com.offlinelearninghub.data.local.TelegramMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiRepository(private val database: AppDatabase) {

    suspend fun processMessage(
        messageId: Long,
        chatId: Long,
        messageText: String
    ): Boolean = withContext(Dispatchers.IO) {
        val responseText = callGemini(messageText) ?: return@withContext false
        val resultJson = JSONObject(responseText)
        if (resultJson.optBoolean("is_important", false)) {
            val entity = TelegramMessageEntity(
                messageId = messageId,
                chatId = chatId,
                title = resultJson.optString("title", ""),
                summary = resultJson.optString("summary", ""),
                category = resultJson.optString("category", "general"),
                originalText = messageText
            )
            database.telegramMessageDao().insert(entity)
            true
        } else false
    }

    private suspend fun callGemini(messageText: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return null

        val systemPrompt = "أنت مساعد ذكي لطالب صيدلة في سوريا. اقرأ رسالة التلجرام. إذا كانت إعلاناً عاماً للدفعة أو تخص (الفئة الثانية) حصراً، أرجع JSON بهذا الشكل: {\"is_important\": true, \"category\": \"pdf/schedule/general\", \"title\": \"عنوان قصير\", \"summary\": \"ملخص بالعامية السورية\"}. إذا كانت تخص فئات أخرى، أرجع {\"is_important\": false}."

        val requestBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", messageText)))
            }))
        }

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 30000

        return try {
            OutputStreamWriter(connection.outputStream).use { it.write(requestBody.toString()) }
            val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
            val fullJson = JSONObject(response)
            fullJson
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text", null)
        } catch (_: Exception) { null }
    }
}
