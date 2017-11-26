package com.teeh.klimasensor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils

class DataVisualizerEditorActivity : BaseActivity() {

    private lateinit var rangeSeekbar: CrystalRangeSeekbar
    private lateinit var tsTypeSpinner: Spinner

    private var minDateSlider: Float? = null
    private var maxDateSlider: Float? = null
    private var sliderOffset: Float? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var tsType: Int = 0

    private val onRangeSeekbarChangeListener: OnRangeSeekbarChangeListener
        get() = object : OnRangeSeekbarChangeListener {

            internal val tvMin = findViewById<View>(R.id.lowerDate) as TextView
            internal val tvMax = findViewById<View>(R.id.upperDate) as TextView

            override fun valueChanged(minValue: Number, maxValue: Number) {

                val minDate = minValue as Long + sliderOffset!!.toLong()
                val maxDate = maxValue as Long + sliderOffset!!.toLong()

                tvMin.text = DateUtils.toString(DateUtils.toLocalDate(minDate))
                tvMax.text = DateUtils.toString(DateUtils.toLocalDate(maxDate))

                startDate = minDate
                endDate = maxDate
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

        // here we could use any other ValueType aswell..
        startDate = DateUtils.toLong(TimeseriesService.instance.readFirstFromDB().timestamp)
        endDate = DateUtils.toLong(TimeseriesService.instance.readLastFromDB().timestamp)

        sliderOffset = startDate!! + (endDate!! - startDate!!)/2f

        minDateSlider = startDate!!.toFloat() - sliderOffset!!
        maxDateSlider = endDate!!.toFloat() - sliderOffset!!

        // setup range seekbar
        rangeSeekbar = findViewById<View>(R.id.rangeSeekbar) as CrystalRangeSeekbar
        rangeSeekbar.setOnRangeSeekbarChangeListener(onRangeSeekbarChangeListener)
        rangeSeekbar.setMinValue(minDateSlider!!)
                .setMaxValue(maxDateSlider!!)
                .setMinStartValue(minDateSlider!!)
                .setMaxStartValue(maxDateSlider!!)
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

        // here we could use any other ValueType aswell..
        startDate = DateUtils.toLong(TimeseriesService.instance.readFirstFromDB().timestamp)
        endDate = DateUtils.toLong(TimeseriesService.instance.readLastFromDB().timestamp)

        sliderOffset = startDate!! + (endDate!! - startDate!!)/2f

        minDateSlider = startDate!!.toFloat() - sliderOffset!!
        maxDateSlider = endDate!!.toFloat() - sliderOffset!!

        rangeSeekbar.setMinValue(minDateSlider!!)
                .setMaxValue(maxDateSlider!!)
                .setMinStartValue(minDateSlider!!)
                .setMaxStartValue(maxDateSlider!!)
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

        Log.i(TAG, "Show graph between " + DateUtils.toLocalDate(startDate!!) +"/"+ DateUtils.toDate(startDate!!) +"/"+ startDate + " and " + DateUtils.toLocalDate(endDate!!))

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
