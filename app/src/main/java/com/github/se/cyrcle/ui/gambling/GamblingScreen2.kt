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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.cos
import kotlin.math.sin

class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "WheelView"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val segments = listOf(
        Segment("Nothing", Color.GRAY),
        Segment("Legendary", Color.rgb(255, 215, 0)),
        Segment("Common", Color.GREEN),
        Segment("Rare", Color.BLUE),
        Segment("Epic", Color.rgb(128, 0, 128))
    )

    private val probabilities = listOf(
        50f,    // Nothing: 50%
        0.1f,   // Legendary: 0.5%
        32f,    // Common: 30%
        16f,    // Rare: 15%
        1.9f    // Epic: 4.5%
    )

    // Range mapping for probabilities
    private val probabilityRanges = run {
        var currentValue = 0
        probabilities.map { probability ->
            val range = (probability * 10).toInt()
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

    private data class Segment(val name: String, val color: Int)

    private fun getSegmentAtPointer(rotation: Float): Int {
        val segmentAngle = 360f / segments.size
        val normalizedAngle = (270 - (rotation % 360)) % 360
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

        post(object : Runnable {
            override fun run() {
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
                        val easeOut = 1f - (1f - progress) * (1f - progress) * (1f - progress) * (1f - progress)
                        rotation = currentRotation + (targetRotation - currentRotation) * easeOut

                        // Optional: Log progress during spin (uncomment if needed)
                        // if (progress % 0.1f < 0.016f) {  // Log approximately every 10% of progress
                        //     Log.d(TAG, "Spin progress: ${(progress * 100).toInt()}%, easeOut: ${easeOut}")
                        // }
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
                postDelayed(this, 16)
            }
        })
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
                paint
            )
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

            canvas.drawText(
                segment.name,
                textX,
                textY + textHeight / 2,
                paint
            )
            canvas.restore()
        }

        canvas.restore()

        // Draw the pointer
        paint.color = Color.RED
        paint.style = Paint.Style.FILL

        val pointerPath = Path()
        val pointerWidth = radius * 0.1f
        val pointerHeight = radius * 0.1f

        pointerPath.moveTo(centerX, centerY - radius + pointerHeight*1.5f)
        pointerPath.lineTo(centerX - pointerWidth/2, centerY - radius - pointerHeight/2)
        pointerPath.lineTo(centerX + pointerWidth/2, centerY - radius - pointerHeight/2)
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

            // Calculate how much we can safely rotate within the target segment
            // First, find where exactly in the target segment we'll land
            val landingAngle = (270 - ((rotation - requiredRotation) % 360)) % 360
            val angleWithinSegment = landingAngle % segmentAngle

            // Calculate maximum safe offset in both directions
            val maxOffsetLeft = angleWithinSegment  // distance to previous segment
            val maxOffsetRight = segmentAngle - angleWithinSegment  // distance to next segment
            val safeOffset = minOf(maxOffsetLeft, maxOffsetRight) * 0.9f // 90% of the minimum to be extra safe

            // Generate random offset between -safeOffset and +safeOffset
            val randomOffset = (Math.random() * 2 * safeOffset - safeOffset).toFloat()
            requiredRotation += randomOffset

            // Ensure we're always rotating clockwise
            if (requiredRotation <= 0) requiredRotation += 360

            // Add 6-7 full rotations for effect
            val fullRotations = (6..7).random() * 360
            targetRotation = rotation - (requiredRotation + fullRotations)

            Log.d(TAG, "Landing angle within segment: $angleWithinSegment")
            Log.d(TAG, "Safe offset range: $safeOffset degrees")
            Log.d(TAG, "Applied random offset: $randomOffset degrees")
            Log.d(TAG, "Required rotation: $requiredRotation degrees")
            Log.d(TAG, "Total rotation with full spins: ${requiredRotation + fullRotations} degrees")

            currentRotation = rotation
            spinStartTime = System.currentTimeMillis()
            spinDuration = 10000 // 10 seconds
            isSpinning = true
        }
    }
}

@Composable
fun GamblingScreen2() {
    var wheelView by remember { mutableStateOf<WheelView?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.size(300.dp),
            factory = { context ->
                WheelView(context).also {
                    wheelView = it
                }
            }
        )

        Button(
            onClick = {
                wheelView?.spin()
            },
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Red
            )
        ) {
            Text(
                "SPIN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}