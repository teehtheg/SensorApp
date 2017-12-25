package com.teeh.klimasensor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.teeh.klimasensor.DataVisualizerEditorActivity.Companion.TAG
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils

import java.util.Date

/**
 * Created by teeh on 21.06.2017.
 */

class DataVisualizerEditorActivity : BaseActivity() {

    private lateinit var rangeSeekbar: CrystalRangeSeekbar
    private lateinit var tsTypeSpinner: Spinner
    private lateinit var sensorTs: SensorTs

    private var minDate: Long? = null
    private var maxDate: Long? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var tsType: Int = 0

    private val onRangeSeekbarChangeListener: OnRangeSeekbarChangeListener
        get() = object : OnRangeSeekbarChangeListener {

            internal val tvMin = findViewById<View>(R.id.lowerDate) as TextView
            internal val tvMax = findViewById<View>(R.id.upperDate) as TextView

            override fun valueChanged(minValue: Number, maxValue: Number) {
                startDate = minValue as Long + minDate!!
                endDate = maxValue as Long + minDate!!

                tvMin.text = DateUtils.toString(DateUtils.toDate(startDate!!))
                tvMax.text = DateUtils.toString(DateUtils.toDate(endDate!!))
            }

        }

    private val onItemSelectedListener: AdapterView.OnItemSelectedListener
        get() = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position) as String
                Log.d(TAG, value)

                tsType = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_visualizer_editor)
    }

    override fun onStart() {
        super.onStart()

        // get range limits
        sensorTs = TimeseriesService.instance.sensorTs
        // here we could use any other ValueType aswell..
        maxDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).latestTimestamp)
        minDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).firstTimestamp)

        val minDateAdjusted = 0
        val maxDateAdjusted = maxDate!! - minDate!!

        Log.i(TAG, "minDate: ${sensorTs.getTs(ValueType.TEMPERATURE).firstTimestamp}")
        Log.i(TAG, "maxDate: ${sensorTs.getTs(ValueType.TEMPERATURE).latestTimestamp}")

        // setup range seekbar
        rangeSeekbar = findViewById<View>(R.id.rangeSeekbar) as CrystalRangeSeekbar
        rangeSeekbar.setOnRangeSeekbarChangeListener(onRangeSeekbarChangeListener)
        rangeSeekbar.setMinValue(minDateAdjusted.toFloat())
                .setMaxValue(maxDateAdjusted.toFloat())
                .setMinStartValue(minDateAdjusted.toFloat())
                .setMaxStartValue(maxDateAdjusted.toFloat())
                .setDataType(CrystalSeekbar.DataType.LONG)
                .apply()

        // setup dataType selector
        tsTypeSpinner = findViewById<View>(R.id.tsTypes) as Spinner
        tsTypeSpinner.onItemSelectedListener = onItemSelectedListener
        val adapter = ArrayAdapter.createFromResource(this, R.array.dataTypes, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tsTypeSpinner.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        maxDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).latestTimestamp)
        minDate = DateUtils.toLong(sensorTs.getTs(ValueType.TEMPERATURE).firstTimestamp)

        val minDateAdjusted = 0
        val maxDateAdjusted = maxDate!! - minDate!!

        rangeSeekbar.setMinValue(minDateAdjusted.toFloat())
                .setMaxValue(maxDateAdjusted.toFloat())
                .setMinStartValue(minDateAdjusted.toFloat())
                .setMaxStartValue(maxDateAdjusted.toFloat())
                .setDataType(CrystalSeekbar.DataType.LONG)
                .apply()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun showGraph(view: View) {
        val serverIntent = Intent(this, DataVisualizerActivity::class.java)

        val b = Bundle()
        b.putLong(DataVisualizerActivity.START_DATE, startDate!!)
        b.putLong(DataVisualizerActivity.END_DATE, endDate!!)
        b.putInt(DataVisualizerActivity.DATA_TYPE, tsType)
        serverIntent.putExtras(b)
        startActivity(serverIntent)
    }

    companion object {

        private val TAG = "DataVisualizerEditor"
    }

}
