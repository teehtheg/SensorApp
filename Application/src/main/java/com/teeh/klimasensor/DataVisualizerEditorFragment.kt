package com.teeh.klimasensor

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.teeh.klimasensor.common.extension.bind
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.database.DatabaseService
import com.teeh.klimasensor.databinding.FragmentDataVisualizerEditorBinding

/**
 * Created by teeh on 21.06.2017.
 */

class DataVisualizerEditorFragment : Fragment() {

    private val rangeSeekbar: CrystalRangeSeekbar by bind(R.id.rangeSeekbar)

    private lateinit var tsTypeSpinner: Spinner

    private var minDate: Long? = null
    private var maxDate: Long? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var tsType: Int = 0

    private val onRangeSeekbarChangeListener: OnRangeSeekbarChangeListener
        get() = object : OnRangeSeekbarChangeListener {

            internal val tvMin = activity!!.findViewById<View>(R.id.lowerDate) as TextView
            internal val tvMax = activity!!.findViewById<View>(R.id.upperDate) as TextView

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentDataVisualizerEditorBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_data_visualizer_editor, container, false)
        binding.editor = this
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        maxDate = DateUtils.toLong(DatabaseService.instance.latestEntry.timestamp)
        minDate = DateUtils.toLong(DatabaseService.instance.oldestEntry.timestamp)

        val minDateAdjusted = 0
        val maxDateAdjusted = maxDate!! - minDate!!

        // setup range seekbar
        rangeSeekbar.setOnRangeSeekbarChangeListener(onRangeSeekbarChangeListener)
        rangeSeekbar.setMinValue(minDateAdjusted.toFloat())
                .setMaxValue(maxDateAdjusted.toFloat())
                .setMinStartValue(minDateAdjusted.toFloat())
                .setMaxStartValue(maxDateAdjusted.toFloat())
                .setDataType(CrystalSeekbar.DataType.LONG)
                .apply()

        // setup dataType selector
        tsTypeSpinner = activity!!.findViewById<View>(R.id.tsTypes) as Spinner
        tsTypeSpinner.onItemSelectedListener = onItemSelectedListener
        val adapter = ArrayAdapter.createFromResource(activity!!, R.array.dataTypes, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tsTypeSpinner.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        maxDate = DateUtils.toLong(DatabaseService.instance.latestEntry.timestamp)
        minDate = DateUtils.toLong(DatabaseService.instance.oldestEntry.timestamp)

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

        val transaction = fragmentManager!!.beginTransaction()
        val fragment = DataVisualizerFragment()
        fragment.startDate = DateUtils.toLocalDate(startDate!!)
        fragment.endDate = DateUtils.toLocalDate(endDate!!)
        fragment.dataType = tsType
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    companion object {
        private val TAG = "DataVisualizerEditor"
    }

}
