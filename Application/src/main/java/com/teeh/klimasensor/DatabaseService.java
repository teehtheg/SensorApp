package com.teeh.klimasensor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.teeh.klimasensor.common.Constants;
import com.teeh.klimasensor.common.ts.SimpleTs;

import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.R.attr.id;
import static android.R.id.list;
import static android.media.CamcorderProfile.get;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by teeh on 29.01.2017.
 */


public class DatabaseService {

    private static final String TAG = "DatabaseService";

    private static DatabaseService instance;

    private KlimasensorDbHelper dbHelper;
    private SQLiteDatabase readableDB;
    private SQLiteDatabase writableDB;

    private String[] sensorDataProj = {
            KlimasensorDbContract.KlimasensorEntry._ID,
            KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP,
            KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TEMPERATURE,
            KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE,
            KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_HUMIDITY,
            KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_PRESSURE
    };

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }

        return instance;
    }

    private DatabaseService() {
    }

    ///////////////////////
    // Lifecycle methods //
    ///////////////////////

    public void start(Context context) {
        dbHelper = new KlimasensorDbHelper(context);

        ReadableDBTask readableDBTask = new ReadableDBTask();
        readableDBTask.execute();

        WritableDBTask writableDBTask = new WritableDBTask();
        writableDBTask.execute();
    }

    public void stop() {
        readableDB.close();
        writableDB.close();
        dbHelper.close();
    }

    //////////////////////
    // Database methods //
    //////////////////////

    public Long getNumberOfEntries() {
        return DatabaseUtils.queryNumEntries(readableDB, KlimasensorDbContract.KlimasensorEntry.TABLE_NAME);
    }

    public TsEntry getOldestEntry() {
        return getExtremalEntry("MIN", KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP);
    }

    public TsEntry getLatestEntry() {
        return getExtremalEntry("MAX", KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP);
    }

    public void clearSensorData() {
        writableDB.delete(
                KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                null,
                null
        );
    }

    public List<TsEntry> getAllSensordata() {

        // Filter results WHERE "title" = 'My Title'
        // String selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
        // String[] selectionArgs = { "My Title" };
        String selection = null;
        String[] selectionArgs = null;

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " ASC";

        Cursor cursor = readableDB.query(
                KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                sensorDataProj,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        Long numEntries = getNumberOfEntries();
        //Long leaveOut = numEntries/1000;
        Long leaveOut = 0L;
        Log.d(TAG, "Leaving out " + leaveOut + " entries.");
        List<TsEntry> result = getTsDataFromCursor(cursor, leaveOut);
        cursor.close();

        return result;
    }

    public List<TsEntry> getAllSensordataRange(Date startDate, Date endDate) {

        Cursor cursor = readableDB.rawQuery("SELECT * FROM " + KlimasensorDbContract.KlimasensorEntry.TABLE_NAME +
                " WHERE " + KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " BETWEEN "+ startDate.getTime() +" AND "+ endDate.getTime() +
                " ORDER BY " + KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " ASC", null);


        Long leaveOut = 0L;
        List<TsEntry> result = getTsDataFromCursor(cursor, leaveOut);
        cursor.close();

        return result;
    }

    public void addNewSensordata(List<String> list) {
        for (String line : list) {
            String[] fields = line.split(",");

            try {
                Date d = SimpleTs.tsFormat.parse(fields[0]);
                Double h = Double.valueOf(fields[1]);
                Double t = Double.valueOf(fields[2]);
                Double p = Double.valueOf(fields[3]);

                ContentValues content = new ContentValues();
                content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP, d.getTime());
                content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_HUMIDITY, h);
                content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TEMPERATURE, t);
                content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_PRESSURE, p);

                long res = writableDB.insert(
                        KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                        null,
                        content
                );


            } catch (NumberFormatException e) {
                Log.e(TAG, e.getLocalizedMessage());
                continue;
            } catch (ParseException e) {
                Log.e(TAG, e.getLocalizedMessage());
                continue;
            }
        }
    }

    public long updateSensordata(List<TsEntry> list) {
        long result = 0;
        for (TsEntry entry : list) {
            ContentValues content = new ContentValues();
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP, entry.getTimestamp().getTime());
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_HUMIDITY, entry.getHumidity());
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TEMPERATURE, entry.getTemperature());
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, entry.getRealTemperature());
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_PRESSURE, entry.getPressure());

            String whereClause = KlimasensorDbContract.KlimasensorEntry._ID + " = ?";
            String whereArgs[] = { entry.getId().toString() };

            long res = writableDB.update(
                    KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                    content,
                    whereClause,
                    whereArgs
            );
            result = result + res;

        }
        return result;
    }

    public boolean updateSensordata(TsEntry entry) {
        List<TsEntry> list = new ArrayList<>();
        list.add(entry);
        long res = updateSensordata(list);
        if (res == 1) {
            return true;
        }
        return false;
    }

    public boolean insertRealTemp(Double value, Integer id) {
        int res = 0;

        if (value != null) {

            ContentValues content = new ContentValues();
            content.put(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, value);

            String whereClause = KlimasensorDbContract.KlimasensorEntry._ID + " = ?";
            String whereArgs[] = { id.toString() };

            res = writableDB.update(
                    KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                    content,
                    whereClause,
                    whereArgs
            );

        }

        if (res == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    /////////////////////
    // Private methods //
    /////////////////////

    private List<TsEntry> getTsDataFromCursor(Cursor cursor) {
        return getTsDataFromCursor(cursor, 0L);
    }

    private List<TsEntry> getTsDataFromCursor(Cursor cursor, Long leaveOut) {
        List<TsEntry> result = new ArrayList<>();
        Long count = 0L;
        while(cursor.moveToNext()) {

            if (leaveOut == 0 || count % leaveOut == 0) {
                Integer id = cursor.getInt(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry._ID));
                Date timestamp = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TIMESTAMP)));
                Double temperature = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_TEMPERATURE));
                Double pressure = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_PRESSURE));
                Double humidity = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_HUMIDITY));
                Double realTemp = null;
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE))) {
                    realTemp = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorDbContract.KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE));
                }

                TsEntry entry = new TsEntry(id, timestamp, humidity, temperature, pressure, realTemp);
                result.add(entry);
            }

            count = count + 1;
        }
        Log.d(TAG, "Read " + result.size() + " entries from cursor");
        return result;
    }

    private TsEntry getExtremalEntry(String extremum, String field) {
        String selection = field + " = (SELECT " + extremum + "(" + field + ") FROM " + KlimasensorDbContract.KlimasensorEntry.TABLE_NAME + ")";

        Cursor cursor = readableDB.query(
                KlimasensorDbContract.KlimasensorEntry.TABLE_NAME,
                sensorDataProj,
                selection,
                null,
                null,
                null,
                null
        );

        List<TsEntry> result = getTsDataFromCursor(cursor);
        cursor.close();

        if (result.size() == 1) {
            return result.get(0);
        }

        if (result.size() > 1) {
            Log.d(TAG, "Extremal entry is not unique!");
        }

        if (result.size() == 0) {
            Log.e(TAG, "No extremal entry found!");
        }

        return null;
    }

    private class ReadableDBTask extends AsyncTask<Void, Void, SQLiteDatabase> {

        protected SQLiteDatabase doInBackground(Void... voids) {
            return dbHelper.getReadableDatabase();
        }

        protected void onPostExecute(SQLiteDatabase result) {
            readableDB = result;
        }
    }

    private class WritableDBTask extends AsyncTask<Void, Void, SQLiteDatabase> {

        protected SQLiteDatabase doInBackground(Void... voids) {
            return dbHelper.getWritableDatabase();
        }

        protected void onPostExecute(SQLiteDatabase result) {
            writableDB = result;
        }
    }
}
