package com.teeh.klimasensor.database

import android.provider.BaseColumns

object DatabaseContract {

    val SQL_CREATE_ENTRIES = "CREATE TABLE " + DatabaseContract.KlimasensorEntry.TABLE_NAME + " (" +
            KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
            KlimasensorEntry.COLUMN_NAME_HUMIDITY + " REAL," +
            KlimasensorEntry.COLUMN_NAME_PRESSURE + " REAL," +
            KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE + " REAL," +
            KlimasensorEntry.COLUMN_NAME_TEMPERATURE + " REAL)"

    val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DatabaseContract.KlimasensorEntry.TABLE_NAME

    /* Inner class that defines the table contents */
    class KlimasensorEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "KlimasensorEntry"
            val COLUMN_NAME_TIMESTAMP = "timestamp"
            val COLUMN_NAME_TEMPERATURE = "temperature"
            val COLUMN_NAME_REAL_TEMPERATURE = "realTemperature"
            val COLUMN_NAME_HUMIDITY = "humidity"
            val COLUMN_NAME_PRESSURE = "pressure"
        }
    }
}