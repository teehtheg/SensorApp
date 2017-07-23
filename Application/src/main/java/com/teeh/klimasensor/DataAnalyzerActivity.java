package com.teeh.klimasensor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.teeh.klimasensor.common.activities.BaseActivity;
import com.teeh.klimasensor.common.ts.SensorTs;
import com.teeh.klimasensor.common.ts.ValueType;

import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.R.attr.duration;
import static android.R.attr.id;
import static android.R.id.input;
import static android.media.CamcorderProfile.get;


/**
 * Created by teeh on 30.12.2016.
 */

public class DataAnalyzerActivity extends BaseActivity {

    private static final String TAG = "DataAnalyzerActivity";

    private SensorTs sensorTs;

    private NumberFormat nf;

    private TextView humidity_lat;
    private TextView humidity_min;
    private TextView humidity_max;
    private TextView humidity_avg;
    private TextView humidity_med;

    private TextView temperature_lat;
    private TextView temperature_min;
    private TextView temperature_max;
    private TextView temperature_avg;
    private TextView temperature_med;
    private TextView temperature_dev;

    private TextView pressure_lat;
    private TextView pressure_min;
    private TextView pressure_max;
    private TextView pressure_avg;
    private TextView pressure_med;

    private EditText realTempInput;

    private ValueType[] tsTypes = {ValueType.HUMIDITY, ValueType.TEMPERATURE, ValueType.PRESSURE};
    private Map<ValueType, List<TextView>> typeDisplayMapping = new HashMap<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analyzer);
    }


    @Override
    public void onStart() {
        super.onStart();

        nf = NumberFormat.getInstance(Locale.GERMAN);
        nf.setMaximumFractionDigits(2);

        humidity_lat = (TextView) findViewById(R.id.humidity_lat);
        humidity_min = (TextView) findViewById(R.id.humidity_min);
        humidity_max = (TextView) findViewById(R.id.humidity_max);
        humidity_avg = (TextView) findViewById(R.id.humidity_avg);
        humidity_med = (TextView) findViewById(R.id.humidity_med);
        TextView[] humidityTw = {humidity_lat, humidity_min, humidity_max, humidity_avg, humidity_med};
        typeDisplayMapping.put(ValueType.HUMIDITY, new ArrayList<TextView>(Arrays.asList(humidityTw)));

        temperature_lat = (TextView) findViewById(R.id.temperature_lat);
        temperature_min = (TextView) findViewById(R.id.temperature_min);
        temperature_max = (TextView) findViewById(R.id.temperature_max);
        temperature_avg = (TextView) findViewById(R.id.temperature_avg);
        temperature_med = (TextView) findViewById(R.id.temperature_med);
        TextView[] temperatureTw = {temperature_lat, temperature_min, temperature_max, temperature_avg, temperature_med};
        typeDisplayMapping.put(ValueType.TEMPERATURE, new ArrayList<TextView>(Arrays.asList(temperatureTw)));

        pressure_lat = (TextView) findViewById(R.id.pressure_lat);
        pressure_min = (TextView) findViewById(R.id.pressure_min);
        pressure_max = (TextView) findViewById(R.id.pressure_max);
        pressure_avg = (TextView) findViewById(R.id.pressure_avg);
        pressure_med = (TextView) findViewById(R.id.pressure_med);
        TextView[] pressureTw = {pressure_lat, pressure_min, pressure_max, pressure_avg, pressure_med};
        typeDisplayMapping.put(ValueType.PRESSURE, new ArrayList<TextView>(Arrays.asList(pressureTw)));

        temperature_dev = (TextView) findViewById(R.id.temperature_dev);

        realTempInput = (EditText) findViewById(R.id.input_real_temp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorTs = TimeseriesService.getInstance().getSensorTs();

        initializeKeyfigures();

        temperature_dev.setText(nf.format(sensorTs.getAvgTempDeviation()));
    }


    private void initializeKeyfigures() {
        for (ValueType i : tsTypes) {
            // this order here is depending on the order of how they're
            // added to typeDisplayMapping!
            setLatest(i, typeDisplayMapping.get(i).get(0), nf);
            setMin(i, typeDisplayMapping.get(i).get(1), nf);
            setMax(i, typeDisplayMapping.get(i).get(2), nf);
            setAvg(i, typeDisplayMapping.get(i).get(3), nf);
            setMedian(i, typeDisplayMapping.get(i).get(4), nf);
        }
    }

    private void setLatest(ValueType type, TextView view, NumberFormat nf) {
        view.setText(nf.format(sensorTs.getTs(type).getLatestValue()));
    }

    private void setMin(ValueType type, TextView view, NumberFormat nf) {
        view.setText(nf.format(sensorTs.getTs(type).getMin()));
    }

    private void setMax(ValueType type, TextView view, NumberFormat nf) {
        view.setText(nf.format(sensorTs.getTs(type).getMax()));
    }

    private void setAvg(ValueType type, TextView view, NumberFormat nf) {
        view.setText(nf.format(sensorTs.getTs(type).getAvg()));
    }

    private void setMedian(ValueType type, TextView view, NumberFormat nf) {
        view.setText(nf.format(sensorTs.getTs(type).getMedian()));
    }

    public void addRealTemp(View view) {
        Editable editable = realTempInput.getText();
        String input = editable.toString();
        boolean success = false;
        TsEntry entry = DatabaseService.getInstance().getLatestEntry();

        if (input != null && entry != null) {
            Double value = Double.valueOf(input);
            success = DatabaseService.getInstance().insertRealTemp(value, entry.getId());
        }

        if (success) {
            realTempInput.setText("");
            Toast.makeText(this, "Value added!", Toast.LENGTH_SHORT).show();
        }
    }
}
