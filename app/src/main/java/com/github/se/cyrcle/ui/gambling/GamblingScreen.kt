package com.github.se.cyrcle.ui.gambling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.github.se.cyrcle.model.user.SPIN_COST
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val textProportion = 0.65f

/**
 * A sophisticated wheel-based gambling interface implementing complex probability and animation
 * systems.
 *
 * Mathematical Components:
 * 1. Probability System:
 *     - Uses a 1000-point scale (0-999) for precise probability distribution
 *     - Segments mapped to probability ranges:
 *         * Nothing: 500 points (50%)
 *         * Legendary: 1 point (0.1%)
 *         * Common: 320 points (32%)
 *         * Rare: 160 points (16%)
 *         * Epic: 19 points (1.9%)
 * 2. Rotation Mathematics:
 *     - Base rotation system: Uses 360-degree circular motion
 *     - Idle animation: -0.2° continuous decrements
 *     - Pointer position fixed at 270° (top of wheel)
 *     - Segment calculation: normalizedAngle = (270 - rotation) % 360
 * 3. Near-Miss System:
 *     - Implements psychological near-miss effects with segment-specific behaviors:
 *     - Nothing (0):
 *         * 33% chance of near-miss
 *         * Left bias: 85-98% of segment width
 *         * Right bias: 95-98% of segment width
 *     - Legendary (1):
 *         * 80% chance of near-perfect landing
 *         * Tiny offset: ±5% of segment width
 *     - Common (2):
 *         * 33% chance of left-side near-miss
 *         * Offset: 80-98% of segment width
 *     - Rare (3):
 *         * 33% chance of right-side near-miss
 *         * Offset: 80-98% of segment width
 *     - Epic (4):
 *         * Similar to Legendary behavior
 * 4. Spinning Animation:
 *     - Duration: 10 seconds (10000ms)
 *     - Base rotations: Random 6-10 full turns (2160°-3600°)
 *     - Easing function: Quartic ease-out
 *         * Formula: rotation = 1 - (1-t)⁴
 *         * Provides realistic deceleration
 *     - Frame rate: ~60fps (16ms delay)
 * 5. Text Positioning:
 *     - Radial text placement using polar coordinates
 *     - Text radius: 65% of wheel radius (textProportion = 0.65f)
 *     - Text rotation: Compensates for segment angle + 90° for readability
 *     - Dynamic scaling: Font size = 6% of wheel radius
 *
 * @param modifier Modifier for the composable
 * @param onSegmentLanded Callback triggered when wheel stops on a segment
 * @return A function that triggers the wheel spin
 */
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
    for (i in probabilityRanges.indices) {
      if (randomValue in probabilityRanges[i]) {
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

        val segmentAngle = 360f / segments.size
        var requiredRotation = ((targetSegment - currentSegment) * segmentAngle)
        val landingAngle = (270 - rotation + requiredRotation) % 360
        val angleWithinSegment = landingAngle % segmentAngle
        val shouldTriggerNearMiss = Math.random() < 0.33f
        var randomOffset = 0f

        when (targetSegment) {
          0 -> {
            if (shouldTriggerNearMiss) {
              if (Math.random() < 0.6) {
                randomOffset = -angleWithinSegment * (0.85f + (Math.random() * 0.13f).toFloat())
              } else {
                val maxOffsetRight = segmentAngle - angleWithinSegment
                randomOffset = maxOffsetRight * (0.95f + (Math.random() * 0.03f).toFloat())
              }
            }
          }
          1 -> {
            if (Math.random() < 0.8f) {
              val maxOffsetRight = segmentAngle - angleWithinSegment
              val nearWinOffset = minOf(angleWithinSegment, maxOffsetRight) * 0.05f
              randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            }
          }
          2 -> {
            if (shouldTriggerNearMiss) {
              randomOffset = -angleWithinSegment * (0.80f + (Math.random() * 0.18f).toFloat())
            }
          }
          3 -> {
            if (shouldTriggerNearMiss) {
              val maxOffsetRight = segmentAngle - angleWithinSegment
              randomOffset = maxOffsetRight * (0.80f + (Math.random() * 0.18f).toFloat())
            }
          }
          4 -> {
            if (Math.random() < 0.8f) {
              val maxOffsetRight = segmentAngle - angleWithinSegment
              val nearWinOffset = minOf(angleWithinSegment, maxOffsetRight) * 0.05f
              randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
            }
          }
        }

        requiredRotation += randomOffset
        if (requiredRotation <= 0) requiredRotation += 360
        val fullRotations = (6..10).random() * 360
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
          onSegmentLanded?.invoke(segments[getSegmentAtPointer(rotation)].name)
          pauseAfterSpin = false
        }
      }
    }
  }

  Box(modifier = modifier.testTag("WheelViewContainer")) {
    Canvas(modifier = Modifier.testTag("WheelCanvas").aspectRatio(1f)) {
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
          val textRadius = radius * textProportion
          val textX = centerX + cos(middleAngle).toFloat() * textRadius
          val textY = centerY + sin(middleAngle).toFloat() * textRadius
          val textLayoutResult =
              textMeasurer.measure(
                  text = segment.name,
                  style =
                      TextStyle(
                          color = Color.White,
                          fontSize = (radius * 0.06f).sp,
                          textAlign = TextAlign.Center))

          rotate(startAngle + segmentAngle / 2 + 90, Offset(textX, textY)) {
            drawText(
                textMeasurer = textMeasurer,
                text = segment.name,
                topLeft =
                    Offset(
                        textX - textLayoutResult.size.width / 2f, // Center horizontally
                        textY - textLayoutResult.size.height / 2f // Center vertically
                        ),
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
  }

  Box(
      modifier =
          Modifier.testTag("WheelSpinState").semantics {
            contentDescription = "Spinning: $isSpinning, Paused: $pauseAfterSpin"
          })

  return spinFunction
}

/**
 * Main gambling interface that manages user progression, animations, and wheel interactions.
 *
 * Level System Mathematics:
 * 1. Experience Points (XP):
 *     - Base XP rewards:
 *         * Legendary: 10.0 flat XP
 *         * Common: 1.0 / sqrt(level + 1)
 *         * Rare: 2.0 / sqrt(level + 1)
 *         * Epic: 16.0 / sqrt(level + 1)
 *     - Normalization factor: sqrt(currentLevel + 1)
 *         * Ensures XP gains decrease as level increases
 *         * Creates logarithmic progression curve
 * 2. Level Progress Animation:
 *     - Uses FastOutSlowInEasing for smooth transitions
 *     - Multi-level progression:
 *         * Fills bar to 100% for each level gained
 *         * Resets to 0% and increments level
 *     - Animation duration: 500ms per transition
 *     - XP popup duration: 2000ms
 *
 * @param navigationActions Navigation handler for the screen
 * @param userViewModel ViewModel managing user state and progression
 */
@Composable
fun GamblingScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val rarityLegendary = stringResource(R.string.rarity_legendary)
  val rarityCommon = stringResource(R.string.rarity_common)
  val rarityRare = stringResource(R.string.rarity_rare)
  val rarityEpic = stringResource(R.string.rarity_epic)

  val userState by userViewModel.currentUser.collectAsState()
  var coins by remember { mutableStateOf(userState?.details?.wallet?.getCoins()) }
  val currentLevel = userState?.public?.userReputationScore ?: 0.0
  val flooredLevel = currentLevel.toInt()
  val targetProgress = (currentLevel - flooredLevel).toFloat()

  var displayedLevel by remember { mutableIntStateOf(flooredLevel) }
  val animatedProgress = remember { Animatable(targetProgress) }

  var xpIncrement by remember { mutableDoubleStateOf(0.0) }
  var showXpIncrement by remember { mutableStateOf(false) }
  var rarityText by remember { mutableStateOf("") }
  var isSpinning by remember { mutableStateOf(false) }

  LaunchedEffect(currentLevel) {
    val startLevel = displayedLevel

    if (flooredLevel > startLevel) {
      for (level in startLevel until flooredLevel) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
        displayedLevel = level + 1
        animatedProgress.snapTo(0f)
      }
    }

    animatedProgress.animateTo(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
    displayedLevel = flooredLevel
  }

  LaunchedEffect(xpIncrement) {
    if (xpIncrement > 0) {
      showXpIncrement = true
      delay(2000)
      showXpIncrement = false
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            navigationActions = navigationActions,
            title = stringResource(R.string.gambling_screen_title),
            testTag = "GamblingScreenTopBar")
      }) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("GamblingScreen"),
            contentAlignment = Alignment.Center) {
              var wheelSpinFunction by remember { mutableStateOf<(() -> Unit)?>(null) }

              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.align(Alignment.TopCenter)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                          Text(
                              text = stringResource(R.string.level_display, displayedLevel),
                              style = MaterialTheme.typography.bodyMedium,
                              modifier = Modifier.testTag("CurrentLevelText"))
                          Text(
                              text = stringResource(R.string.level_display, (displayedLevel + 1)),
                              style = MaterialTheme.typography.bodyMedium,
                              modifier = Modifier.testTag("NextLevelText"))
                        }

                    LinearProgressIndicator(
                        progress = { animatedProgress.value },
                        modifier =
                            Modifier.fillMaxWidth(0.8f).height(8.dp).testTag("LevelProgressBar"),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = displayedLevel < flooredLevel,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()) {
                          Text(
                              text = stringResource(R.string.level_up),
                              color = MaterialTheme.colorScheme.primary,
                              style = MaterialTheme.typography.headlineSmall,
                              modifier = Modifier.padding(vertical = 8.dp).testTag("LevelUpText"))
                        }

                    Text(
                        text = stringResource(R.string.gambling_screen_coins_display, coins ?: 0),
                        modifier = Modifier.testTag("CoinDisplay"),
                        style = MaterialTheme.typography.headlineMedium)

                    AnimatedVisibility(
                        visible = showXpIncrement && (xpIncrement > 0),
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 500))) {
                          Text(
                              text = stringResource(R.string.xp_increment, rarityText, xpIncrement),
                              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Green),
                              modifier =
                                  Modifier.align(Alignment.CenterHorizontally)
                                      .padding(top = 16.dp)
                                      .testTag("xpIncrementText"))
                        }
                  }

              WheelView(
                      modifier = Modifier.size(300.dp).testTag("WheelView"),
                      onSegmentLanded = { segmentName ->
                        val normalizationFactor = sqrt(currentLevel + 1.0)
                        val reputationIncrement =
                            when (segmentName) {
                              rarityLegendary -> 10.0
                              rarityCommon -> 1.0 / normalizationFactor
                              rarityRare -> 2.0 / normalizationFactor
                              rarityEpic -> 16.0 / normalizationFactor
                              else -> 0.0
                            }

                        val updatedUser =
                            userState?.copy(
                                public =
                                    userState!!
                                        .public
                                        .copy(
                                            userReputationScore =
                                                userState!!.public.userReputationScore +
                                                    reputationIncrement))
                        if (updatedUser != null) {
                          userViewModel.updateUser(user = updatedUser)
                        }

                        xpIncrement = reputationIncrement
                        rarityText = segmentName
                        isSpinning = false
                      })
                  .let { spinFn -> wheelSpinFunction = spinFn }

              val canSpin =
                  remember(userState, isSpinning) {
                    userState?.details?.wallet?.isSolvable(SPIN_COST, 0) == true &&
                        !isSpinning &&
                        wheelSpinFunction != null
                  }

              Button(
                  onClick = {
                    userViewModel.tryDebitCoinsFromCurrentUser(
                        SPIN_COST,
                        0,
                        onSuccess = {
                          coins = userState?.details?.wallet?.getCoins()
                          isSpinning = true
                          wheelSpinFunction?.invoke()
                        })
                  },
                  enabled = canSpin,
                  modifier = Modifier.size(90.dp).clip(CircleShape).testTag("SpinButton"),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Color.Red, disabledContainerColor = Color.Gray)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentSize()) {
                          Text(
                              text = stringResource(R.string.spin_button_text),
                              fontSize = 16.sp,
                              fontWeight = FontWeight.Bold,
                              textAlign = TextAlign.Center,
                              modifier = Modifier.testTag("SpinButtonText"))
                          Text(
                              text =
                                  stringResource(
                                      R.string.gambling_screen_spin_button_cost, SPIN_COST),
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Medium,
                              textAlign = TextAlign.Center,
                              modifier = Modifier.testTag("SpinCostText"))
                        }
                  }
            }
      }
}
