package com.teeh.klimasensor.common.mappers

import com.jjoe64.graphview.series.DataPoint
import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.ts.SimpleEntry
import com.teeh.klimasensor.common.ts.SimpleTs
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils

import java.time.ZoneId
import java.util.ArrayList
import java.util.Date

object SimpleTsMapper {

    fun toSimpleTs(list_: List<TsEntry>, type: ValueType): SimpleTs {
        val list = ArrayList<SimpleEntry>()

        for (entry in list_) {
            val value: Double?

            when (type) {
                ValueType.TEMPERATURE -> value = entry.temperature
                ValueType.HUMIDITY -> value = entry.humidity
                ValueType.PRESSURE -> value = entry.pressure
                ValueType.REAL_TEMPERATURE -> value = entry.realTemperature
                else -> throw RuntimeException("incorrect ValueType")
            }

            val se = SimpleEntry(type, value, entry.timestamp)
            list.add(se)
        }

        return SimpleTs(list)
    }

    fun fromSimpleTs(ts: SimpleTs): List<DataPoint> {
        val list = ArrayList<DataPoint>()
        for ((_, value, timestamp1) in ts.ts) {
            if (value != null) {
                val timestamp = DateUtils.toDate(timestamp1)
                list.add(DataPoint(timestamp, value))
            }
        }
        return list
    }
}
