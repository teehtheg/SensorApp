package com.teeh.klimasensor;

import android.content.Context;
import android.util.Log;

import com.teeh.klimasensor.common.ts.SensorTs;
import com.teeh.klimasensor.common.mappers.SensorTsMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.provider.Telephony.Mms.Part.FILENAME;

/**
 * Created by teeh on 30.12.2016.
 */

public class    TimeseriesService {

    private static final Integer NUM_ENTRIES = 500;

    private static final String TAG = "TimeseriesService";

    private static TimeseriesService instance;

    public static TimeseriesService getInstance() {
        if (instance == null) {
            instance = new TimeseriesService();
        }
        return instance;
    }

    private TimeseriesService() {
    }

    /////////////////////////////
    // Loading Timeseries Data //
    /////////////////////////////

    public SensorTs getSensorTs() {
        List<TsEntry> list = readFromDB();
        return getSensorTs(list, false);
    }

    public SensorTs getSensorTs(Date startDate, Date endDate) {
        List<TsEntry> list = readRangeFromDB(startDate, endDate);
        return getSensorTs(list, false);
    }

    public SensorTs getSensorTsReduced() {
        List<TsEntry> list = readFromDB();
        return getSensorTs(list, true);
    }

    public SensorTs getSensorTsReduced(Date startDate, Date endDate) {
        List<TsEntry> list = readRangeFromDB(startDate, endDate);
        return getSensorTs(list, true);
    }

    ////////////////////
    // Database stuff //
    ////////////////////

    public List<TsEntry> readRangeFromDB(Date startDate, Date endDate) {
        return DatabaseService.getInstance().getAllSensordataRange(startDate, endDate);
    }

    public List<TsEntry> readFromDB() {

        return DatabaseService.getInstance().getAllSensordata();
    }

    public void writeToDB(List<String> list) {
        DatabaseService.getInstance().addNewSensordata(list);
    }

    ////////////////////////////
    // Old file storage stuff //
    ////////////////////////////

    public void writeFile(List<String> file, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            for (String line : file) {
                fos.write(line.getBytes());
                if (!line.endsWith("\n")) {
                    fos.write("\n".getBytes());
                }
            }
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Internal storage file not found.");
        } catch (IOException e) {
            Log.e(TAG, "An IOException occured while writing to file.");
        }
    }

    public List<String> loadFromFile(Context context) throws IOException {
        String msg = "";

        FileInputStream fis = context.openFileInput(FILENAME);
        byte[] buffer = new byte[1024];

        while (true) {
            int bytes = fis.read(buffer);
            if (bytes == -1) {
                break;
            }
            String msgPart = new String(buffer, 0, bytes);
            msg = msg + msgPart;
        }
        fis.close();


        String[] lines = msg.split("\n");
        List<String> list = Arrays.asList(lines);
        Log.i(TAG, "Parsed " + list.size() + " lines from file.");
        return list;
    }

    public void appendToFile(List<String> update, Context context) {
        List<String> file;
        try {
            file = new ArrayList<>(instance.loadFromFile(context));
        } catch (IOException e) {
            Log.e(TAG, "No file found. Creating new one");
            file = new ArrayList<>();
        }
        Log.d(TAG, "Appending " + update.size() + " lines to file.");

        file.addAll(update);
        instance.writeFile(file,context);
    }

    /////////////////////
    // Utility Methods //
    /////////////////////

    // reduce the number of TsEntries to NUM_ENTRIES
    private List<TsEntry> reduceTo(List<TsEntry> list, Integer reduceTo) {
        Integer num = list.size();
        Integer toRemove = reduceTo - num;

        if (toRemove > 0) {
            return list;
        }
        else {
            Integer ith = num / toRemove;
            List<TsEntry> newList = new ArrayList<TsEntry>();
            for (int i=0; i<num; i++) {
                if (i % ith != 0) {
                    newList.add(list.get(i));
                }
            }
            return newList;
        }
    }

    private SensorTs getSensorTs(List<TsEntry> list, boolean reduced) {
        if (reduced) {
            list = reduceTo(list, NUM_ENTRIES);
        }
        return SensorTsMapper.createSensorTs(list);
    }
}
