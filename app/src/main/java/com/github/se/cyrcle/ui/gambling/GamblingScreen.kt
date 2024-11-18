package com.github.se.cyrcle.ui.gambling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI

class GamblingScreen {

    private object WheelColors {
        val Nothing = Color.Gray
        val Common = Color.Green
        val Rare = Color(0xFF0066FF)    // Blue
        val Epic = Color(0xFF800080)     // Purple
        val Legendary = Color(0xFFFFD700) // Gold
    }

    private enum class WheelSegment {
        NOTHING,
        COMMON,
        RARE,
        EPIC,
        LEGENDARY
    }

    private val segmentPattern = listOf(
        WheelSegment.NOTHING,
        WheelSegment.COMMON,
        WheelSegment.NOTHING,
        WheelSegment.RARE,
        WheelSegment.NOTHING,
        WheelSegment.COMMON,
        WheelSegment.NOTHING,
        WheelSegment.EPIC,
        WheelSegment.NOTHING,
        WheelSegment.COMMON,
        WheelSegment.NOTHING,
        WheelSegment.RARE,
        WheelSegment.NOTHING,
        WheelSegment.LEGENDARY,
        WheelSegment.NOTHING,
        WheelSegment.COMMON,
    )

    companion object {
        private const val BASE_SPIN_SPEED = 0.1f     // Slow constant spin speed
        private const val MAX_SPIN_SPEED = 20f       // Maximum speed when spinning
        private const val MIN_SPIN_SPEED = 0.1f      // Minimum speed (same as BASE_SPIN_SPEED)
        private const val SPIN_SLOWDOWN_FACTOR = 0.99f // How quickly the spin slows down
        private const val PAUSE_DURATION = 4000L      // 4 seconds pause after spinning
    }

    @Composable
    fun GamblingScreenContent() {
        var rotationAngle by remember { mutableStateOf(0f) }
        var isSpinning by remember { mutableStateOf(false) }
        var isPaused by remember { mutableStateOf(false) }
        var spinSpeed by remember { mutableStateOf(BASE_SPIN_SPEED) }

        // Combined continuous rotation animation
        LaunchedEffect(Unit) {
            while(true) {
                delay(16) // roughly 60 FPS

                when {
                    isSpinning -> {
                        // Fast spin with slowdown
                        rotationAngle = (rotationAngle + spinSpeed) % 360f
                        spinSpeed *= SPIN_SLOWDOWN_FACTOR

                        // Check if spin is finished
                        if(spinSpeed <= MIN_SPIN_SPEED) {
                            isSpinning = false
                            isPaused = true
                            spinSpeed = 0f // Complete stop

                            // Here you could calculate and announce the prize
                            // based on the final rotationAngle

                            // Start pause timer
                            delay(PAUSE_DURATION)
                            isPaused = false
                            spinSpeed = BASE_SPIN_SPEED
                        }
                    }
                    isPaused -> {
                        // Do nothing - wheel stays still
                    }
                    else -> {
                        // Constant slow spin
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
                PrizeWheel(rotationAngle = rotationAngle)

                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    enabled = !isSpinning && !isPaused,  // Disable button during spin and pause
                    onClick = {
                        isSpinning = true
                        spinSpeed = MAX_SPIN_SPEED
                    }
                ) {
                    Text(when {
                        isSpinning -> "Spinning..."
                        isPaused -> "Getting Result..."
                        else -> "SPIN!"
                    })
                }
            }
        }
    }

    @Composable
    private fun PrizeWheel(
        modifier: Modifier = Modifier,
        rotationAngle: Float
    ) {
        Canvas(
            modifier = Modifier.size(300.dp)
        ) {
            val center = size.width / 2
            val radius = size.width / 2
            val segmentAngle = 2f * PI.toFloat() / segmentPattern.size

            // Rotate the entire wheel
            rotate(degrees = rotationAngle) {
                // Draw wheel segments
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

                    drawPath(
                        path = path,
                        color = color
                    )

                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2f)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = radius,
                    style = Stroke(width = 4f)
                )

                drawCircle(
                    color = Color.White,
                    radius = radius * 0.1f
                )
            }

            // Draw pointer (outside rotation scope so it stays fixed)
            drawLine(
                color = Color.Black,
                start = Offset(center, center),
                end = Offset(center, center - radius + 10f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // Draw center circle (over the pointer)
            drawCircle(
                color = Color.Black,
                radius = 8f,
                center = Offset(center, center)
            )
        }
    }
}