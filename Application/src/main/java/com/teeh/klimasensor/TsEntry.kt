package com.teeh.klimasensor

import java.time.LocalDateTime

class TsEntry(val id: Int, val timestamp: LocalDateTime, val humidity: Double?, val temperature: Double?, val pressure: Double?) {

    var realTemperature: Double? = null

    constructor(i: Int, d: LocalDateTime, h: Double?, t: Double?, p: Double?, rt: Double?) : this(i, d, h, t, p) {
        realTemperature = rt
    }
}