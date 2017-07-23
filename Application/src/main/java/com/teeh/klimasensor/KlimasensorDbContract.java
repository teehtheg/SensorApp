package com.teeh.klimasensor;

import android.provider.BaseColumns;

/**
 * Created by teeh on 28.01.2017.
 */

public class KlimasensorDbContract {

    private KlimasensorDbContract() {}

    /* Inner class that defines the table contents */
    public static class KlimasensorEntry implements BaseColumns {
        public static final String TABLE_NAME = "KlimasensorEntry";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TEMPERATURE = "temperature";
        public static final String COLUMN_NAME_REAL_TEMPERATURE = "realTemperature";
        public static final String COLUMN_NAME_HUMIDITY = "humidity";
        public static final String COLUMN_NAME_PRESSURE = "pressure";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + KlimasensorDbContract.KlimasensorEntry.TABLE_NAME + " (" +
                    KlimasensorEntry._ID + " INTEGER PRIMARY KEY," +
                    KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    KlimasensorEntry.COLUMN_NAME_HUMIDITY + " REAL," +
                    KlimasensorEntry.COLUMN_NAME_PRESSURE + " REAL," +
                    KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE + " REAL," +
                    KlimasensorEntry.COLUMN_NAME_TEMPERATURE + " REAL)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + KlimasensorDbContract.KlimasensorEntry.TABLE_NAME;
}