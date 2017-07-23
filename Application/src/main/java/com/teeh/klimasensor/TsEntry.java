package com.teeh.klimasensor;

import java.util.Date;

/**
 * Created by teeh on 28.01.2017.
 */

public class TsEntry {
    private Integer id;
    private Date timestamp;
    private Double humidity;
    private Double temperature;
    private Double realTemperature;
    private Double pressure;

    TsEntry(Integer i, Date d, Double h, Double t, Double p) {
        id = i;
        timestamp = d;
        humidity = h;
        temperature = t;
        pressure = p;
    }

    TsEntry(Integer i, Date d, Double h, Double t, Double p, Double rt) {
        id = i;
        timestamp = d;
        humidity = h;
        temperature = t;
        realTemperature = rt;
        pressure = p;
    }

    public Integer getId() { return id; }

    public Date getTimestamp() {
        return timestamp;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getRealTemperature() {
        return realTemperature;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setRealTemperature(Double realTemperature) {
        this.realTemperature = realTemperature;
    }
}