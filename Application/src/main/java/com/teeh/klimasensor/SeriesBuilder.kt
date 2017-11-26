package com.teeh.klimasensor

import android.hardware.camera2.params.TonemapCurve.POINT_SIZE
import android.util.Log
import com.jjoe64.graphview.series.BaseSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import com.teeh.klimasensor.common.exception.BusinessException

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

/**
 * Created by teeh on 13.08.2017.
 */

class SeriesBuilder internal constructor(private val pointSize: Float?) {

    private val TAG = "SeriesBuilder"

    fun getSeries(dataPointsList: List<DataPoint>, clazz: Class<out BaseSeries<*>>): BaseSeries<DataPoint> {
        var dataPoints = dataPointsList.toTypedArray<DataPoint>()

        try {
            val paramTypes = arrayOf<Class<*>>(Array<DataPoint>::class.java)
            val paramValues = arrayOf<Any>(dataPoints)

            val ctor = clazz.asSubclass(BaseSeries::class.java).getConstructor()
            val instance = ctor.newInstance()

            if (instance is BaseSeries<*>) {
                var series: BaseSeries<DataPoint> = instance as BaseSeries<DataPoint>
                series.resetData(dataPoints)

                if (series is PointsGraphSeries<*>) {
                    (series as PointsGraphSeries<*>).size = pointSize!!
                }

                return series
            }
            else {
                throw BusinessException("Unknown series type")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Something serious happened.")
            throw Exception(e)
        }
    }
}
