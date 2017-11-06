package com.teeh.klimasensor.common.ts

import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.utils.CalcUtil

import java.util.ArrayList

class SensorTs(private val temperature: SimpleTs,
               private val realTemperature: SimpleTs,
               private val pressure: SimpleTs,
               private val humidity: SimpleTs) {


    /////////////////
    // Aggregators //
    /////////////////

    val avgTempDeviation: Double?
        get() {
            val deviations = ArrayList<Double>()
            for (entry in realTemperature.ts) {
                val measuredEntry = temperature.getEntry(entry.timestamp)
                if (measuredEntry != null && measuredEntry.value != null && entry.value != null) {
                    deviations.add(entry.value!! - measuredEntry.value!!)
                }
            }
            val sum = CalcUtil.sum(deviations)
            return sum!! / deviations.size
        }

    fun getTs(type: ValueType): SimpleTs {
        when (type) {
            ValueType.HUMIDITY -> {
                return humidity
            }
            ValueType.TEMPERATURE -> {
                return temperature
            }
            ValueType.REAL_TEMPERATURE -> {
                return realTemperature
            }
            ValueType.PRESSURE -> {
                return pressure
            }
            else -> {
                throw Exception("Unknown type")
            }
        }
    }

}
