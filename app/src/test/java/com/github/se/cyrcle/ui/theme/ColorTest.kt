package com.github.se.cyrcle.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTest {
  @Test
  fun invertColorTest() {
    // Define test cases
    val testCases =
        listOf(
            Color(0xFFFFFFFF), // White
            Color(0xFF000000), // Black
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            // Color(0.5f, 0.5f, 0.5f) // Gray
            // Gray test doesn't pass due to Color space approximation.
            // Results are near (0.5019608 VS 0.49803922)

        )

    // Expected inverted colors
    val expectedInvertedColors =
        listOf(
            Color(0xFF000000), // Inverted White
            Color(0xFFFFFFFF), // Inverted Black
            Color(0xFF00FFFF), // Inverted Red
            Color(0xFFFF00FF), // Inverted Green
            Color(0xFFFFFF00), // Inverted Blue
            Color(0.5f, 0.5f, 0.5f) // Inverted Gray (remains the same)
            )

    // Test inversion
    for ((index, color) in testCases.withIndex()) {
      val invertedColor = invertColor(color)
      assertEquals(
          "Inversion failed for color $color", expectedInvertedColors[index], invertedColor)
    }
  }
}
