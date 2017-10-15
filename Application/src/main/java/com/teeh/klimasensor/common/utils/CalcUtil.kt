package com.teeh.klimasensor.common.utils

object CalcUtil {

    fun <T : Number?> sum(collection: Collection<T>): Double {
        var sum: Double = 0.0

        for (entry in collection) {
            sum = sum + entry as Double
        }

        return sum
    }

}
