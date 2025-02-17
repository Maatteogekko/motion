package me.cendre.motion.plugin

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink

internal class StreamHandlerImpl(
        private val sensorManager: SensorManager,
        sensorType: Int,
        private val updateInterval : Int
) : EventChannel.StreamHandler {
    private var sensorEventListener: SensorEventListener? = null

    private val sensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(sensorType)
    }

    override fun onListen(arguments: Any?, events: EventSink) {
        sensor?.let {
            sensorEventListener = createSensorEventListener(events)
            sensorManager.registerListener(sensorEventListener, it, updateInterval)
        } ?: run {
            events.error("SENSOR_UNAVAILABLE", "Sensor not available on this device.", null)
        }
    }

    override fun onCancel(arguments: Any?) {
        sensorEventListener?.let {
            sensorManager.unregisterListener(it)
        }
    }

    private fun createSensorEventListener(events: EventSink): SensorEventListener {
        return object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {
                val sensorValues = DoubleArray(event.values.size)
                event.values.forEachIndexed { index, value ->
                    sensorValues[index] = value.toDouble()
                }
                events.success(sensorValues)
            }
        }
    }
}
