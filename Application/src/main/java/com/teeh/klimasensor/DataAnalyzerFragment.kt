package com.teeh.klimasensor

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.extension.bind
import com.teeh.klimasensor.common.ts.SensorTs
import com.teeh.klimasensor.common.ts.ValueType

import java.text.NumberFormat
import java.util.HashMap
import java.util.Locale

import com.teeh.klimasensor.database.DatabaseService
import com.teeh.klimasensor.weather.CurrentWeather
import com.teeh.klimasensor.weather.OutsideWeatherService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataAnalyzerFragment : Fragment() {

    private lateinit var sensorTs: SensorTs

    private lateinit var nf: NumberFormat

    private val outside_temp: TextView by bind(R.id.outside_temp)

    private val humidity_lat: TextView by bind(R.id.humidity_lat)
    private val humidity_min: TextView by bind(R.id.humidity_min)
    private val humidity_max: TextView by bind(R.id.humidity_max)
    private val humidity_avg: TextView by bind(R.id.humidity_avg)
    private val humidity_med: TextView by bind(R.id.humidity_med)

    private val temperature_lat: TextView by bind(R.id.temperature_lat)
    private val temperature_min: TextView by bind(R.id.temperature_min)
    private val temperature_max: TextView by bind(R.id.temperature_max)
    private val temperature_avg: TextView by bind(R.id.temperature_avg)
    private val temperature_med: TextView by bind(R.id.temperature_med)
    private val temperature_dev: TextView by bind(R.id.temperature_dev)

    private val pressure_lat: TextView by bind(R.id.pressure_lat)
    private val pressure_min: TextView by bind(R.id.pressure_min)
    private val pressure_max: TextView by bind(R.id.pressure_max)
    private val pressure_avg: TextView by bind(R.id.pressure_avg)
    private val pressure_med: TextView by bind(R.id.pressure_med)

    private val realTempInput: EditText by bind(R.id.input_real_temp)

    private val tsTypes: List<ValueType> = listOf(ValueType.HUMIDITY, ValueType.TEMPERATURE, ValueType.PRESSURE)
    private val typeDisplayMapping = HashMap<ValueType, List<TextView>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_data_analyzer, container, false)
    }

    public override fun onStart() {
        super.onStart()


        nf = NumberFormat.getInstance(Locale.GERMAN)
        nf.maximumFractionDigits = 2

        typeDisplayMapping.put(ValueType.HUMIDITY, listOf(humidity_lat, humidity_min, humidity_max, humidity_avg, humidity_med))
        typeDisplayMapping.put(ValueType.TEMPERATURE, listOf(temperature_lat, temperature_min, temperature_max, temperature_avg, temperature_med))
        typeDisplayMapping.put(ValueType.PRESSURE, listOf(pressure_lat, pressure_min, pressure_max, pressure_avg, pressure_med))
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()

        val weatherService = OutsideWeatherService(activity!!)
        weatherService.getWeather(getWeatherCallback())

        launch(UI) {
            sensorTs = TimeseriesService.instance.sensorTs.await()
            initializeKeyfigures()
            temperature_dev.text = nf.format(sensorTs.avgTempDeviation)
        }
    }


    private fun initializeKeyfigures() {
        for ((key, values) in typeDisplayMapping) {
            // this order here is depending on the order of how they're
            // added to typeDisplayMapping!
            setLatest(key, values.get(0), nf)
            setMin(key, values.get(1), nf)
            setMax(key, values.get(2), nf)
            setAvg(key, values.get(3), nf)
            setMedian(key, values.get(4), nf)

        }
    }

    private fun setLatest(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).latestValue)
    }

    private fun setMin(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).min)
    }

    private fun setMax(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).max)
    }

    private fun setAvg(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).avg)
    }

    private fun setMedian(type: ValueType, view: TextView, nf: NumberFormat) {
        view.text = nf.format(sensorTs.getTs(type).median)
    }

    fun addRealTemp(view: View) {
        val editable = realTempInput.text
        val input = editable.toString()
        var success = false
        val entry = DatabaseService.instance.latestEntry

        if (input != null && entry != null) {
            val value = java.lang.Double.valueOf(input)
            success = DatabaseService.instance.insertRealTemp(value, entry.id)
        }

        if (success) {
            realTempInput.setText("")
            Toast.makeText(activity!!, "Value added!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getWeatherCallback():Callback<CurrentWeather> {
        return object : Callback<CurrentWeather> {
            override fun onResponse(call: Call<CurrentWeather>, response: Response<CurrentWeather>) {
                if (response.isSuccessful) {
                    Log.i(TAG, response.toString())
                    // tasks available
                    val weather = response.body()!!
                    outside_temp.setText(nf.format(weather.main!!.temp!! - 273.15))
                } else {
                    // error response, no access to resource?
                }
            }

            override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.message)
            }
        }
    }

    companion object {
        private val TAG = "DataAnalyzerActivity"
    }
}
