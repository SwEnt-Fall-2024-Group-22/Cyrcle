package com.github.se.cyrcle.ui.gambling

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.getButtonColors
import com.github.se.cyrcle.ui.theme.getOnColor
import kotlin.math.cos
import kotlin.math.sin



class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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

    private var rotation = 0f
    private var isSpinning = false
    private var spinSpeed = 0f
    private var lastUpdateTime = System.currentTimeMillis()
    private val idleRotationSpeed = -0.2f
    private var pauseAfterSpin = false
    private var pauseStartTime = 0L
    private val pauseDuration = 2000L

    private data class Segment(val name: String, val color: Int)

    private fun getSegmentAtPointer(rotation: Float): Int {
        val segmentAngle = 360f / segments.size
        val normalizedAngle = (270 - (rotation % 360)) % 360
        val segmentIndex = (normalizedAngle / segmentAngle).toInt()
        return segmentIndex
    }

    init {
        post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastUpdateTime
                lastUpdateTime = currentTime

                if (isSpinning) {
                    rotation -= spinSpeed
                    if (rotation <= -360f) rotation += 360f
                    spinSpeed *= 0.99f

                    if (spinSpeed < 0.1f) {
                        isSpinning = false
                        pauseAfterSpin = true
                        pauseStartTime = currentTime
                        spinSpeed = 0f
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
            // Random number of full rotations (2-4 spins) plus random additional angle
            val totalRotation = (kotlin.random.Random.nextFloat() * 2 + 2) * 360
            spinSpeed = totalRotation / 50f
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