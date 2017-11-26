package com.teeh.klimasensor.common.ts

import com.teeh.klimasensor.common.utils.CalcUtil
import com.teeh.klimasensor.common.utils.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime

import java.util.Date
import java.util.HashMap

class SimpleTs(val ts: List<SimpleEntry>) {
    private val dateMap: MutableMap<LocalDateTime, Int>

    val valueList: List<Double?>
        get() = ts.map { x -> x.value }

    ////////////
    // Limits //
    ////////////

    val min: Double?
        get() = valueList.filterNotNull().min()

    val max: Double?
        get() = valueList.filterNotNull().max()

    val latestEntry: SimpleEntry
        get() = ts.last()


    /////////////////
    // Aggregators //
    /////////////////

    val latestValue: Double?
        get() = latestEntry.value

    val avg: Double
        get() {
            val ts = valueList
            val sum = CalcUtil.sum(ts)
            return sum / ts.size
        }

    val median: Double
        get() {
            val ts = valueList.filterNotNull().sorted()
            if (ts.size % 2 == 0) {
                val half = ts.size / 2
                return (ts[half - 1] + ts[half]) / 2.0
            } else {
                val half = (ts.size - 1) / 2
                return ts[half]
            }
        }

    val latestTimestamp: LocalDateTime
        get() = latestEntry.timestamp

    val firstTimestamp: LocalDateTime
        get() = ts[0].timestamp

    val latestTimestampString: String
        get() = DateUtils.toString(latestTimestamp)

    val firstTimestampString: String
        get() = DateUtils.toString(firstTimestamp)


    init {
        dateMap = HashMap()
        ts.forEachIndexed { index, simpleEntry -> dateMap.put(simpleEntry.timestamp, index) }
    }

    fun getEntry(d: LocalDateTime): SimpleEntry? {
        val index = dateMap[d]
        return if (index != null && index < ts.size) {
            ts[index]
        } else {
            null
        }
    }
}
