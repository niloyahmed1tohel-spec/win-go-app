package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class TelegramResult<out T> {
    data class Success<out T>(val data: T) : TelegramResult<T>()
    data class Error(val message: String, val statusCode: Int? = null) : TelegramResult<Nothing>()
}

data class BotInfo(
    val id: Long,
    val firstName: String,
    val username: String?
)

data class MessageSentResult(
    val messageId: Long,
    val chatId: Long,
    val date: Long
)

class TelegramApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun getMe(botToken: String): TelegramResult<BotInfo> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token cannot be empty")
        }

        val url = "https://api.telegram.org/bot$cleanToken/getMe"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val desc = parseErrorDescription(body) ?: "HTTP ${response.code}: ${response.message}"
                    return@withContext TelegramResult.Error(desc, response.code)
                }

                val json = JSONObject(body)
                if (json.optBoolean("ok", false)) {
                    val result = json.getJSONObject("result")
                    val info = BotInfo(
                        id = result.getLong("id"),
                        firstName = result.getString("first_name"),
                        username = result.optString("username", null)
                    )
                    TelegramResult.Success(info)
                } else {
                    val desc = json.optString("description", "Failed to query Telegram Bot API")
                    TelegramResult.Error(desc)
                }
            }
        } catch (e: Exception) {
            TelegramResult.Error(e.localizedMessage ?: "Network error connecting to Telegram")
        }
    }

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String,
        parseMode: String = "HTML"
    ): TelegramResult<MessageSentResult> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        val cleanChatId = chatId.trim()

        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token is missing")
        }
        if (cleanChatId.isEmpty()) {
            return@withContext TelegramResult.Error("Chat ID is missing")
        }

        val url = "https://api.telegram.org/bot$cleanToken/sendMessage"

        val formBody = FormBody.Builder()
            .add("chat_id", cleanChatId)
            .add("text", text)
            .add("parse_mode", parseMode)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val desc = parseErrorDescription(body) ?: "HTTP ${response.code}: ${response.message}"
                    return@withContext TelegramResult.Error(desc, response.code)
                }

                val json = JSONObject(body)
                if (json.optBoolean("ok", false)) {
                    val result = json.getJSONObject("result")
                    val chatObj = result.optJSONObject("chat")
                    val returnedChatId = chatObj?.optLong("id") ?: 0L
                    val sentResult = MessageSentResult(
                        messageId = result.getLong("message_id"),
                        chatId = returnedChatId,
                        date = result.optLong("date", System.currentTimeMillis() / 1000)
                    )
                    TelegramResult.Success(sentResult)
                } else {
                    val desc = json.optString("description", "Telegram API returned ok=false")
                    TelegramResult.Error(desc)
                }
            }
        } catch (e: Exception) {
            TelegramResult.Error(e.localizedMessage ?: "Failed to send message to Telegram")
        }
    }

    private fun parseErrorDescription(body: String): String? {
        return try {
            val json = JSONObject(body)
            json.optString("description").takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }
}
