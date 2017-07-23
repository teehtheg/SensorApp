package com.teeh.klimasensor;

import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.teeh.klimasensor.common.activities.BaseActivity;
import com.teeh.klimasensor.common.formatter.CustomDateAsXAxisFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.teeh.klimasensor.common.mappers.SimpleTsMapper;
import com.teeh.klimasensor.common.ts.SensorTs;
import com.teeh.klimasensor.common.ts.SimpleEntry;
import com.teeh.klimasensor.common.ts.SimpleTs;
import com.teeh.klimasensor.common.ts.ValueType;
import com.teeh.klimasensor.common.utils.DateUtils;
import com.teeh.klimasensor.common.utils.TsUtil;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by teeh on 30.12.2016.
 */

public class DataVisualizerActivity extends BaseActivity {

    private static final String TAG = "DataVisualizerActivity";

    private static final Class<? extends BaseSeries> POINTS_GRAPH = PointsGraphSeries.class;
    private static final Class<? extends BaseSeries> BAR_GRAPH = BarGraphSeries.class;
    private static final Class<? extends BaseSeries> LINE_GRAPH = LineGraphSeries.class;
    private static final Float POINT_SIZE = 2f;

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String DATA_TYPE = "dataType";

    private TimeseriesService service;
    private SensorTs sensorTs;
    private GraphView graph;

    private Date startDate;
    private Date endDate;
    private int dataType;

    private List<Date> timeLine;

    private static List<Integer> colors;

//    public class TsType {
//        public static final int TEMP = 0;
//        public static final int PRESSURE = 1;
//        public static final int HUMIDITY = 2;
//        public static final int TEMP_COMBINED = 3;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualizer);
    }


    @Override
    public void onStart() {
        super.onStart();

        service = TimeseriesService.getInstance();
        graph = (GraphView) findViewById(R.id.graph);
        initializeGraph();

        Bundle b = getIntent().getExtras();

        // Extract settings
        if(b != null) {
            startDate = DateUtils.toDate(b.getLong(START_DATE));
            endDate = DateUtils.toDate(b.getLong(END_DATE));
            dataType = b.getInt(DATA_TYPE);
        }

        // Fetch Timeseries data
        if (startDate != null && endDate != null) {
            sensorTs = service.getSensorTs(startDate, endDate);
        }
        else {
            sensorTs = service.getSensorTs();
        }

        // populate colors
        Integer[] colorsArray = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
        colors = new ArrayList<>(Arrays.asList(colorsArray));

        // Compose graph
        switch(dataType) {
            case 0:
                createSingleGraph(sensorTs.getTs(ValueType.TEMPERATURE));
                break;
            case 1:
                createSingleGraph(sensorTs.getTs(ValueType.PRESSURE));
                break;
            case 2:
                createSingleGraph(sensorTs.getTs(ValueType.HUMIDITY));
                break;
            case 3:
                createTempMultiGraph();
                break;
            case 4:
                createUniformMultiGraph(sensorTs.getTs(ValueType.HUMIDITY),
                        sensorTs.getTs(ValueType.TEMPERATURE));
                break;
            default:
        }
    }

    private void createSingleGraph(SimpleTs ts) {
        List<DataPoint> list = SimpleTsMapper.fromSimpleTs(ts);
        BaseSeries<DataPoint> series = getSeries(list, POINTS_GRAPH);
        composeGraph(series);
    }

    private void createUniformMultiGraph(SimpleTs... ts) {
        List<BaseSeries<DataPoint>> seriesList = new ArrayList<>();
        for (SimpleTs t : ts) {
            List<DataPoint> list = SimpleTsMapper.fromSimpleTs(t);
            BaseSeries<DataPoint> series = getSeries(list, POINTS_GRAPH);
            seriesList.add(series);
        }

        composeGraph(seriesList);
    }

    private void createTempMultiGraph() {
        List<BaseSeries<DataPoint>> seriesList = new ArrayList<>();

        List<DataPoint> tempList = SimpleTsMapper.fromSimpleTs(sensorTs.getTs(ValueType.TEMPERATURE));
        List<DataPoint> realTempList = SimpleTsMapper.fromSimpleTs(sensorTs.getTs(ValueType.REAL_TEMPERATURE));
        List<DataPoint> tempListShifted = SimpleTsMapper.fromSimpleTs(
                TsUtil.shiftBy(sensorTs.getTs(ValueType.TEMPERATURE), sensorTs.getAvgTempDeviation())
        );

        BaseSeries<DataPoint> tempSeries = getSeries(tempList, POINTS_GRAPH);
        BaseSeries<DataPoint> realTempSeries = getSeries(realTempList, LINE_GRAPH);
        BaseSeries<DataPoint> shiftedMaTempSeries = getMovingAverage(tempListShifted, 20);

        seriesList.add(tempSeries);
        seriesList.add(realTempSeries);
        seriesList.add(shiftedMaTempSeries);

        composeGraph(seriesList);
    }

    /////////////////////
    // Utility methods //
    /////////////////////

    private void composeGraph(BaseSeries<DataPoint>... series) {
        List<BaseSeries<DataPoint>> seriesList = new ArrayList<>();
        for (BaseSeries<DataPoint> serie : series) {
            seriesList.add(serie);
        }
        composeGraph(seriesList);
    }

    private void composeGraph(List<BaseSeries<DataPoint>> seriesList) {
        int iColor = 0;

        for (BaseSeries<DataPoint> series : seriesList) {
            series.setColor(colors.get(iColor));
            iColor = iColor + 1;
        }

        showOnGraph(seriesList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.visualizer_menu, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private BaseSeries<DataPoint> getMovingAverage(List<DataPoint> list, Integer n) {
        List<DataPoint> avgList = new ArrayList<>();
        for (int i = n-1; i < list.size(); i++) {
            double sum = 0;
            double date = list.get(i).getX();
            for (int j = 0; j < n; j++) {
                sum += list.get(i-j).getY();
            }
            double avg = sum / n;
            avgList.add(new DataPoint(date, avg));
        }

        BaseSeries<DataPoint> avgSeries = getSeries(avgList, LineGraphSeries.class);

        return avgSeries;
    }

    private void showOnGraph(BaseSeries<DataPoint> ... allSeries) {
        graph.removeAllSeries();

        for (BaseSeries<DataPoint> series : allSeries) {
            // set manual x bounds to have nice steps
            graph.getViewport().setMinX(series.getLowestValueX());
            graph.getViewport().setMaxX(series.getHighestValueX());
            graph.getViewport().setXAxisBoundsManual(true);

            graph.addSeries(series);
        }
    }

    private void showOnGraph(List<BaseSeries<DataPoint>> allSeries) {
        graph.removeAllSeries();

        for (BaseSeries<DataPoint> series : allSeries) {
            // set manual x bounds to have nice steps
            graph.getViewport().setMinX(series.getLowestValueX());
            graph.getViewport().setMaxX(series.getHighestValueX());
            graph.getViewport().setXAxisBoundsManual(true);

            graph.addSeries(series);
        }
    }

    private void initializeGraph() {
        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.getGridLabelRenderer().setLabelFormatter(new CustomDateAsXAxisFormatter(this));
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(45);
        graph.getGridLabelRenderer().setHumanRounding(false);
    }

    private BaseSeries<DataPoint> getSeries(List<DataPoint> dataPointsList, Class<? extends BaseSeries> clazz) {
        BaseSeries<DataPoint> series = null;
        DataPoint[] dataPoints = new DataPoint[dataPointsList.size()];
        dataPoints = dataPointsList.toArray(dataPoints);

        try {
            Class [] paramTypes = { DataPoint[].class };
            Object [] paramValues = { dataPoints };

            Constructor<?> ctor = clazz.asSubclass(BaseSeries.class).getConstructor();
            Object object = ctor.newInstance();

            if (object instanceof BaseSeries<?>) {
                series = (BaseSeries<DataPoint>)object;
                series.resetData(dataPoints);

                if (series instanceof PointsGraphSeries<?>) {
                    ((PointsGraphSeries) series).setSize(POINT_SIZE);
                }
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return series;
    }

}
