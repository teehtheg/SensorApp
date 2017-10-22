package com.teeh.klimasensor.bluetooth

import android.os.AsyncTask
import com.teeh.klimasensor.TimeseriesService

class WriteToDBTask : AsyncTask<List<String>, Void, Boolean>() {

    override fun doInBackground(vararg update: List<String>): Boolean? {
        for (elem in update) {
            TimeseriesService.instance.writeToDB(elem)
        }
        return true
    }

    override fun onPostExecute(result: Boolean?) {

    }
}