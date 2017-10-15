package com.teeh.klimasensor.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DatabaseHelper(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "Klimasensor.db"

        private var instance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(KlimasensorEntry.TABLE_NAME, true,
                KlimasensorEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY,
                KlimasensorEntry.COLUMN_NAME_TIMESTAMP to INTEGER,
                KlimasensorEntry.COLUMN_NAME_HUMIDITY to REAL,
                KlimasensorEntry.COLUMN_NAME_PRESSURE to REAL,
                KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE to REAL,
                KlimasensorEntry.COLUMN_NAME_TEMPERATURE to REAL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // db.execSQL(KlimasensorDbContract.SQL_DELETE_ENTRIES);
        // onCreate(db);
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

}