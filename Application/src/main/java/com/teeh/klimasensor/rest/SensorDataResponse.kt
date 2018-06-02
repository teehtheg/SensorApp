package com.teeh.klimasensor.rest

class SensorDataResponse {
    var data: List<SensorData>? = null
    var next: Int? = null

    @Override
    fun isEmpty():Boolean {
        if (checkNotNull(data).isEmpty()) {
            return true
        }
        return false
    }

    fun isLast():Boolean {
        if (next == null) {
            return true
        }
        return false
    }
}