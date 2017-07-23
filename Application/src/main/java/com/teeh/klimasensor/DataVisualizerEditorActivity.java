package com.teeh.klimasensor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.teeh.klimasensor.common.activities.BaseActivity;
import com.teeh.klimasensor.common.ts.SensorTs;
import com.teeh.klimasensor.common.ts.ValueType;
import com.teeh.klimasensor.common.utils.DateUtils;

import java.util.Date;

/**
 * Created by teeh on 21.06.2017.
 */

public class DataVisualizerEditorActivity extends BaseActivity {

    private static final String TAG = "DataVisualizerEditor";

    private CrystalRangeSeekbar rangeSeekbar;
    private Spinner tsTypeSpinner;
    private SensorTs sensorTs;

    private Long minDate;
    private Long maxDate;
    private Long startDate;
    private Long endDate;
    private int tsType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualizer_editor);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get range limits
        sensorTs = TimeseriesService.getInstance().getSensorTs();
        // here we could use any other ValueType aswell..
        maxDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).getLatestTimestamp());
        minDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).getFirstTimestamp());

        // setup range seekbar
        rangeSeekbar = (CrystalRangeSeekbar) findViewById(R.id.rangeSeekbar);
        rangeSeekbar.setOnRangeSeekbarChangeListener(getOnRangeSeekbarChangeListener());
        rangeSeekbar.setMinValue(minDate)
                    .setMaxValue(maxDate)
                    .setMinStartValue(minDate)
                    .setMaxStartValue(maxDate)
                    .setDataType(CrystalSeekbar.DataType.LONG)
                    .apply();

        // setup dataType selector
        tsTypeSpinner = (Spinner) findViewById(R.id.tsTypes);
        tsTypeSpinner.setOnItemSelectedListener(getOnItemSelectedListener());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dataTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tsTypeSpinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        maxDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).getLatestTimestamp());
        minDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).getFirstTimestamp());

        rangeSeekbar.setMinValue(minDate)
                    .setMaxValue(maxDate)
                    .setMinStartValue(minDate)
                    .setMaxStartValue(maxDate)
                    .setDataType(CrystalSeekbar.DataType.LONG)
                    .apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private OnRangeSeekbarChangeListener getOnRangeSeekbarChangeListener() {

        OnRangeSeekbarChangeListener listener = new OnRangeSeekbarChangeListener() {

            final TextView tvMin = (TextView) findViewById(R.id.lowerDate);
            final TextView tvMax = (TextView) findViewById(R.id.upperDate);

            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvMin.setText(DateUtils.toString(DateUtils.toDate((Long)minValue)));
                tvMax.setText(DateUtils.toString(DateUtils.toDate((Long)maxValue)));

                startDate = (Long)minValue;
                endDate = (Long)maxValue;
            }

        };

        return listener;
    }

    private Spinner.OnItemSelectedListener getOnItemSelectedListener() {

        Spinner.OnItemSelectedListener listener = new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = (String)parent.getItemAtPosition(position);
                Log.d(TAG, value);

                tsType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        };

        return listener;
    }

    public void showGraph(View view) {
        Intent serverIntent = new Intent(this, DataVisualizerActivity.class);

        Bundle b = new Bundle();
        b.putLong(DataVisualizerActivity.START_DATE, startDate);
        b.putLong(DataVisualizerActivity.END_DATE, endDate);
        b.putInt(DataVisualizerActivity.DATA_TYPE, tsType);
        serverIntent.putExtras(b);
        startActivity(serverIntent);
    }

}
