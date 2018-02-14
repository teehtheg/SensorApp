package com.teeh.klimasensor.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase

import android.util.Log

import java.text.ParseException
import java.util.ArrayList
import java.util.Date

import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.constants.Constants
import com.teeh.klimasensor.common.exception.BusinessException
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.rest.SensorData
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatabaseService private constructor() {

    private val TAG = "DatabaseService"

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var readableDB: SQLiteDatabase
    private lateinit var writableDB: SQLiteDatabase

    private object Holder { val INSTANCE = DatabaseService() }

    companion object {
        val instance: DatabaseService by lazy { Holder.INSTANCE }

        private val EXTREMUM_MAX: String = "MAX"
        private val EXTREMUM_MIN: String = "MIN"
    }

    private val sensorDataProj = arrayOf(KlimasensorEntry.COLUMN_NAME_ID, KlimasensorEntry.COLUMN_NAME_TIMESTAMP, KlimasensorEntry.COLUMN_NAME_TEMPERATURE, KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, KlimasensorEntry.COLUMN_NAME_HUMIDITY, KlimasensorEntry.COLUMN_NAME_PRESSURE)

    //////////////////////
    // Database methods //
    //////////////////////

    val numberOfEntries: Long
        get() = DatabaseUtils.queryNumEntries(readableDB, KlimasensorEntry.TABLE_NAME)

    val oldestEntry: TsEntry
        get() = getExtremalEntry(EXTREMUM_MIN, KlimasensorEntry.COLUMN_NAME_TIMESTAMP)

    val latestEntry: TsEntry
        get() = getExtremalEntry(EXTREMUM_MAX, KlimasensorEntry.COLUMN_NAME_TIMESTAMP)

    ///////////////////////
    // Lifecycle methods //
    ///////////////////////

    fun start(context: Context) {
        dbHelper = DatabaseHelper.getInstance(context)

        doAsync {
            readableDB = dbHelper.readableDatabase
            writableDB = dbHelper.writableDatabase
        }
    }

    fun stop() {
        readableDB.close()
        writableDB.close()
        dbHelper.close()
    }

    fun getSensordata(leaveOut: Long): List<TsEntry> {
        val selection: String? = null
        val selectionArgs: Array<String>? = null
        val sortOrder = KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " ASC"

        val cursor = readableDB.query(
                KlimasensorEntry.TABLE_NAME,
                sensorDataProj,
                selection,
                selectionArgs, null, null,
                sortOrder
        )

        val result = getTsDataFromCursor(cursor, leaveOut)
        cursor.close()

        return result
    }

    fun getSensordataRange(startDate: LocalDateTime, endDate: LocalDateTime, leaveOut: Long): List<TsEntry> {
        val cursor = readableDB.rawQuery("SELECT * FROM " + KlimasensorEntry.TABLE_NAME +
                " WHERE " + KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " BETWEEN " + DateUtils.toLong(startDate) + " AND " + DateUtils.toLong(endDate) +
                " ORDER BY " + KlimasensorEntry.COLUMN_NAME_TIMESTAMP + " ASC", null)

        val result = getTsDataFromCursor(cursor, leaveOut)
        cursor.close()

        return result
    }

    fun getAllSensordataAsync(): Deferred<List<TsEntry>> {
        return async(CommonPool) {
            getSensordata(0L)
        }
    }

    fun getAllSensordata(): List<TsEntry> {
        return getSensordata(0L)
    }

    fun getAllSensordataRange(startDate: LocalDateTime, endDate: LocalDateTime): List<TsEntry> {
        return getSensordataRange(startDate, endDate, 0L)
    }

    fun addNewSensordata(list: List<String>) {
        for (line in list) {
            val fields = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            try {
                val d = DateUtils.toLocalDate(fields[0])
                val h = java.lang.Double.valueOf(fields[1])
                val t = java.lang.Double.valueOf(fields[2])
                val p = java.lang.Double.valueOf(fields[3])

                val content = ContentValues()
                content.put(KlimasensorEntry.COLUMN_NAME_TIMESTAMP, DateUtils.toLong(d))
                content.put(KlimasensorEntry.COLUMN_NAME_HUMIDITY, h)
                content.put(KlimasensorEntry.COLUMN_NAME_TEMPERATURE, t)
                content.put(KlimasensorEntry.COLUMN_NAME_PRESSURE, p)

                val res = writableDB.insert(
                        KlimasensorEntry.TABLE_NAME, null,
                        content
                )


            } catch (e: NumberFormatException) {
                Log.e(TAG, e.localizedMessage)
                continue
            } catch (e: ParseException) {
                Log.e(TAG, e.localizedMessage)
                continue
            }

        }
    }

    fun addNewSensordataFromRest(list: List<SensorData>) {
        for (line in list) {

            try {
                val d = DateUtils.toLocalDate(line.timestamp!!, DateTimeFormatter.RFC_1123_DATE_TIME)
                val h = line.humidity
                val t = line.temperature
                val p = line.pressure

                val content = ContentValues()
                content.put(KlimasensorEntry.COLUMN_NAME_TIMESTAMP, DateUtils.toLong(d))
                content.put(KlimasensorEntry.COLUMN_NAME_HUMIDITY, h)
                content.put(KlimasensorEntry.COLUMN_NAME_TEMPERATURE, t)
                content.put(KlimasensorEntry.COLUMN_NAME_PRESSURE, p)

                val res = writableDB.insert(
                        KlimasensorEntry.TABLE_NAME, null,
                        content
                )


            } catch (e: NumberFormatException) {
                Log.e(TAG, e.localizedMessage)
                continue
            } catch (e: ParseException) {
                Log.e(TAG, e.localizedMessage)
                continue
            }

        }
    }

    fun createEntries(list: List<TsEntry>): Long {
        var result: Long = 0

        for (entry in list) {
            val content = ContentValues()
            content.put(KlimasensorEntry.COLUMN_NAME_TIMESTAMP, DateUtils.toLong(entry.timestamp))
            content.put(KlimasensorEntry.COLUMN_NAME_HUMIDITY, entry.humidity)
            content.put(KlimasensorEntry.COLUMN_NAME_TEMPERATURE, entry.temperature)
            content.put(KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, entry.realTemperature)
            content.put(KlimasensorEntry.COLUMN_NAME_PRESSURE, entry.pressure)

            val res = writableDB.insert(
                    KlimasensorEntry.TABLE_NAME,
                    null,
                    content).toLong()

            result = result + res

        }
        return result
    }

    fun updateEntries(list: List<TsEntry>): Long {
        var result: Long = 0
        for (entry in list) {
            val content = ContentValues()
            content.put(KlimasensorEntry.COLUMN_NAME_TIMESTAMP, DateUtils.toLong(entry.timestamp))
            content.put(KlimasensorEntry.COLUMN_NAME_HUMIDITY, entry.humidity)
            content.put(KlimasensorEntry.COLUMN_NAME_TEMPERATURE, entry.temperature)
            content.put(KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, entry.realTemperature)
            content.put(KlimasensorEntry.COLUMN_NAME_PRESSURE, entry.pressure)

            val whereClause = KlimasensorEntry.COLUMN_NAME_ID + " = ?"
            val whereArgs = arrayOf(entry.id.toString())

            val res = writableDB.update(
                    KlimasensorEntry.TABLE_NAME,
                    content,
                    whereClause,
                    whereArgs
            ).toLong()
            result = result + res

        }
        return result
    }

    fun clearSensorData(): Long {
        return writableDB.delete(
                KlimasensorEntry.TABLE_NAME, "1", null
        ).toLong()
    }

    fun deleteEntries(list: List<TsEntry>): Long {
        var result: Long = 0
        for (entry in list) {
            val whereClause = KlimasensorEntry.COLUMN_NAME_ID + " = ?"
            val whereArgs = arrayOf(entry.id.toString())

            val res = writableDB.delete(
                    KlimasensorEntry.TABLE_NAME,
                    whereClause,
                    whereArgs
            ).toLong()
            result = result + res

        }
        return result
    }

    fun updateEntry(entry: TsEntry): Boolean {
        val list = ArrayList<TsEntry>()
        list.add(entry)
        val res = updateEntries(list)
        return if (res == 1L) {
            true
        } else false
    }

    fun createEntry(entry: TsEntry): Boolean {
        val list = ArrayList<TsEntry>()
        list.add(entry)
        val res = createEntries(list)
        return if (res == -1L) {
            true
        } else false
    }

    fun deleteEntry(entry: TsEntry): Boolean {
        val list = ArrayList<TsEntry>()
        list.add(entry)
        Log.i(BaseActivity.TAG, "delete: deleting" + entry.id)
        val res = deleteEntries(list)
        Log.i(BaseActivity.TAG, "delete: " + res.toString())
        return if (res == 1L) {
            true
        } else false
    }

    fun insertRealTemp(value: Double?, id: Int?): Boolean {
        var res = 0

        if (value != null) {

            val content = ContentValues()
            content.put(KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE, value)

            val whereClause = KlimasensorEntry.COLUMN_NAME_ID + " = ?"
            val whereArgs = arrayOf(id.toString())

            res = writableDB.update(
                    KlimasensorEntry.TABLE_NAME,
                    content,
                    whereClause,
                    whereArgs
            )

        }

        return if (res == 1) {
            true
        } else {
            false
        }
    }

    /////////////////////
    // Private methods //
    /////////////////////

    private fun getTsDataFromCursor(cursor: Cursor, leaveOut: Long = 0L): List<TsEntry> {
        val result = ArrayList<TsEntry>()
        var count: Long = 0L
        while (cursor.moveToNext()) {

            if (leaveOut == 0L || count % leaveOut == 0L) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_ID))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_TIMESTAMP))
                val temperature = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_TEMPERATURE))
                val pressure = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_PRESSURE))
                val humidity = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_HUMIDITY))
                var realTemp: Double? = null
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE))) {
                    realTemp = cursor.getDouble(cursor.getColumnIndexOrThrow(KlimasensorEntry.COLUMN_NAME_REAL_TEMPERATURE))
                }

                val entry = TsEntry(id,
                        DateUtils.toLocalDate(timestamp),
                        humidity,
                        temperature,
                        pressure,
                        realTemp)
                result.add(entry)
            }

            count = count + 1
        }
        Log.d(TAG, "Read ${result.size} entries from cursor (left out $leaveOut)")
        return result
    }

    private fun getExtremalEntry(extremum: String, field: String): TsEntry {
        val selection = field + " = (SELECT " + extremum + "(" + field + ") FROM " + KlimasensorEntry.TABLE_NAME + ")"

        val cursor = readableDB.query(
                KlimasensorEntry.TABLE_NAME,
                sensorDataProj,
                selection, null, null, null, null
        )

        val result = getTsDataFromCursor(cursor)
        cursor.close()

        Log.d(TAG, "Fetched: " + result)

        if (result.size == 1) {
            return result[0]
        }
        else if (result.size > 1) {
            Log.w(TAG, "Extremal entry is not unique!")
            return result[0]
        }
        else {
            Log.w(TAG, "Database is empty.")
            return TsEntry(0, LocalDateTime.of(0, 0, 0, 0, 0), null, null, null)
        }
    }
}
