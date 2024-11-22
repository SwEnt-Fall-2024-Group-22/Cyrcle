package com.github.se.cyrcle.ui.gambling

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamblingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun gambling_screen_shows_all_elements() {
    // When: The gambling screen is launched
    composeTestRule.setContent { GamblingScreen { wheelView -> wheelView.stopAnimation() } }

    // Then: All UI elements should be visible
    composeTestRule.onNodeWithTag("gambling_screen").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("wheel_view").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("spin_button").assertExists().assertIsDisplayed()
  }

  @Test
  fun spin_button_has_correct_properties() {
    // When: The gambling screen is launched
    composeTestRule.setContent { GamblingScreen { wheelView -> wheelView.stopAnimation() } }

    // Then: The spin button should have correct properties
    composeTestRule.onNodeWithTag("spin_button").assertHasClickAction().assertTextEquals("SPIN")
  }

  @Test
  fun wheel_view_has_correct_size() {
    // When: The gambling screen is launched
    composeTestRule.setContent { GamblingScreen { wheelView -> wheelView.stopAnimation() } }

    // Then: The wheel view should have the correct size
    composeTestRule
        .onNodeWithTag("wheel_view")
        .assertHeightIsEqualTo(300.dp)
        .assertWidthIsEqualTo(300.dp)
  }

  @Test
  fun spin_button_has_correct_size() {
    // When: The gambling screen is launched
    composeTestRule.setContent { GamblingScreen { wheelView -> wheelView.stopAnimation() } }

    // Then: The spin button should have the correct size
    composeTestRule
        .onNodeWithTag("spin_button")
        .assertHeightIsEqualTo(90.dp)
        .assertWidthIsEqualTo(90.dp)
  }

  @Test
  fun wheel_segments_have_correct_probabilities() {
    var wheelView: WheelView? = null

    composeTestRule.setContent {
      GamblingScreen { view ->
        wheelView = view
        view.stopAnimation()
      }
    }

    val segments =
        wheelView!!
            .javaClass
            .getDeclaredField("segments")
            .apply { isAccessible = true }
            .get(wheelView) as List<*>

    val probabilities =
        segments.map {
          it!!.javaClass.getDeclaredField("probability").apply { isAccessible = true }.get(it)
              as Float
        }

    assertEquals(100f, probabilities.sum(), 0.1f)
    assertEquals(50f, probabilities[0], 0.1f) // Nothing
    assertEquals(0.1f, probabilities[1], 0.1f) // Legendary
    assertEquals(32f, probabilities[2], 0.1f) // Common
    assertEquals(16f, probabilities[3], 0.1f) // Rare
    assertEquals(1.9f, probabilities[4], 0.1f) // Epic
  }

  @Test
  fun wheel_segments_have_correct_colors() {
    var wheelView: WheelView? = null

    composeTestRule.setContent {
      GamblingScreen { view ->
        wheelView = view
        view.stopAnimation()
      }
    }

    val segments =
        wheelView!!
            .javaClass
            .getDeclaredField("segments")
            .apply { isAccessible = true }
            .get(wheelView) as List<*>

    val colors =
        segments.map {
          it!!.javaClass.getDeclaredField("color").apply { isAccessible = true }.get(it) as Int
        }

    assertEquals(Color.GRAY, colors[0]) // Nothing
    assertEquals(Color.rgb(255, 215, 0), colors[1]) // Legendary
    assertEquals(Color.GREEN, colors[2]) // Common
    assertEquals(Color.BLUE, colors[3]) // Rare
    assertEquals(Color.rgb(128, 0, 128), colors[4]) // Epic
  }

  @Test
  fun spin_button_triggers_wheel_spin() {
    var wheelView: WheelView? = null
    val latch = CountDownLatch(1)

    composeTestRule.setContent {
      GamblingScreen { view ->
        wheelView = view
        view.stopAnimation()
      }
    }

    composeTestRule.onNodeWithTag("spin_button").performClick()

    // Check if spinning was initiated
    val isSpinning =
        wheelView!!
            .javaClass
            .getDeclaredField("isSpinning")
            .apply { isAccessible = true }
            .get(wheelView) as Boolean

    assertTrue(isSpinning)
  }

  @Test
  fun probability_ranges_are_continuous() {
    var wheelView: WheelView? = null

    composeTestRule.setContent {
      GamblingScreen { view ->
        wheelView = view
        view.stopAnimation()
      }
    }

    val ranges =
        wheelView!!
            .javaClass
            .getDeclaredField("probabilityRanges")
            .apply { isAccessible = true }
            .get(wheelView) as List<IntRange>

    // Check that ranges are continuous
    for (i in 0 until ranges.size - 1) {
      assertEquals(ranges[i].last + 1, ranges[i + 1].first)
    }

    // Check total range
    assertEquals(0, ranges.first().first)
    assertEquals(999, ranges.last().last)
  }

  @Test
  fun idle_rotation_speed_is_correct() {
    var wheelView: WheelView? = null

    composeTestRule.setContent {
      GamblingScreen { view ->
        wheelView = view
        view.stopAnimation()
      }
    }

    val idleRotationSpeed =
        wheelView!!
            .javaClass
            .getDeclaredField("idleRotationSpeed")
            .apply { isAccessible = true }
            .get(wheelView) as Float

    assertEquals(-0.2f, idleRotationSpeed, 0.001f)
  }

  @Test
  fun test_wheel_view_creation() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val wheelView = WheelView(context)

    assertNotNull(wheelView)
    assertEquals(View.VISIBLE, wheelView.visibility)
  }

  @Test
  fun test_onDraw_execution() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val wheelView = WheelView(context)

    // Force multiple redraws
    repeat(5) {
      wheelView.invalidate()
      runBlocking { delay(100) }
    }
  }

  @Test
  fun test_spin_with_different_view_sizes() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val wheelView = WheelView(context)

    // Test different view sizes
    val sizes = listOf(100, 200, 300, 400, 500)
    sizes.forEach { size ->
      wheelView.layout(0, 0, size, size)
      wheelView.spin()
      runBlocking { delay(100) }
    }
  }
}
