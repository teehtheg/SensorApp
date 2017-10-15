package com.teeh.klimasensor.common.mappers

import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.SimpleTs
import com.teeh.klimasensor.common.ts.ValueType

object SensorTsMapper {

    fun createSensorTs(list: List<TsEntry>): SensorTs {

        val temperature = SimpleTsMapper.toSimpleTs(list, ValueType.TEMPERATURE)
        val realTemperature = SimpleTsMapper.toSimpleTs(list, ValueType.REAL_TEMPERATURE)
        val pressure = SimpleTsMapper.toSimpleTs(list, ValueType.PRESSURE)
        val humidity = SimpleTsMapper.toSimpleTs(list, ValueType.HUMIDITY)

        return SensorTs(temperature, realTemperature, pressure, humidity)
    }

}
