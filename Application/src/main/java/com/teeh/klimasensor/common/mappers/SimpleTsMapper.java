package com.teeh.klimasensor.common.mappers;

import com.jjoe64.graphview.series.DataPoint;
import com.teeh.klimasensor.TsEntry;
import com.teeh.klimasensor.common.ts.SimpleEntry;
import com.teeh.klimasensor.common.ts.SimpleTs;
import com.teeh.klimasensor.common.ts.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teeh on 16.07.2017.
 */

public class SimpleTsMapper {

    public static SimpleTs toSimpleTs(List<TsEntry> list_, ValueType type) {
        List<SimpleEntry> list = new ArrayList<SimpleEntry>();

        for (TsEntry entry : list_) {
            Double value;

            switch(type) {
                case TEMPERATURE:
                    value = entry.getTemperature();
                    break;
                case HUMIDITY:
                    value = entry.getHumidity();
                    break;
                case PRESSURE:
                    value = entry.getPressure();
                    break;
                case REAL_TEMPERATURE:
                    value = entry.getRealTemperature();
                    break;
                default:
                    throw new RuntimeException("incorrect ValueType");
            }

            SimpleEntry se = new SimpleEntry(type, value, entry.getTimestamp());
            list.add(se);
        }

        return new SimpleTs(list);
    }

    public static List<DataPoint> fromSimpleTs(SimpleTs ts) {
        List<DataPoint> list = new ArrayList<>();
        for (SimpleEntry entry : ts.getTs()) {
            if (entry.getValue() != null) {
                list.add(new DataPoint(entry.getTimestamp(), entry.getValue()));
            }
        }
        return list;
    }
}
