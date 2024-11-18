package com.github.se.cyrcle.ui.gambling

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect
import kotlin.math.PI

class GamblingScreen {

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

    @Composable
    fun GamblingScreenContent() {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PrizeWheel()
            }
        }
    }

    @Composable
    private fun PrizeWheel(
        modifier: Modifier = Modifier
    ) {
        Canvas(
            modifier = Modifier.size(300.dp)
        ) {
            val center = size.width / 2
            val radius = size.width / 2
            val segmentAngle = 2f * PI.toFloat() / segmentPattern.size

            segmentPattern.forEachIndexed { index, segment ->
                val startAngle = index * segmentAngle
                val path = Path().apply {
                    moveTo(center, center)
                    // Create rectangle for the arc
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
    }
}