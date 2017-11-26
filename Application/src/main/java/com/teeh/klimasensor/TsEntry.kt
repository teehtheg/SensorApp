package com.teeh.klimasensor

import java.time.LocalDateTime
import java.util.*

class TsEntry(val id: Int, val timestamp: Date, val humidity: Double?, val temperature: Double?, val pressure: Double?) {

    var realTemperature: Double? = null

    constructor(id: Int,
                timestamp: Date,
                humidity: Double?,
                temperature: Double?,
                pressure: Double?,
                realTemperature: Double?) : this(id, timestamp, humidity, temperature, pressure) {

        this.realTemperature = realTemperature
    }

    override fun toString():String {
        return "TsEntry[id: '" + id + "', timestamp: '" + timestamp + "']";
    }
}