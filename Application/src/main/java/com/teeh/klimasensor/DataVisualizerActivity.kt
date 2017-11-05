package com.teeh.klimasensor

import android.graphics.Color
import android.os.Bundle
import android.view.Menu

import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.BaseSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.formatter.CustomDateAsXAxisFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.teeh.klimasensor.common.mappers.SimpleTsMapper
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.SimpleTs
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.common.utils.TsUtil

import android.util.Log
import com.teeh.klimasensor.common.exception.BusinessException

import java.time.LocalDateTime
import java.util.*


/**
 * Created by teeh on 30.12.2016.
 */

class DataVisualizerActivity : BaseActivity() {

    private val TAG = "DataVisualizerActivity"

    private val POINTS_GRAPH = PointsGraphSeries::class.java
    private val BAR_GRAPH = BarGraphSeries::class.java
    private val LINE_GRAPH = LineGraphSeries::class.java
    private val POINT_SIZE = 2f

    companion object {
        val START_DATE = "startDate"
        val END_DATE = "endDate"
        val DATA_TYPE = "dataType"
    }

    private lateinit var colors: List<Int>

    private lateinit var service: TimeseriesService
    private lateinit var sensorTs: SensorTs
    private lateinit var graph: GraphView

    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var dataType: Int = 0

    //    public class TsType {
    //        public static final int TEMP = 0;
    //        public static final int PRESSURE = 1;
    //        public static final int HUMIDITY = 2;
    //        public static final int TEMP_COMBINED = 3;
    //    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_visualizer)
    }


    public override fun onStart() {
        super.onStart()

        service = TimeseriesService.instance
        graph = findViewById<GraphView>(R.id.graph)
        initializeGraph()

        val b = intent.extras

        // Extract settings
        if (b != null) {
            startDate = DateUtils.toDate(b.getLong(START_DATE))
            endDate = DateUtils.toDate(b.getLong(END_DATE))
            dataType = b.getInt(DATA_TYPE)
        }

        // Fetch Timeseries data
        if (startDate != null && endDate != null) {
            sensorTs = service.getSensorTs(startDate, endDate)
        } else {
            sensorTs = service.sensorTs
        }

        // populate colors
        val colorsArray = arrayOf(Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA)
        colors = ArrayList(Arrays.asList(*colorsArray))

        // Compose graph
        when (dataType) {
            0 -> createSingleGraph(sensorTs.getTs(ValueType.TEMPERATURE))
            1 -> createSingleGraph(sensorTs.getTs(ValueType.PRESSURE))
            2 -> createSingleGraph(sensorTs.getTs(ValueType.HUMIDITY))
            3 -> createTempMultiGraph()
            4 -> createUniformMultiGraph(sensorTs.getTs(ValueType.HUMIDITY),
                    sensorTs.getTs(ValueType.TEMPERATURE))
        }
    }

    private fun createSingleGraph(ts: SimpleTs) {
        val list = SimpleTsMapper.fromSimpleTs(ts)
        val series = getSeries(list, POINTS_GRAPH)
        composeGraph(series)
    }

    private fun createUniformMultiGraph(vararg ts: SimpleTs) {
        val seriesList = ArrayList<BaseSeries<DataPoint>>()
        for (t in ts) {
            val list = SimpleTsMapper.fromSimpleTs(t)
            val series = getSeries(list, POINTS_GRAPH)
            seriesList.add(series)
        }

        composeGraph(seriesList)
    }

    private fun createTempMultiGraph() {
        val seriesList = ArrayList<BaseSeries<DataPoint>>()

        val tempList = SimpleTsMapper.fromSimpleTs(sensorTs.getTs(ValueType.TEMPERATURE))
        val realTempList = SimpleTsMapper.fromSimpleTs(sensorTs.getTs(ValueType.REAL_TEMPERATURE))
        val tempListShifted = SimpleTsMapper.fromSimpleTs(
                TsUtil.shiftBy(sensorTs.getTs(ValueType.TEMPERATURE), sensorTs.avgTempDeviation!!)
        )

        val tempSeries = getSeries(tempList, POINTS_GRAPH)
        val realTempSeries = getSeries(realTempList, LINE_GRAPH)
        val shiftedMaTempSeries = getMovingAverage(tempListShifted, 20)

        seriesList.add(tempSeries)
        seriesList.add(realTempSeries)
        seriesList.add(shiftedMaTempSeries)

        composeGraph(seriesList)
    }

    /////////////////////
    // Utility methods //
    /////////////////////

    private fun composeGraph(vararg series: BaseSeries<DataPoint>) {
        val seriesList = ArrayList<BaseSeries<DataPoint>>()
        for (serie in series) {
            seriesList.add(serie)
        }
        composeGraph(seriesList)
    }

    private fun composeGraph(seriesList: List<BaseSeries<DataPoint>>) {
        var iColor = 0

        for (series in seriesList) {
            series.color = colors[iColor]
            iColor = iColor + 1
        }

        showOnGraph(seriesList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.visualizer_menu, menu)
        return true
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()
    }


    //
    //    private void showTemperature() {
    //
    //        if (sensorTs == null) {
    //            Toast.makeText(this, R.string.empty_data, Toast.LENGTH_SHORT).show();
    //            return;
    //        }
    //
    //        List<DataPoint> tempList = new ArrayList<>();
    //        List<DataPoint> adjustedTempList = new ArrayList<>();
    //
    //        Double deviation = sensorTs.getAvgTempDeviation();
    //        Log.d(TAG, "Deviation: " + deviation);
    //
    //        int i = 0;
    //        for (TsEntry entry : sensorTs.getAllTs())  {
    //            tempList.add(new DataPoint(entry.getTimestamp(), entry.getTemperature()));
    //            Double adjustedTemp = deviation + entry.getTemperature();
    //            adjustedTempList.add(new DataPoint(entry.getTimestamp(), adjustedTemp));
    //        }
    //
    //        BaseSeries<DataPoint> adjustedTempSeries = getSeries(adjustedTempList, SERIES_TYPE);
    //        BaseSeries<DataPoint> tempSeries = getSeries(tempList, SERIES_TYPE);
    //
    //        tempSeries.setColor(Color.BLUE );
    //        adjustedTempSeries.setColor(Color.GREEN );
    //
    //        Log.d(TAG, "highest adjusted value: " + String.valueOf(adjustedTempSeries.getHighestValueY()));
    //        Log.d(TAG, "lowest adjusted value: " + String.valueOf(adjustedTempSeries.getLowestValueY()));
    //
    //        BaseSeries<DataPoint> tempMA = getMovingAverage(tempList, 20);
    //        tempMA.setColor(Color.RED);
    //
    //        showOnGraph(tempSeries, adjustedTempSeries);
    //    }

    //////////////////////
    // Helper functions //
    //////////////////////

    private fun getMovingAverage(list: List<DataPoint>, n: Int?): BaseSeries<DataPoint> {
        val avgList = ArrayList<DataPoint>()
        for (i in n!! - 1 until list.size) {
            var sum = 0.0
            val date = list[i].x
            for (j in 0 until n) {
                sum += list[i - j].y
            }
            val avg = sum / n
            avgList.add(DataPoint(date, avg))
        }

        return getSeries(avgList, LineGraphSeries::class.java)
    }

    private fun showOnGraph(vararg allSeries: BaseSeries<DataPoint>) {
        graph.removeAllSeries()

        for (series in allSeries) {
            // set manual x bounds to have nice steps
            graph.viewport.setMinX(series.lowestValueX)
            graph.viewport.setMaxX(series.highestValueX)
            graph.viewport.isXAxisBoundsManual = true

            graph.addSeries(series)
        }
    }

    private fun showOnGraph(allSeries: List<BaseSeries<DataPoint>>) {
        graph.removeAllSeries()

        for (series in allSeries) {
            // set manual x bounds to have nice steps
            graph.viewport.setMinX(series.lowestValueX)
            graph.viewport.setMaxX(series.highestValueX)
            graph.viewport.isXAxisBoundsManual = true

            graph.addSeries(series)
        }
    }

    private fun initializeGraph() {
        // enable scaling and scrolling
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)

        graph.gridLabelRenderer.labelFormatter = CustomDateAsXAxisFormatter(this)
        graph.gridLabelRenderer.setHorizontalLabelsAngle(45)
        graph.gridLabelRenderer.isHumanRounding = false
    }

    private fun getSeries(dataPointsList: List<DataPoint>, clazz: Class<out BaseSeries<*>>): BaseSeries<DataPoint> {
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
                    (series as PointsGraphSeries<*>).size = POINT_SIZE
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
