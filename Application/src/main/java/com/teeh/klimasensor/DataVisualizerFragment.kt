package com.teeh.klimasensor

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup

import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.BaseSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.formatter.CustomDateAsXAxisFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.teeh.klimasensor.R.id.graph
import com.teeh.klimasensor.common.extension.bind
import com.teeh.klimasensor.common.mappers.SimpleTsMapper
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.SimpleTs
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.common.utils.TsUtil

import com.teeh.klimasensor.common.utils.CurveFittingUtil
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.time.LocalDateTime

import java.util.*

class DataVisualizerFragment : Fragment() {

    private val TAG = "DataVisualizerFragment"

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

    private lateinit var seriesBuilder: SeriesBuilder
    private lateinit var service: TimeseriesService
    private lateinit var sensorTs: SensorTs
    private lateinit var graph: GraphView

    private val appbar: AppBarLayout by bind(R.id.id_appbar)

    public lateinit var startDate: LocalDateTime
    public lateinit var endDate: LocalDateTime
    public var dataType: Int = 0

    //    public class TsType {
    //        public static final int TEMP = 0;
    //        public static final int PRESSURE = 1;
    //        public static final int HUMIDITY = 2;
    //        public static final int TEMP_COMBINED = 3;
    //    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_data_visualizer, container, false)
    }

    public override fun onStart() {
        super.onStart()

        setMenuVisibility(false)
        appbar.setExpanded(false, true)

        seriesBuilder = SeriesBuilder(POINT_SIZE)
        service = TimeseriesService.instance
        graph = activity!!.findViewById<GraphView>(R.id.graph)
        initializeGraph()

        Log.i(TAG, "Show graph from $startDate until $endDate for dataType $dataType")

        // Fetch Timeseries data
        async(UI) {
            sensorTs = service.getSensorTsReducedAsync(startDate, endDate).await()

            Log.i(TAG, "Sucessfully fetched the following entries: ${sensorTs.getTs(ValueType.TEMPERATURE).ts}")

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
                5 -> createGraphAndFit(sensorTs.getTs(ValueType.TEMPERATURE))
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        graph.removeAllSeries()
        graph.visibility = View.GONE
    }

    public override fun onResume() {
        super.onResume()
    }

    private fun createSingleGraph(ts: SimpleTs) {
        val list = SimpleTsMapper.fromSimpleTs(ts)
        val series = seriesBuilder.getSeries(list, POINTS_GRAPH)
        composeGraph(series)
    }

    private fun createUniformMultiGraph(vararg ts: SimpleTs) {
        val seriesList = ArrayList<BaseSeries<DataPoint>>()
        for (t in ts) {
            val list = SimpleTsMapper.fromSimpleTs(t)
            val series = seriesBuilder.getSeries(list, POINTS_GRAPH)
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

        val tempSeries = seriesBuilder.getSeries(tempList, POINTS_GRAPH)
        val realTempSeries = seriesBuilder.getSeries(realTempList, LINE_GRAPH)
        val shiftedMaTempSeries = seriesBuilder.getSeries(CurveFittingUtil.getMovingAverage(tempListShifted, 20), LINE_GRAPH);

        seriesList.add(tempSeries)
        seriesList.add(realTempSeries)
        seriesList.add(shiftedMaTempSeries)

        composeGraph(seriesList)
    }

    private fun createGraphAndFit(ts: SimpleTs) {
        val seriesList = ArrayList<BaseSeries<DataPoint>>()
        val shift = 0
        val weights = CurveFittingUtil.getNormalWeightVector(10)

        val measurement = SimpleTsMapper.fromSimpleTs(ts)

        val measurementSeries = seriesBuilder.getSeries(measurement, POINTS_GRAPH)
        val fitSeries = seriesBuilder.getSeries(CurveFittingUtil.getWeightedMovingAverage(measurement, weights, shift), LINE_GRAPH)

        seriesList.add(measurementSeries)
        seriesList.add(fitSeries)

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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.visualizer_menu, menu)
//        return true
//    }

    //////////////////////
    // Helper functions //
    //////////////////////

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

        graph.visibility = View.VISIBLE
    }

    private fun initializeGraph() {
        // enable scaling and scrolling
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)

        graph.gridLabelRenderer.labelFormatter = CustomDateAsXAxisFormatter(activity!!)
        graph.gridLabelRenderer.setHorizontalLabelsAngle(45)
        graph.gridLabelRenderer.isHumanRounding = false
    }
}
