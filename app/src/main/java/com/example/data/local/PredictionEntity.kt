package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Prediction
import com.example.data.model.PredictionStatus

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val period: Long,
    val luckyNumber: Int,
    val size: String,
    val betOn: String,
    val plan: String,
    val timestamp: Long,
    val status: String,
    val telegramPosted: Boolean,
    val errorMessage: String? = null
) {
    fun toDomain(): Prediction {
        val parsedStatus = try {
            PredictionStatus.valueOf(status)
        } catch (_: Exception) {
            PredictionStatus.PENDING
        }
        return Prediction(
            id = id,
            period = period,
            luckyNumber = luckyNumber,
            size = size,
            betOn = betOn,
            plan = plan,
            timestamp = timestamp,
            status = parsedStatus,
            telegramPosted = telegramPosted,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun fromDomain(domain: Prediction): PredictionEntity {
            return PredictionEntity(
                id = domain.id,
                period = domain.period,
                luckyNumber = domain.luckyNumber,
                size = domain.size,
                betOn = domain.betOn,
                plan = domain.plan,
                timestamp = domain.timestamp,
                status = domain.status.name,
                telegramPosted = domain.telegramPosted,
                errorMessage = domain.errorMessage
            )
        }
    }
}
