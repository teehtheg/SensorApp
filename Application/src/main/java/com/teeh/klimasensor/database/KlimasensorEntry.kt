package com.teeh.klimasensor.database

import android.provider.BaseColumns

object KlimasensorEntry : BaseColumns {
    val TABLE_NAME = "KlimasensorEntry"
    val COLUMN_NAME_ID = "_id"
    val COLUMN_NAME_TIMESTAMP = "timestamp"
    val COLUMN_NAME_TEMPERATURE = "temperature"
    val COLUMN_NAME_REAL_TEMPERATURE = "realTemperature"
    val COLUMN_NAME_HUMIDITY = "humidity"
    val COLUMN_NAME_PRESSURE = "pressure"
}