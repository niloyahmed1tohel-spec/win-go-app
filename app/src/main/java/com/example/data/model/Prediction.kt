package com.example.data.model

enum class PredictionStatus {
    PENDING,
    WIN,
    LOSS
}

data class Prediction(
    val id: Long = 0,
    val period: Long,
    val luckyNumber: Int,
    val size: String, // "BIG" or "SMALL"
    val betOn: String, // e.g. "🟢 GREEN", "🔴 RED", "🟢 GREEN + 🟣 VIOLET", "🔴 RED + 🟣 VIOLET"
    val plan: String, // "x1", "x3", "x9", "x27"
    val timestamp: Long = System.currentTimeMillis(),
    val status: PredictionStatus = PredictionStatus.PENDING,
    val telegramPosted: Boolean = false,
    val errorMessage: String? = null
)
