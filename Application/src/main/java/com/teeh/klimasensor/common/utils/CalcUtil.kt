package com.teeh.klimasensor.common.utils

import java.util.ArrayList

object CalcUtil {

    fun <T : Number?> sum(collection: Collection<T>): Double {
        var sum: Double = 0.0

        for (entry in collection) {
            sum = sum + entry as Double
        }

        return sum
    }

    fun normalize(collection: Collection<Double>, normalization: Double?): List<Double> {
        val result = ArrayList<Double>()
        for (entry in collection) {
            result.add(entry / normalization!!)
        }
        return result
    }

}
