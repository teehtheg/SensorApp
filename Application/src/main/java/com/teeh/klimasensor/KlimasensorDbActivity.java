package com.teeh.klimasensor;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.teeh.klimasensor.common.activities.BaseActivity;
import com.teeh.klimasensor.common.ts.SimpleTs;

import java.text.ParseException;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.N;

/**
 * Created by teeh on 28.01.2017.
 */

public class KlimasensorDbActivity extends BaseActivity {

    private EditText tsEntryTimestamp;
    private EditText tsEntryPressure;
    private EditText tsEntryTemperature;
    private EditText tsEntryRealTemperature;
    private EditText tsEntryHumidity;


    private TextView dbNumEntries;
    private TextView dbOldestEntry;
    private TextView dbLatestEntry;
    private SeekBar seekBar;
    private TextView seekBarText;
    private View.OnClickListener clearDataListener;
    private View.OnClickListener updateDataListener;

    private Integer currentIndex;
    private TsEntry shownEntry;
    private List<TsEntry> seekBarSteps;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_util);
    }

    @Override
    public void onStart() {
        super.onStart();

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBarText = (TextView) findViewById(R.id.seek_bar_text);
        seekBarSteps = DatabaseService.getInstance().getAllSensordata();
        seekBar.setMax(seekBarSteps.size()-1);
        currentIndex = 0;

        seekBar.setOnSeekBarChangeListener(getOnSeekBarChangeListener());

        tsEntryHumidity = (EditText) findViewById(R.id.tsentry_humidity);
        tsEntryPressure = (EditText) findViewById(R.id.tsentry_pressure);
        tsEntryRealTemperature = (EditText) findViewById(R.id.tsentry_real_temp);
        tsEntryTemperature = (EditText) findViewById(R.id.tsentry_temp);
        tsEntryTimestamp = (EditText) findViewById(R.id.tsentry_timestamp);

        dbNumEntries = (TextView) findViewById(R.id.db_num_entries);
        dbOldestEntry = (TextView) findViewById(R.id.db_oldest_entry);
        dbLatestEntry = (TextView) findViewById(R.id.db_latest_entry);

        Long numEntries = DatabaseService.getInstance().getNumberOfEntries();
        TsEntry oldestEntry = DatabaseService.getInstance().getOldestEntry();
        TsEntry latestEntry = DatabaseService.getInstance().getLatestEntry();

        dbNumEntries.setText(String.valueOf(numEntries));

        if (oldestEntry != null) {
            dbOldestEntry.setText(SimpleTs.Companion.getTsFormat().format(oldestEntry.getTimestamp()));
        }

        if (latestEntry != null) {
            dbLatestEntry.setText(SimpleTs.Companion.getTsFormat().format(latestEntry.getTimestamp()));
        }

        clearDataListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseService.getInstance().clearSensorData();
            }
        };

        updateDataListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSensordata();
            }
        };

    }

    public void clearDataWarning(View view) {
        Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.clear_data_warning, Snackbar.LENGTH_SHORT);

        mySnackbar.setAction("YES", clearDataListener)
                .setActionTextColor(Color.GREEN)
                .show();
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void updateSensordata(View v) {
        Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_warning, Snackbar.LENGTH_LONG);

        mySnackbar.setAction("YES", updateDataListener)
                .setActionTextColor(Color.GREEN)
                .show();
    }


    private void updateSensordata() {
        TsEntry entry = readTsEntry();
        boolean res = DatabaseService.getInstance().updateSensordata(entry);
        Snackbar snackbar;
        if (res) {
            snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_success, Snackbar.LENGTH_SHORT);
        } else {
            snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.update_data_failure, Snackbar.LENGTH_SHORT);
        }
        snackbar.show();
    }

    private void showTsEntry(TsEntry entry) {
        shownEntry = entry;

        tsEntryTimestamp.setText(SimpleTs.Companion.getTsFormat().format(entry.getTimestamp()));
        tsEntryTemperature.setText(entry.getTemperature().toString());
        tsEntryRealTemperature.setText(entry.getRealTemperature() != null ? entry.getRealTemperature().toString() : "null");
        tsEntryPressure.setText(entry.getPressure().toString());
        tsEntryHumidity.setText(entry.getHumidity().toString());
    }

    private TsEntry readTsEntry() {
        String ts = tsEntryTimestamp.getText().toString();
        String temp = tsEntryTemperature.getText().toString();
        String humid = tsEntryHumidity.getText().toString();
        String realtemp = tsEntryRealTemperature.getText().toString();
        String press = tsEntryPressure.getText().toString();

        try {
            shownEntry.setHumidity(Double.valueOf(humid));
            shownEntry.setPressure(Double.valueOf(press));
            shownEntry.setTemperature(Double.valueOf(temp));
            shownEntry.setRealTemperature("null".equals(realtemp) ? null : Double.valueOf(realtemp));
            shownEntry.setTimestamp(SimpleTs.Companion.getTsFormat().parse(ts));
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return shownEntry;
    }

    public void loadNext(View v) {
        if (currentIndex + 1 < seekBarSteps.size()) {
            currentIndex = currentIndex + 1;
            showTsEntry(seekBarSteps.get(currentIndex));
            seekBar.setProgress(currentIndex);
        }
    }

    public void loadPrev(View v) {
        if (currentIndex - 1 >= 0) {
            currentIndex = currentIndex - 1;
            showTsEntry(seekBarSteps.get(currentIndex));
            seekBar.setProgress(currentIndex);
        }
    }

    private SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {

            TsEntry current;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                current = seekBarSteps.get(progresValue);
                seekBarText.setText(SimpleTs.Companion.getTsFormat().format(current.getTimestamp()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                current = seekBarSteps.get(currentIndex);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentIndex = seekBar.getProgress();
                showTsEntry(current);
            }
        };

        return listener;
    }


}
