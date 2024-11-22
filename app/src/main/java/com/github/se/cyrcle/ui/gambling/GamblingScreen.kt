package com.github.se.cyrcle.ui.gambling

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.cyrcle.R
import kotlin.math.*
import kotlinx.coroutines.*

@Composable
fun WheelView(
    modifier: Modifier = Modifier,
    onSegmentLanded: ((String) -> Unit)? = null
): () -> Unit {
  data class Segment(val name: String, val color: Color, val probability: Float)

  val rarityNothing = stringResource(R.string.rarity_nothing)
  val rarityLegendary = stringResource(R.string.rarity_legendary)
  val rarityCommon = stringResource(R.string.rarity_common)
  val rarityRare = stringResource(R.string.rarity_rare)
  val rarityEpic = stringResource(R.string.rarity_epic)

  val segments = remember {
    listOf(
        Segment(rarityNothing, Color.Gray, 50f),
        Segment(rarityLegendary, Color(0xFFFFD700), 0.1f),
        Segment(rarityCommon, Color.Green, 32f),
        Segment(rarityRare, Color.Blue, 16f),
        Segment(rarityEpic, Color(0xFF800080), 1.9f))
  }

  val probabilityRanges = remember {
    var currentValue = 0
    segments.map { segment ->
      val range = (segment.probability * 10).toInt()
      val start = currentValue
      currentValue += range
      start until currentValue
    }
  }

  var rotation by remember { mutableFloatStateOf(0f) }
  var isSpinning by remember { mutableStateOf(false) }
  var targetRotation by remember { mutableFloatStateOf(0f) }
  var currentRotation by remember { mutableFloatStateOf(0f) }
  var spinStartTime by remember { mutableLongStateOf(0L) }
  var pauseAfterSpin by remember { mutableStateOf(false) }
  var expectedSegment by remember { mutableIntStateOf(0) }
  val textMeasurer = rememberTextMeasurer()

  fun getSegmentAtPointer(rotation: Float): Int {
    val segmentAngle = 360f / segments.size
    val normalizedAngle = (270 - rotation) % 360
    return (normalizedAngle / segmentAngle).toInt()
  }

  fun determineTargetSegment(): Int {
    val randomValue = (0..999).random()
    Log.d("FAVORITE", "Random value generated: $randomValue")
    for (i in probabilityRanges.indices) {
      if (randomValue in probabilityRanges[i]) {
        Log.d("FAVORITE", "Selected segment $i (${segments[i].name})")
        return i
      }
    }
    return 0
  }

  LaunchedEffect(Unit) {
    while (true) {
      if (!isSpinning && !pauseAfterSpin) {
        rotation -= 0.2f
        if (rotation <= -360f) rotation += 360f
      }
      delay(16)
    }
  }

  val spinFunction = remember {
    {
      if (!isSpinning && !pauseAfterSpin) {
        val currentSegment = getSegmentAtPointer(rotation)
        val targetSegment = determineTargetSegment()
        expectedSegment = targetSegment

        Log.d(
            "FAVORITE",
            "Starting spin from segment $currentSegment (${segments[currentSegment].name})")
        Log.d("FAVORITE", "Target segment is $targetSegment (${segments[targetSegment].name})")

        val segmentAngle = 360f / segments.size
        var requiredRotation = ((targetSegment - currentSegment) * segmentAngle)
        val landingAngle = (270 - ((rotation - requiredRotation) % 360)) % 360
        val angleWithinSegment = landingAngle % segmentAngle
        val shouldTriggerNearMiss = Math.random() < 0.33f
        var randomOffset = 0f

        when (targetSegment) {
          0 -> {
            if (shouldTriggerNearMiss) {
              Log.d("FAVORITE", "Triggering near miss for Nothing segment")
              if (Math.random() < 0.6) {
                val maxOffsetLeft = angleWithinSegment
                randomOffset = -maxOffsetLeft * (0.85f + (Math.random() * 0.13f).toFloat())
                Log.d("FAVORITE", "Near miss to the left")
              } else {
                val maxOffsetRight = segmentAngle - angleWithinSegment
                randomOffset = maxOffsetRight * (0.95f + (Math.random() * 0.03f).toFloat())
                Log.d("FAVORITE", "Near miss to the right")
              }
            }
          }
          1 -> {
            if (Math.random() < 0.8f) {
              Log.d("FAVORITE", "Triggering near win for Legendary")
              val maxOffsetLeft = angleWithinSegment
              val maxOffsetRight = segmentAngle - angleWithinSegment
              val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
              randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            }
          }
          2 -> {
            if (shouldTriggerNearMiss) {
              Log.d("FAVORITE", "Triggering near miss for Common")
              val maxOffsetLeft = angleWithinSegment
              randomOffset = -maxOffsetLeft * (0.80f + (Math.random() * 0.18f).toFloat())
            }
          }
          3 -> {
            if (shouldTriggerNearMiss) {
              Log.d("FAVORITE", "Triggering near miss for Rare")
              val maxOffsetRight = segmentAngle - angleWithinSegment
              randomOffset = maxOffsetRight * (0.80f + (Math.random() * 0.18f).toFloat())
            }
          }
          4 -> {
            if (Math.random() < 0.8f) {
              Log.d("FAVORITE", "Triggering near win for Epic")
              val maxOffsetLeft = angleWithinSegment
              val maxOffsetRight = segmentAngle - angleWithinSegment
              val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
              randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            }
          }
        }

        requiredRotation += randomOffset
        if (requiredRotation <= 0) requiredRotation += 360
        val fullRotations = (6..7).random() * 360
        targetRotation = rotation - (requiredRotation + fullRotations)
        currentRotation = rotation
        spinStartTime = System.currentTimeMillis()
        isSpinning = true

        CoroutineScope(Dispatchers.Default).launch {
          val spinDuration = 10000L
          var progress = 0f
          val startTime = System.currentTimeMillis()

          while (progress < 1f) {
            progress = (System.currentTimeMillis() - startTime).toFloat() / spinDuration
            val easeOut = 1f - (1f - progress) * (1f - progress) * (1f - progress) * (1f - progress)
            rotation = currentRotation + (targetRotation - currentRotation) * easeOut
            delay(16)
          }

          rotation = targetRotation % 360
          isSpinning = false
          pauseAfterSpin = true
          delay(2000)
          pauseAfterSpin = false

          onSegmentLanded?.invoke(segments[getSegmentAtPointer(rotation)].name)
          Log.d(
              "FAVORITE",
              "Spin finished on segment ${getSegmentAtPointer(rotation)} (${segments[getSegmentAtPointer(rotation)].name})")
        }
      }
    }
  }

  Box(modifier = modifier.testTag("wheel_view_container")) {
    Canvas(modifier = Modifier.testTag("wheel_canvas").aspectRatio(1f)) {
      val centerX = size.width / 2f
      val centerY = size.height / 2f
      val radius = minOf(size.width, size.height) / 2f * 0.9f

      rotate(rotation, Offset(centerX, centerY)) {
        val segmentAngle = 360f / segments.size
        segments.forEachIndexed { index, segment ->
          drawArc(
              color = segment.color,
              startAngle = index * segmentAngle,
              sweepAngle = segmentAngle,
              useCenter = true,
              topLeft = Offset(centerX - radius, centerY - radius),
              size = Size(radius * 2, radius * 2))

          val startAngle = index * segmentAngle
          val middleAngle = Math.toRadians((startAngle + segmentAngle / 2.0))
          val textRadius = radius * 0.6f
          val textX = centerX + cos(middleAngle).toFloat() * textRadius
          val textY = centerY + sin(middleAngle).toFloat() * textRadius

          rotate(startAngle + segmentAngle / 2 + 90, Offset(textX, textY)) {
            drawText(
                textMeasurer = textMeasurer,
                text = segment.name,
                topLeft = Offset(textX - 25f, textY - 10f),
                style =
                    TextStyle(
                        color = Color.White,
                        fontSize = (radius * 0.06f).sp,
                        textAlign = TextAlign.Center))
          }
        }
      }

      drawPath(
          path =
              Path().apply {
                val pointerWidth = radius * 0.1f
                val pointerHeight = radius * 0.1f
                moveTo(centerX, centerY - radius + pointerHeight * 1.5f)
                lineTo(centerX - pointerWidth / 2, centerY - radius - pointerHeight / 2)
                lineTo(centerX + pointerWidth / 2, centerY - radius - pointerHeight / 2)
                close()
              },
          color = Color.Red)
    }

    segments.forEachIndexed { index, segment ->
      Box(
          modifier =
              Modifier.testTag("segment_$index").semantics {
                contentDescription = "Wheel segment ${segment.name}"
              })
      Box(
          modifier =
              Modifier.testTag("segment_text_$index").semantics {
                contentDescription = "Segment text ${segment.name}"
              })
    }

    Box(
        modifier =
            Modifier.testTag("wheel_pointer").semantics { contentDescription = "Wheel pointer" })

    Box(
        modifier =
            Modifier.testTag("wheel_rotation").semantics {
              contentDescription = "Current rotation: $rotation"
            })

    Box(
        modifier =
            Modifier.testTag("wheel_spin_state").semantics {
              contentDescription = "Spinning: $isSpinning, Paused: $pauseAfterSpin"
            })
  }

  return spinFunction
}

@Composable
fun GamblingScreen() {
  Box(
      modifier = Modifier.fillMaxSize().testTag("gambling_screen"),
      contentAlignment = Alignment.Center) {
        var wheelSpinFunction by remember { mutableStateOf<(() -> Unit)?>(null) }

        WheelView(
                modifier = Modifier.size(300.dp).testTag("wheel_view"),
                onSegmentLanded = { /* Handle landing */})
            .let { spinFn -> wheelSpinFunction = spinFn }

        Button(
            onClick = { wheelSpinFunction?.invoke() },
            modifier = Modifier.size(90.dp).clip(CircleShape).testTag("spin_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
              Text(
                  text = stringResource(R.string.spin_button_text),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.wrapContentSize().testTag("spin_button_text"))
            }
      }
}
