package com.example.data.generator

import com.example.data.model.Prediction
import com.example.data.model.PredictionStatus
import kotlin.random.Random

object WinGoGenerator {

    private val PLANS = listOf("x1", "x3", "x9", "x27")

    fun generatePrediction(period: Long): Prediction {
        val lucky = Random.nextInt(0, 10) // 0 to 9 inclusive
        val size = if (lucky >= 5) "BIG" else "SMALL"

        val betOn = when (lucky) {
            1, 3, 7, 9 -> "🟢 GREEN"
            2, 4, 6, 8 -> "🔴 RED"
            5 -> "🟢 GREEN + 🟣 VIOLET"
            else -> "🔴 RED + 🟣 VIOLET" // 0
        }

        val plan = PLANS.random()

        return Prediction(
            period = period,
            luckyNumber = lucky,
            size = size,
            betOn = betOn,
            plan = plan,
            timestamp = System.currentTimeMillis(),
            status = PredictionStatus.PENDING,
            telegramPosted = false
        )
    }

    fun formatSignalMessage(prediction: Prediction, officialTag: String): String {
        return """🎮 <b>WIN GO 1 MIN LIVE PREDICTION</b> 🎮
━━━━━━━━━━━━━━━━━━━━━
🆔 <b>PERIOD:</b> ${prediction.period}
🎯 <b>BET ON:</b> ${prediction.betOn}
🔮 <b>LUCKY NUMBER:</b> [  ${prediction.luckyNumber}  ] |${prediction.size}
📊 <b>PLAN:</b> ${prediction.plan}
⏰ <b>TIME:</b> 1 MINUTE LIVE
━━━━━━━━━━━━━━━━━━━━━
📢 <b>OFFICIAL:</b> $officialTag"""
    }

    fun formatWinMessage(period: Long, officialTag: String): String {
        return """📊 <b>PERIOD:</b> $period

🔥 <b>STATUS:</b> Win ✅

📢 <b>OFFICIAL:</b> $officialTag"""
    }

    fun formatLossMessage(period: Long, officialTag: String): String {
        return """📊 <b>PERIOD:</b> $period

✨ <b>STATUS:</b> Loss ❌

📢 <b>OFFICIAL:</b> $officialTag"""
    }
}
