package com.teeh.klimasensor

import java.time.LocalDateTime
import java.util.*

class TsEntry(val id: Int, val timestamp: Date, val humidity: Double?, val temperature: Double?, val pressure: Double?) {

    var realTemperature: Double? = null

    constructor(i: Int, d: Date, h: Double?, t: Double?, p: Double?, rt: Double?) : this(i, d, h, t, p) {
        realTemperature = rt
    }
}