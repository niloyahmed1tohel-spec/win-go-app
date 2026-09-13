package com.example.data.repository

import android.content.Context
import com.example.data.generator.WinGoGenerator
import com.example.data.local.AppDatabase
import com.example.data.local.PredictionEntity
import com.example.data.model.Prediction
import com.example.data.model.PredictionStatus
import com.example.data.remote.BotInfo
import com.example.data.remote.TelegramApiService
import com.example.data.remote.TelegramResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PredictionRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val telegramApi: TelegramApiService = TelegramApiService()
) {
    private val prefs = context.getSharedPreferences("wingo_settings", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_BOT_TOKEN = "8970868661:AAGOSNYBLmFbRma2b6PCIUPiUG2PXwduaxI"
        const val DEFAULT_CHAT_ID = "-1003951631341"
        const val DEFAULT_OFFICIAL_TAG = "@niloyEditzzone"
        const val DEFAULT_PERIOD = 20260913100010128L

        private const val KEY_BOT_TOKEN = "key_bot_token"
        private const val KEY_CHAT_ID = "key_chat_id"
        private const val KEY_OFFICIAL_TAG = "key_official_tag"
        private const val KEY_CURRENT_PERIOD = "key_current_period"
        private const val KEY_LAST_POSTED_PERIOD = "key_last_posted_period"
    }

    var botToken: String
        get() = prefs.getString(KEY_BOT_TOKEN, DEFAULT_BOT_TOKEN) ?: DEFAULT_BOT_TOKEN
        set(value) = prefs.edit().putString(KEY_BOT_TOKEN, value.trim()).apply()

    var chatId: String
        get() = prefs.getString(KEY_CHAT_ID, DEFAULT_CHAT_ID) ?: DEFAULT_CHAT_ID
        set(value) = prefs.edit().putString(KEY_CHAT_ID, value.trim()).apply()

    var officialTag: String
        get() = prefs.getString(KEY_OFFICIAL_TAG, DEFAULT_OFFICIAL_TAG) ?: DEFAULT_OFFICIAL_TAG
        set(value) = prefs.edit().putString(KEY_OFFICIAL_TAG, value.trim()).apply()

    var currentPeriod: Long
        get() = prefs.getLong(KEY_CURRENT_PERIOD, DEFAULT_PERIOD)
        set(value) = prefs.edit().putLong(KEY_CURRENT_PERIOD, value).apply()

    var lastPostedPeriod: Long
        get() = prefs.getLong(KEY_LAST_POSTED_PERIOD, DEFAULT_PERIOD)
        set(value) = prefs.edit().putLong(KEY_LAST_POSTED_PERIOD, value).apply()

    fun getPredictions(): Flow<List<Prediction>> {
        return database.predictionDao().getAllPredictionsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getLatestPrediction(): Prediction? {
        return database.predictionDao().getLatestPrediction()?.toDomain()
    }

    suspend fun postSignal(period: Long): Result<Prediction> {
        val prediction = WinGoGenerator.generatePrediction(period)
        val text = WinGoGenerator.formatSignalMessage(prediction, officialTag)

        val apiResult = telegramApi.sendMessage(
            botToken = botToken,
            chatId = chatId,
            text = text,
            parseMode = "HTML"
        )

        val (isSuccess, errorMsg) = when (apiResult) {
            is TelegramResult.Success -> Pair(true, null)
            is TelegramResult.Error -> Pair(false, apiResult.message)
        }

        val updatedPrediction = prediction.copy(
            telegramPosted = isSuccess,
            errorMessage = errorMsg
        )

        // Save to Room DB
        val insertedId = database.predictionDao().insertPrediction(
            PredictionEntity.fromDomain(updatedPrediction)
        )

        lastPostedPeriod = period
        currentPeriod = period + 1

        return if (isSuccess) {
            Result.success(updatedPrediction.copy(id = insertedId))
        } else {
            Result.failure(Exception(errorMsg ?: "Failed to post to Telegram"))
        }
    }

    suspend fun postWin(period: Long): Result<Unit> {
        val text = WinGoGenerator.formatWinMessage(period, officialTag)
        val apiResult = telegramApi.sendMessage(
            botToken = botToken,
            chatId = chatId,
            text = text,
            parseMode = "HTML"
        )

        database.predictionDao().updateStatusByPeriod(period, PredictionStatus.WIN.name)

        return when (apiResult) {
            is TelegramResult.Success -> Result.success(Unit)
            is TelegramResult.Error -> Result.failure(Exception(apiResult.message))
        }
    }

    suspend fun postLoss(period: Long): Result<Unit> {
        val text = WinGoGenerator.formatLossMessage(period, officialTag)
        val apiResult = telegramApi.sendMessage(
            botToken = botToken,
            chatId = chatId,
            text = text,
            parseMode = "HTML"
        )

        database.predictionDao().updateStatusByPeriod(period, PredictionStatus.LOSS.name)

        return when (apiResult) {
            is TelegramResult.Success -> Result.success(Unit)
            is TelegramResult.Error -> Result.failure(Exception(apiResult.message))
        }
    }

    suspend fun testBot(): TelegramResult<BotInfo> {
        return telegramApi.getMe(botToken)
    }

    suspend fun clearHistory() {
        database.predictionDao().clearAll()
    }
}
