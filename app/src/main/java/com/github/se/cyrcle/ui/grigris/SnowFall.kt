package com.github.se.cyrcle.ui.grigris

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.se.cyrcle.R
import com.github.se.cyrcle.ui.theme.CeruleanLowest
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * A simple snowfall animation where snowflakes fall from the top of the screen to the bottom. The
 * animation uses Jetpack Compose's Canvas to draw snowflakes as white circles, and their positions
 * are updated over time to simulate falling.
 */
@Composable
fun SnowfallAnimation() {
  // The size of the canvas (available drawing area). Initialized with a default size of 1000x500.
  // This value is updated when the canvas size becomes available.
  var size by remember { mutableStateOf(Size(1000f, 800f)) }

  // Create a list of 100 snowflakes, initialized with the canvas size
  val snowflakes = remember { List(100) { Snowflake(size) } }

  // Draw a slightly transparent blue background to simulate the sky
  Box(modifier = Modifier.fillMaxSize().background(CeruleanLowest.copy(alpha = 0.1f))) {
    // Draw the snowflakes on the canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      // Update the canvas size when it becomes available
      size = this.size

      // Draw each snowflake as a white circle
      snowflakes.forEach { snowflake -> drawSnowflake(snowflake) }
    }
  }

  // Continuously update the positions of the snowflakes in a loop
  LaunchedEffect(size) {
    while (true) {
      snowflakes.forEach { it.update(size) }
      delay(16) // Roughly 60 frames per second (FPS)
    }
  }
}

/**
 * Represents a single snowflake in the snowfall animation. Each snowflake has a position (`x`,
 * `y`), a radius, and a speed at which it falls.
 *
 * @param initialSize The initial size of the canvas. Used to set random initial positions for the
 *   snowflake.
 */
class Snowflake(initialSize: Size) {
  var x by mutableFloatStateOf(0f) // Horizontal position of the snowflake
  var y by mutableFloatStateOf(0f) // Vertical position of the snowflake
  var radius by mutableFloatStateOf(0f) // Radius of the snowflake (visual size)
  private var speed by mutableFloatStateOf(0f) // Falling speed of the snowflake

  init {
    reset(initialSize.width, initialSize.height)
  }

  /**
   * Resets the attributes of the snowflake (position, radius, and speed). The snowflake is placed
   * at a random position at the top of the screen with random size and speed.
   *
   * @param width The width of the canvas (used for randomizing x position).
   * @param height The height of the canvas (used for randomizing y position).
   */
  private fun reset(width: Float, height: Float) {
    x = Random.nextFloat() * width // Random horizontal position
    y = -Random.nextFloat() * height / 2 // Start above the visible area
    radius = 1f + Random.nextFloat() * 4 // Random radius between 1 and 5
    speed = 1f + Random.nextFloat() * 3 // Random falling speed between 1 and 4
  }

  /**
   * Updates the vertical position (`y`) of the snowflake based on its speed. If it falls below the
   * screen, its attributes are reset to simulate re-entering from above.
   *
   * @param size The current size of the canvas. Used to determine boundaries for resetting.
   */
  fun update(size: Size) {
    y += speed
    if (y > size.height + radius) { // If it falls below the screen
      reset(size.width, size.height)
    }
  }
}

/**
 * Draws a single snowflake on the canvas. A snowflake is represented as a white circle.
 *
 * @param snowflake The `Snowflake` object containing its position and radius.
 */
fun DrawScope.drawSnowflake(snowflake: Snowflake) {
  drawCircle(
      color = Color.White, radius = snowflake.radius, center = Offset(snowflake.x, snowflake.y))
}

/** Shows a toast message indicating whether it's snowing or not. */
fun snowToast(isSnowing: Boolean, context: Context) {
  val toastMessageRes = if (isSnowing) R.string.snow_enabled else R.string.snow_disabled
  Toast.makeText(context, context.getString(toastMessageRes), Toast.LENGTH_SHORT).show()
}
