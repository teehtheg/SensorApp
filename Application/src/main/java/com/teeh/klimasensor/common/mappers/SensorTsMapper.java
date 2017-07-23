package com.teeh.klimasensor.common.mappers;

import com.teeh.klimasensor.TsEntry;
import com.teeh.klimasensor.common.mappers.SimpleTsMapper;
import com.teeh.klimasensor.common.ts.SensorTs;
import com.teeh.klimasensor.common.ts.SimpleTs;
import com.teeh.klimasensor.common.ts.ValueType;

import java.util.List;

/**
 * Created by teeh on 16.07.2017.
 */

public class SensorTsMapper {

    public static SensorTs createSensorTs(List<TsEntry> list) {

        SimpleTs temperature = SimpleTsMapper.toSimpleTs(list, ValueType.TEMPERATURE);
        SimpleTs realTemperature = SimpleTsMapper.toSimpleTs(list, ValueType.REAL_TEMPERATURE);
        SimpleTs pressure = SimpleTsMapper.toSimpleTs(list, ValueType.PRESSURE);
        SimpleTs humidity = SimpleTsMapper.toSimpleTs(list, ValueType.HUMIDITY);

        return new SensorTs(temperature, realTemperature, pressure, humidity);
    }

}
