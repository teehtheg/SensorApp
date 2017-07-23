package com.teeh.klimasensor.common.ts;

import java.util.Date;

/**
 * Created by teeh on 16.07.2017.
 */

public class SimpleEntry {
    private ValueType type;
    private Double value;
    private Date timestamp;

    public SimpleEntry(ValueType type_, Double value_, Date timestamp_) {
        type = type_;
        value = value_;
        timestamp = timestamp_;
    }

    public ValueType getType() {
        return type;
    }

    public Double getValue() {
        return value;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
