package com.teeh.klimasensor.common.ts;

import com.teeh.klimasensor.TsEntry;
import com.teeh.klimasensor.common.utils.CalcUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teeh on 16.07.2017.
 */

public class SensorTs {
    private SimpleTs temperature;
    private SimpleTs realTemperature;
    private SimpleTs pressure;
    private SimpleTs humidity;

    public SensorTs(SimpleTs temperature, SimpleTs realTemperature, SimpleTs pressure, SimpleTs humidity) {
        this.temperature = temperature;
        this.realTemperature = realTemperature;
        this.pressure = pressure;
        this.humidity = humidity;
    }

    public SimpleTs getTs(ValueType type) {
        switch(type) {
            case HUMIDITY: {
                return humidity;
            }
            case TEMPERATURE: {
                return temperature;
            }
            case REAL_TEMPERATURE: {
                return realTemperature;
            }
            case PRESSURE: {
                return pressure;
            }
            default: {
                return null;
            }
        }
    }


    /////////////////
    // Aggregators //
    /////////////////

    public Double getAvgTempDeviation() {
        List<Double> deviations = new ArrayList<Double>();
        for (SimpleEntry entry : realTemperature.getTs()) {
            SimpleEntry measuredEntry = temperature.getEntry(entry.getTimestamp());
            if (measuredEntry != null && measuredEntry.getValue() != null && entry.getValue() != null) {
                deviations.add(entry.getValue() - measuredEntry.getValue());
            }
        }
        Double sum = CalcUtil.sum(deviations);
        return sum / deviations.size();
    }

}
