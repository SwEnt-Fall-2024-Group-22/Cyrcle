package com.github.se.cyrcle.ui.gambling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
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
    companion object {
        // Define our colors
        private object WheelColors {
            val Nothing = Color.Gray
            val Common = Color.Green
            val Rare = Color(0xFF0066FF)    // Blue
            val Epic = Color(0xFF800080)     // Purple
            val Legendary = Color(0xFFFFD700) // Gold
        }

        // Define our segments pattern
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
    }

    @Composable
    fun GamblingScreenContent() {
        var rotationAngle by remember { mutableStateOf(0f) }

        // Continuous rotation animation
        LaunchedEffect(Unit) {
            while(true) {
                delay(16) // roughly 60 FPS
                rotationAngle = (rotationAngle + 0.5f) % 360f // Adjust speed by changing 0.5f
            }
        }

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PrizeWheel(rotationAngle = rotationAngle)
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

                    // Draw segment fill
                    drawPath(
                        path = path,
                        color = color
                    )

                    // Draw segment borders
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw outer circle
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    style = Stroke(width = 4f)
                )

                // Draw inner circle
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