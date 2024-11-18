package com.github.se.cyrcle.ui.gambling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import com.github.se.cyrcle.model.user.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class GamblingScreen {

    private object WheelColors {
        val Nothing = Color.Gray
        val Common = Color.Green
        val Rare = Color(0xFF0066FF)
        val Epic = Color(0xFF800080)
        val Legendary = Color(0xFFFFD700)
    }

    private enum class WheelSegment(val displayName: String) {
        NOTHING("Nothing"),
        COMMON("Common Prize!"),
        RARE("Rare Prize!!"),
        EPIC("Epic Prize!!!"),
        LEGENDARY("LEGENDARY!!!!")
    }

    private fun generateNewPattern(): List<WheelSegment> {
        return buildList {
            repeat(65) { add(WheelSegment.NOTHING) }
            repeat(20) { add(WheelSegment.COMMON) }
            repeat(10) { add(WheelSegment.RARE) }
            repeat(4) { add(WheelSegment.EPIC) }
            repeat(1) { add(WheelSegment.LEGENDARY) }
        }.shuffled()
    }

    companion object {
        private const val BASE_SPIN_SPEED = 0.1f
        private const val MAX_SPIN_SPEED = 20f
        private const val MIN_SPIN_SPEED = 0.1f
        private const val SPIN_SLOWDOWN_FACTOR = 0.99f
        private const val PAUSE_DURATION = 4000L
        const val SPIN_COST = 100
    }

    private fun calculatePrize(rotationAngle: Float, pattern: List<WheelSegment>): WheelSegment {
        val segmentAngle = 360f / pattern.size
        val normalizedAngle = (270 - (rotationAngle % 360)) % 360
        val segmentIndex = (normalizedAngle / segmentAngle).toInt()
        return pattern[segmentIndex]
    }

    private fun deductSpinCost(userViewModel: UserViewModel): Boolean {
        userViewModel.currentUser.value?.let { currentUser ->
            val wallet = currentUser.details?.wallet ?: return false
            if (!wallet.isSolvable(SPIN_COST, 0)) return false
            wallet.debitCoins(SPIN_COST)
            val updatedDetails = currentUser.details.copy(wallet = wallet)
            val updatedUser = currentUser.copy(details = updatedDetails)
            userViewModel.updateUser(updatedUser)
            userViewModel.setCurrentUser(updatedUser)
            return true
        }
        return false
    }

    @Composable
    private fun PrizeText(prize: WheelSegment) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (prize == WheelSegment.LEGENDARY) 1.2f else 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            )
        )

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically()
        ) {
            Text(
                text = prize.displayName,
                modifier = Modifier
                    .scale(scale)
                    .padding(top = 32.dp),
                fontSize = when(prize) {
                    WheelSegment.LEGENDARY -> 40.sp
                    WheelSegment.EPIC -> 33.sp
                    else -> 22.sp
                },
                textAlign = TextAlign.Center,
                color = when(prize) {
                    WheelSegment.NOTHING -> Color.Gray
                    WheelSegment.COMMON -> Color.Green
                    WheelSegment.RARE -> Color(0xFF0066FF)
                    WheelSegment.EPIC -> Color(0xFF800080)
                    WheelSegment.LEGENDARY -> Color(0xFFFFD700)
                }
            )
        }
    }

    @Composable
    fun GamblingScreenContent(
        userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    ) {
        var rotationAngle by remember { mutableStateOf(0f) }
        var isSpinning by remember { mutableStateOf(false) }
        var isPaused by remember { mutableStateOf(false) }
        var spinSpeed by remember { mutableStateOf(BASE_SPIN_SPEED) }
        var currentPrize by remember { mutableStateOf<WheelSegment?>(null) }
        var currentPattern by remember { mutableStateOf(generateNewPattern()) }
        var showInsufficientFunds by remember { mutableStateOf(false) }

        val currentUser by userViewModel.currentUser.collectAsState()
        val currentBalance = currentUser?.details?.wallet?.getCoins() ?: 0

        LaunchedEffect(Unit) {
            while(true) {
                delay(16)
                when {
                    isSpinning -> {
                        rotationAngle = (rotationAngle + spinSpeed) % 360f
                        spinSpeed *= SPIN_SLOWDOWN_FACTOR

                        if(spinSpeed <= MIN_SPIN_SPEED) {
                            isSpinning = false
                            isPaused = true
                            spinSpeed = 0f
                            currentPrize = calculatePrize(rotationAngle, currentPattern)
                            delay(PAUSE_DURATION)
                            isPaused = false
                            spinSpeed = BASE_SPIN_SPEED
                            currentPattern = generateNewPattern()
                        }
                    }
                    isPaused -> {}
                    else -> {
                        currentPrize = null
                        rotationAngle = (rotationAngle + BASE_SPIN_SPEED) % 360f
                    }
                }
            }
        }

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Balance: $currentBalance coins",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    color = Color.White
                )

                PrizeWheel(
                    rotationAngle = rotationAngle,
                    segmentPattern = currentPattern
                )

                currentPrize?.let { prize ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PrizeText(prize)
                    }
                }

                if (showInsufficientFunds) {
                    Text(
                        text = "Insufficient funds!",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 100.dp)
                    )
                }

                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    enabled = !isSpinning && !isPaused,
                    onClick = {
                        if (deductSpinCost(userViewModel)) {
                            isSpinning = true
                            spinSpeed = MAX_SPIN_SPEED
                            currentPrize = null
                            showInsufficientFunds = false
                        } else {
                            showInsufficientFunds = true
                        }
                    }
                ) {
                    Text(
                        when {
                            isSpinning -> "Spinning..."
                            isPaused -> currentPrize?.displayName ?: "Getting Result..."
                            else -> "SPIN! ($SPIN_COST coins)"
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun PrizeWheel(
        modifier: Modifier = Modifier,
        rotationAngle: Float,
        segmentPattern: List<WheelSegment>
    ) {
        Canvas(
            modifier = Modifier.size(300.dp)
        ) {
            val center = size.width / 2
            val radius = size.width / 2
            val segmentAngle = 2f * PI.toFloat() / segmentPattern.size

            rotate(degrees = rotationAngle) {
                segmentPattern.forEachIndexed { index, segment ->
                    val startAngle = index * segmentAngle
                    val path = Path().apply {
                        moveTo(center, center)
                        val rect = Rect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height
                        )
                        arcTo(
                            rect = rect,
                            startAngleDegrees = Math.toDegrees(startAngle.toDouble()).toFloat(),
                            sweepAngleDegrees = Math.toDegrees(segmentAngle.toDouble()).toFloat(),
                            forceMoveTo = false
                        )
                        lineTo(center, center)
                        close()
                    }

                    val color = when(segment) {
                        WheelSegment.NOTHING -> WheelColors.Nothing
                        WheelSegment.COMMON -> WheelColors.Common
                        WheelSegment.RARE -> WheelColors.Rare
                        WheelSegment.EPIC -> WheelColors.Epic
                        WheelSegment.LEGENDARY -> WheelColors.Legendary
                    }

                    drawPath(path = path, color = color)
                    drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
                }

                drawCircle(color = Color.White, radius = radius, style = Stroke(width = 4f))
                drawCircle(color = Color.White, radius = radius * 0.1f)
            }

            drawLine(
                color = Color.Black,
                start = Offset(center, center),
                end = Offset(center, center - radius + 10f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.Black,
                radius = 8f,
                center = Offset(center, center)
            )
        }
    }
}