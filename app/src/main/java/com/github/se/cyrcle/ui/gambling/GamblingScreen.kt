package com.github.se.cyrcle.ui.gambling

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.se.cyrcle.R
import kotlin.math.cos
import kotlin.math.sin

class WheelView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

  private val TAG = "WheelView"
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  private data class Segment(val name: String, val color: Int, val probability: Float)

  private val segments =
      listOf(
          Segment("Nothing", Color.GRAY, 50f), // 50%
          Segment("Legendary", Color.rgb(255, 215, 0), 0.1f), // 0.1%
          Segment("Common", Color.GREEN, 32f), // 32%
          Segment("Rare", Color.BLUE, 16f), // 16%
          Segment("Epic", Color.rgb(128, 0, 128), 1.9f) // 1.9%
          )

  private var isAnimating = true
  private lateinit var animationRunnable: Runnable

  // Range mapping for probabilities
  private val probabilityRanges = run {
    var currentValue = 0
    segments.map { segment ->
      val range = (segment.probability * 10).toInt()
      val start = currentValue
      currentValue += range
      start until currentValue
    }
  }

  private var rotation = 0f
  private var isSpinning = false
  private var targetRotation = 0f
  private var currentRotation = 0f
  private var spinStartTime = 0L
  private var spinDuration = 0L
  private var lastUpdateTime = System.currentTimeMillis()
  private val idleRotationSpeed = -0.2f
  private var pauseAfterSpin = false
  private var pauseStartTime = 0L
  private val pauseDuration = 2000L
  private var expectedSegment = 0 // To track expected segment for logging

  internal fun stopAnimation() {
    isAnimating = false
    removeCallbacks(animationRunnable)
  }

  private fun getSegmentAtPointer(rotation: Float): Int {
    val segmentAngle = 360f / segments.size
    val normalizedAngle = (270 - rotation) % 360
    return (normalizedAngle / segmentAngle).toInt()
  }

  private fun getSegmentName(index: Int): String {
    return segments[index].name
  }

  private fun determineTargetSegment(): Int {
    val randomValue = (0..999).random()
    Log.d(TAG, "Generated random value: $randomValue")

    for (i in probabilityRanges.indices) {
      if (randomValue in probabilityRanges[i]) {
        Log.d(TAG, "Random value $randomValue falls in range for segment: ${getSegmentName(i)}")
        return i
      }
    }
    return 0
  }

  init {
    // Log probability ranges for debugging
    probabilityRanges.forEachIndexed { index, range ->
      Log.d(TAG, "Probability range for ${getSegmentName(index)}: ${range.first}-${range.last}")
    }

    animationRunnable =
        object : Runnable {
          override fun run() {
            if (!isAnimating) return

            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastUpdateTime
            lastUpdateTime = currentTime

            if (isSpinning) {
              val progress = (currentTime - spinStartTime).toFloat() / spinDuration
              if (progress >= 1f) {
                rotation = targetRotation % 360
                isSpinning = false
                pauseAfterSpin = true
                pauseStartTime = currentTime

                // Log final landing segment
                val finalSegment = getSegmentAtPointer(rotation)
                Log.d(TAG, "Wheel stopped on segment: ${getSegmentName(finalSegment)}")
                Log.d(TAG, "Expected segment was: ${getSegmentName(expectedSegment)}")
                if (finalSegment != expectedSegment) {
                  Log.e(TAG, "ERROR: Wheel landed on wrong segment!")
                }
              } else {
                // Quartic ease-out for more gradual slowdown
                val easeOut =
                    1f - (1f - progress) * (1f - progress) * (1f - progress) * (1f - progress)
                rotation = currentRotation + (targetRotation - currentRotation) * easeOut
              }
            } else if (pauseAfterSpin) {
              if (currentTime - pauseStartTime >= pauseDuration) {
                pauseAfterSpin = false
              }
            } else {
              rotation += idleRotationSpeed * (deltaTime / 16.67f)
              if (rotation <= -360f) rotation += 360f
            }

            invalidate()
            if (isAnimating) {
              postDelayed(this, 16)
            }
          }
        }

    post(animationRunnable)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val centerX = width / 2f
    val centerY = height / 2f
    val radius = minOf(width, height) / 2f * 0.9f

    canvas.save()
    canvas.rotate(rotation, centerX, centerY)

    val segmentAngle = 360f / segments.size
    segments.forEachIndexed { index, segment ->
      paint.color = segment.color

      val startAngle = index * segmentAngle
      canvas.save()
      paint.style = Paint.Style.FILL
      canvas.drawArc(
          centerX - radius,
          centerY - radius,
          centerX + radius,
          centerY + radius,
          startAngle,
          segmentAngle,
          true,
          paint)
      canvas.restore()

      // Draw segment text
      paint.color = Color.WHITE
      paint.textSize = radius * 0.15f
      paint.textAlign = Paint.Align.CENTER

      val middleAngleRadians = Math.toRadians(startAngle + segmentAngle / 2.0)

      val textRadius = radius * 0.6f
      val textX = centerX + cos(middleAngleRadians).toFloat() * textRadius
      val textY = centerY + sin(middleAngleRadians).toFloat() * textRadius

      canvas.save()
      val textAngle = startAngle + segmentAngle / 2
      canvas.rotate(textAngle + 90, textX, textY)

      val bounds = Rect()
      paint.getTextBounds(segment.name, 0, segment.name.length, bounds)
      val textHeight = bounds.height()

      canvas.drawText(segment.name, textX, textY + textHeight / 2, paint)
      canvas.restore()
    }

    canvas.restore()

    // Draw the pointer
    paint.color = Color.RED
    paint.style = Paint.Style.FILL

    val pointerPath = Path()
    val pointerWidth = radius * 0.1f
    val pointerHeight = radius * 0.1f

    pointerPath.moveTo(centerX, centerY - radius + pointerHeight * 1.5f)
    pointerPath.lineTo(centerX - pointerWidth / 2, centerY - radius - pointerHeight / 2)
    pointerPath.lineTo(centerX + pointerWidth / 2, centerY - radius - pointerHeight / 2)
    pointerPath.close()

    canvas.drawPath(pointerPath, paint)
  }

  fun spin() {
    if (!isSpinning && !pauseAfterSpin) {
      val currentSegment = getSegmentAtPointer(rotation)
      Log.d(TAG, "Starting spin from segment: ${getSegmentName(currentSegment)}")

      val targetSegment = determineTargetSegment()
      expectedSegment = targetSegment
      Log.d(TAG, "Target segment is: ${getSegmentName(targetSegment)}")

      // Calculate required rotation to reach target segment center
      val segmentAngle = 360f / segments.size
      var requiredRotation = ((targetSegment - currentSegment) * segmentAngle)

      // Calculate where we'll land within the target segment
      val landingAngle = (270 - ((rotation - requiredRotation) % 360)) % 360
      val angleWithinSegment = landingAngle % segmentAngle

      val shouldTriggerNearMiss = Math.random() < 0.33f // 30% chance for near-miss
      var randomOffset = 0f

      when (targetSegment) {
        0 -> { // Nothing segment
          if (shouldTriggerNearMiss) {
            // For Nothing, randomly choose between getting close to Epic (left) or Legendary
            // (right)
            if (Math.random() < 0.6) {
              // Get close to Epic (left edge)
              val maxOffsetLeft = angleWithinSegment
              randomOffset = -maxOffsetLeft * (0.85f + (Math.random() * 0.13f).toFloat())
              Log.d(TAG, "Triggering near-miss on Nothing segment (close to Epic)")
            } else {
              // Get close to Legendary (right edge)
              val maxOffsetRight = segmentAngle - angleWithinSegment
              randomOffset = maxOffsetRight * (0.95f + (Math.random() * 0.03f).toFloat())
              Log.d(TAG, "Triggering near-miss on Nothing segment (close to Legendary)")
            }
          } else {
            // Normal random offset within safe bounds
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
            randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
          }
        }
        1 -> { // Legendary segment
          if (Math.random() < 0.8f) { // 80% chance for near win effect
            // Get very close to the edge but still within Legendary
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            // Use a very small offset to make it look like it almost didn't make it
            val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
            randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            Log.d(TAG, "Triggering near-win on Legendary segment")
          } else {
            // Land more clearly in the middle for a clear win
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
            randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
          }
        }
        2 -> { // Common segment
          if (shouldTriggerNearMiss) {
            // For Common, we want to get close to the left edge (near Legendary)
            val maxOffsetLeft = angleWithinSegment
            // Use only 2-20% of the distance to get very close to Legendary
            randomOffset = -maxOffsetLeft * (0.80f + (Math.random() * 0.18f).toFloat())
            Log.d(TAG, "Triggering near-miss on Common segment (close to Legendary)")
          } else {
            // Normal random offset within safe bounds
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
            randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
          }
        }
        3 -> { // Rare segment
          if (shouldTriggerNearMiss) {
            // For Rare, we want to get close to the right edge (near Epic)
            val maxOffsetRight = segmentAngle - angleWithinSegment
            // Use 85-98% of the distance to get very close to Epic
            randomOffset = maxOffsetRight * (0.80f + (Math.random() * 0.18f).toFloat())
            Log.d(TAG, "Triggering near-miss on Rare segment (close to Epic)")
          } else {
            // Normal random offset within safe bounds
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
            randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
          }
        }
        4 -> { // Epic segment
          if (Math.random() < 0.8f) { // 80% chance for near win effect
            // Get very close to the edge but still within Epic
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            // Use a very small offset to make it look like it almost didn't make it
            val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
            randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            Log.d(TAG, "Triggering near-win on Epic segment")
          } else {
            // Land more clearly in the middle for a clear win
            val maxOffsetLeft = angleWithinSegment
            val maxOffsetRight = segmentAngle - angleWithinSegment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
            randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
          }
        }
        else -> {
          // For other segments, use normal random offset within safe bounds
          val maxOffsetLeft = angleWithinSegment
          val maxOffsetRight = segmentAngle - angleWithinSegment
          val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.5f
          randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
        }
      }

      requiredRotation += randomOffset

      // Ensure we're always rotating clockwise
      if (requiredRotation <= 0) requiredRotation += 360

      // Add 6-7 full rotations for effect
      val fullRotations = (6..7).random() * 360
      targetRotation = rotation - (requiredRotation + fullRotations)

      Log.d(TAG, "Landing angle within segment: $angleWithinSegment")
      Log.d(TAG, "Total rotation with full spins: ${requiredRotation + fullRotations} degrees")
      Log.d(TAG, "Near-miss triggered: $shouldTriggerNearMiss")

      currentRotation = rotation
      spinStartTime = System.currentTimeMillis()
      spinDuration = 10000 // 10 seconds spin
      isSpinning = true
    }
  }
}

@Composable
fun GamblingScreen(onWheelViewCreated: ((WheelView) -> Unit)? = null) {
  var wheelView by remember { mutableStateOf<WheelView?>(null) }

  Box(
      modifier = Modifier.fillMaxSize().testTag("gambling_screen"),
      contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.size(300.dp).testTag("wheel_view"),
            factory = { context ->
              WheelView(context).also {
                wheelView = it
                onWheelViewCreated?.invoke(it)
              }
            })

        Button(
            onClick = { wheelView?.spin() },
            modifier = Modifier.size(90.dp).clip(CircleShape).testTag("spin_button"),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Red)) {
              Text(
                  text = stringResource(R.string.spin_button_text),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.wrapContentSize())
            }
      }
}
