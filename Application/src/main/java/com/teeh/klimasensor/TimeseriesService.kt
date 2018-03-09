package com.teeh.klimasensor

import android.content.Context
import android.util.Log

import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.mappers.SensorTsMapper
import com.teeh.klimasensor.database.DatabaseService

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.util.ArrayList
import java.util.Arrays
import java.util.Date

import android.provider.Telephony.Mms.Part.FILENAME
import com.google.android.gms.tasks.Tasks.await
import com.teeh.klimasensor.rest.SensorData
import kotlinx.coroutines.experimental.*

/**
 * Created by teeh on 30.12.2016.
 */

class TimeseriesService private constructor() {

    private object Holder { val INSTANCE = TimeseriesService() }

    companion object {
        private val NUM_ENTRIES = 2000
        private val TAG = "TimeseriesService"

        val instance: TimeseriesService by lazy { Holder.INSTANCE }
    }

    /////////////////////////////
    // Loading Timeseries Data //
    /////////////////////////////

    val sensorTs: Deferred<SensorTs>
        get() = async(CommonPool) {
            getSensorTs(readFromDB(), false)
        }

    val sensorTsReduced: Deferred<SensorTs>
        get() = async(CommonPool) {
            getSensorTs(readFromDB(), true)
        }

    fun getSensorTsAsync(startDate: LocalDateTime, endDate: LocalDateTime): Deferred<SensorTs> {
        return async(CommonPool) {
            getSensorTs(readRangeFromDB(startDate, endDate), false)
        }
    }

    fun getSensorTsReducedAsync(startDate: LocalDateTime, endDate: LocalDateTime): Deferred<SensorTs> {
        return async(CommonPool) {
            getSensorTs(readRangeFromDBReduced(startDate, endDate), false)
        }
    }

    fun updateSensorTsAsync(update: List<SensorData>): Job {
        return launch(CommonPool) {
            DatabaseService.instance.addNewSensordataFromRest(update)
        }
    }

    ////////////////////
    // Database stuff //
    ////////////////////

    fun readRangeFromDB(startDate: LocalDateTime, endDate: LocalDateTime): List<TsEntry> {
        return DatabaseService.instance.getAllSensordataRange(startDate, endDate)
    }

    fun readRangeFromDBReduced(startDate: LocalDateTime, endDate: LocalDateTime): List<TsEntry> {
        val numEntries = DatabaseService.instance.numberOfEntries
        val leaveOut = numEntries/NUM_ENTRIES
        return DatabaseService.instance.getSensordataRange(startDate, endDate, leaveOut)
    }

    fun readFromDB(): List<TsEntry> {
        return DatabaseService.instance.getAllSensordata()
    }

    fun writeToDB(list: List<String>) {
        DatabaseService.instance.addNewSensordata(list)
    }

    fun readFirstFromDB(): TsEntry {
        return DatabaseService.instance.oldestEntry
    }

    fun readLastFromDB(): TsEntry {
        return DatabaseService.instance.latestEntry
    }

    ////////////////////////////
    // Old file storage stuff //
    ////////////////////////////

    fun writeFile(file: List<String>, context: Context) {
        try {
            val fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
            for (line in file) {
                fos.write(line.toByteArray())
                if (!line.endsWith("\n")) {
                    fos.write("\n".toByteArray())
                }
            }
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Internal storage file not found.")
        } catch (e: IOException) {
            Log.e(TAG, "An IOException occured while writing to file.")
        }

    }

    @Throws(IOException::class)
    fun loadFromFile(context: Context): List<String> {
        var msg = ""

        val fis = context.openFileInput(FILENAME)
        val buffer = ByteArray(1024)

        while (true) {
            val bytes = fis.read(buffer)
            if (bytes == -1) {
                break
            }
            val msgPart = String(buffer, 0, bytes)
            msg = msg + msgPart
        }
        fis.close()


        val lines = msg.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list = Arrays.asList(*lines)
        Log.i(TAG, "Parsed " + list.size + " lines from file.")
        return list
    }

    fun appendToFile(update: List<String>, context: Context) {
        var file: MutableList<String>
        try {
            file = ArrayList(instance.loadFromFile(context))
        } catch (e: IOException) {
            Log.e(TAG, "No file found. Creating new one")
            file = ArrayList()
        }

        Log.d(TAG, "Appending " + update.size + " lines to file.")

        file.addAll(update)
        instance.writeFile(file, context)
    }

    /////////////////////
    // Utility Methods //
    /////////////////////

    // reduce the number of TsEntries to NUM_ENTRIES
    private fun reduceTo(list: List<TsEntry>, reduceTo: Int): List<TsEntry> {
        val num = list.size
        val toRemove = num - reduceTo

        if (toRemove <= 0) {
            return list
        } else {
            val percentToRemove = toRemove / num.toDouble()
            var newList = ArrayList<TsEntry>()

            if (percentToRemove > 0.5) {
                val ith = num / reduceTo
                for (i in 0 until num) {
                    if (i % ith == 0) {
                        newList.add(list[i])
                    }
                }
            }
            else {
                val ith = num / toRemove
                for (i in 0 until num) {
                    if (i % ith != 0) {
                        newList.add(list[i])
                    }
                }
            }

            return newList
        }
    }

    private fun getSensorTs(list: List<TsEntry>, reduced: Boolean): SensorTs {
        var list = list
        if (reduced) {
            list = reduceTo(list, NUM_ENTRIES)
            Log.d(TAG, "Reduced ts to " + list.size + " entries.")
        }
        return SensorTsMapper.createSensorTs(list)
    }
}
