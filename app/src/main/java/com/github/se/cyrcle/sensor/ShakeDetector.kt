package com.github.se.cyrcle.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(context: Context, private val onShakeDetected: () -> Unit) :
    SensorEventListener {

  // Initialize the sensor manager and accelerometer sensor
  private val sensorManager: SensorManager =
      context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

  // Variables to keep track of the last x, y, z values and the last update time
  private var lastUpdate: Long = 0
  private var lastShakeTime: Long = 0
  private var lastX = 0f
  private var lastY = 0f
  private var lastZ = 0f

  /** Start listening for shake events. */
  fun start() {
    accelerometer?.let {
      sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
    }
  }

  /** Stop listening for shake events. */
  fun stop() {
    sensorManager.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent) {
    // Check if the sensor type is accelerometer
    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
      val curTime = System.currentTimeMillis()
      // Only allow one update after a certain time interval
      if ((curTime - lastUpdate) > SHAKE_SLOP_TIME_MS) {
        val diffTime = curTime - lastUpdate
        lastUpdate = curTime

        // Get the current x, y, z values from the accelerometer
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate the speed of the shake
        val speed =
            sqrt(
                (x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) /
                diffTime * 10000

        // If the speed is greater than the threshold and cooldown has passed, consider it a shake
        if ((speed > SHAKE_THRESHOLD) && (curTime - lastShakeTime > SHAKE_COOLDOWN_MS)) {
          onShakeDetected()
          lastShakeTime = curTime // Update toggle time after changing snow state
        }

        // Update the last x, y, z values
        lastX = x
        lastY = y
        lastZ = z
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // Not needed for this implementation
  }

  /**
   * Companion object to hold constants for shake detection.
   *
   * @property SHAKE_THRESHOLD The threshold for shake detection
   * @property SHAKE_SLOP_TIME_MS The time interval between shake updates
   * @property SHAKE_COOLDOWN_MS The cooldown time between shakes
   */
  companion object {
    private const val SHAKE_THRESHOLD = 650
    private const val SHAKE_SLOP_TIME_MS = 100
    private const val SHAKE_COOLDOWN_MS = 2000
  }
}
