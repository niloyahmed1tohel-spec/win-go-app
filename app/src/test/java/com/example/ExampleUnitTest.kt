package com.example

import com.example.data.generator.WinGoGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun winGoGenerator_producesValidPrediction() {
    val period = 20260913100010128L
    val prediction = WinGoGenerator.generatePrediction(period)

    assertEquals(period, prediction.period)
    assertTrue("Lucky number must be 0-9", prediction.luckyNumber in 0..9)

    val expectedSize = if (prediction.luckyNumber >= 5) "BIG" else "SMALL"
    assertEquals(expectedSize, prediction.size)

    val expectedBet = when (prediction.luckyNumber) {
      1, 3, 7, 9 -> "🟢 GREEN"
      2, 4, 6, 8 -> "🔴 RED"
      5 -> "🟢 GREEN + 🟣 VIOLET"
      else -> "🔴 RED + 🟣 VIOLET"
    }
    assertEquals(expectedBet, prediction.betOn)

    assertTrue("Plan must be in x1, x3, x9, x27", prediction.plan in listOf("x1", "x3", "x9", "x27"))

    val signalMsg = WinGoGenerator.formatSignalMessage(prediction, "@niloyEditzzone")
    assertTrue(signalMsg.contains("WIN GO 1 MIN LIVE PREDICTION"))
    assertTrue(signalMsg.contains(period.toString()))
    assertTrue(signalMsg.contains(prediction.betOn))
    assertTrue(signalMsg.contains(prediction.plan))
    assertTrue(signalMsg.contains("@niloyEditzzone"))

    val winMsg = WinGoGenerator.formatWinMessage(period, "@niloyEditzzone")
    assertTrue(winMsg.contains("Win ✅"))
    assertTrue(winMsg.contains("STATUS:"))
    assertTrue(winMsg.contains(period.toString()))

    val lossMsg = WinGoGenerator.formatLossMessage(period, "@niloyEditzzone")
    assertTrue(lossMsg.contains("Loss ❌"))
    assertTrue(lossMsg.contains("STATUS:"))
    assertTrue(lossMsg.contains(period.toString()))
  }
}

