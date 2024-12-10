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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.capitalize
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
                                val maxOffsetLeft = angleWithinSegment
                                randomOffset = -maxOffsetLeft * (0.85f + (Math.random() * 0.13f).toFloat())
                            } else {
                                val maxOffsetRight = segmentAngle - angleWithinSegment
                                randomOffset = maxOffsetRight * (0.95f + (Math.random() * 0.03f).toFloat())
                            }
                        }
                    }
                    1 -> {
                        if (Math.random() < 0.8f) {
                            val maxOffsetLeft = angleWithinSegment
                            val maxOffsetRight = segmentAngle - angleWithinSegment
                            val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
                            randomOffset = (Math.random() * 2 * nearWinOffset - nearWinOffset).toFloat()
                        }
                    }
                    2 -> {
                        if (shouldTriggerNearMiss) {
                            val maxOffsetLeft = angleWithinSegment
                            randomOffset = -maxOffsetLeft * (0.80f + (Math.random() * 0.18f).toFloat())
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
                            val maxOffsetLeft = angleWithinSegment
                            val maxOffsetRight = segmentAngle - angleWithinSegment
                            val nearWinOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.05f
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
                    delay(2000)

                    withContext(Dispatchers.Main) {
                        pauseAfterSpin = false
                        delay(16)
                    }
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
                    val textRadius = radius * 0.65f
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
                                textX - textLayoutResult.size.width / 2f,
                                textY - textLayoutResult.size.height / 2f
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
        Modifier.testTag("wheel_spin_state").semantics {
            contentDescription = "Spinning: $isSpinning, Paused: $pauseAfterSpin"
        })

    return spinFunction
}

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

    var xpIncrement by remember { mutableStateOf(0.0) }
    var showXpIncrement by remember { mutableStateOf(false) }
    var rarityText by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(currentLevel) {
        val startLevel = displayedLevel
        val endLevel = flooredLevel

        if (endLevel > startLevel) {
            for (level in startLevel until endLevel) {
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
                displayedLevel = level + 1
                animatedProgress.snapTo(0f)
            }
        }

        animatedProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
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
                testTag = "gambling_screen_top_bar"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("gambling_screen"),
            contentAlignment = Alignment.Center
        ) {
            var wheelSpinFunction by remember { mutableStateOf<(() -> Unit)?>(null) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level ${displayedLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("current_level_text")
                    )
                    Text(
                        text = "Level ${displayedLevel + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("next_level_text")
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .testTag("level_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = displayedLevel < flooredLevel,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = "Level Up!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.gambling_screen_coins_display, coins ?: 0),
                    modifier = Modifier.testTag("coin_display"),
                    style = MaterialTheme.typography.headlineMedium
                )

                AnimatedVisibility(
                    visible = showXpIncrement && (xpIncrement > 0),
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 500))
                ) {
                    Text(
                        text = "${rarityText}: +%.2f XP!".format(xpIncrement),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Green),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                            .testTag("xp_increment_text")
                    )
                }
            }

            WheelView(
                modifier = Modifier.size(300.dp).testTag("wheel_view"),
                onSegmentLanded = { segmentName ->
                    val reputationIncrement = when (segmentName) {
                        rarityLegendary -> 10.0
                        rarityCommon -> 1.0 / sqrt(currentLevel + 1.0)
                        rarityRare -> 2.0 / sqrt(currentLevel + 1.0)
                        rarityEpic -> 16.0 / sqrt(currentLevel + 1.0)
                        else -> 0.0
                    }

                    val updatedUser = userState?.copy(
                        public = userState!!.public.copy(
                            userReputationScore = userState!!.public.userReputationScore + reputationIncrement
                        )
                    )
                    if (updatedUser != null) {
                        userViewModel.updateUser(user = updatedUser)
                    }

                    xpIncrement = reputationIncrement
                    rarityText = segmentName
                    isSpinning = false
                }
            ).let { spinFn -> wheelSpinFunction = spinFn }

            val canSpin = remember(userState, isSpinning) {
                userState?.details?.wallet?.isSolvable(SPIN_COST, 0) == true &&
                        !isSpinning &&
                        wheelSpinFunction != null
            }

            Button(
                onClick = {
                    if (canSpin) {
                        userViewModel.tryDebitCoinsFromCurrentUser(
                            SPIN_COST,
                            0,
                            onSuccess = {
                                coins = userState?.details?.wallet?.getCoins()
                                isSpinning = true
                                wheelSpinFunction?.invoke()
                            }
                        )
                    }
                },
                enabled = canSpin,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .testTag("spin_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = stringResource(R.string.spin_button_text),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("spin_button_text")
                    )
                    Text(
                        text = stringResource(
                            R.string.gambling_screen_spin_button_cost, SPIN_COST
                        ),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("spin_cost_text")
                    )
                }
            }
        }
    }
}



