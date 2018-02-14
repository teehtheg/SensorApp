package com.teeh.klimasensor.rest

import android.content.Context
import com.teeh.klimasensor.TimeseriesService

import com.teeh.klimasensor.common.config.ConfigService
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.rest.SensorData
import com.teeh.klimasensor.rest.SensorDataServiceInterface

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by teeh on 23.07.2017.
 */

class SensorDataService(private val context: Context) {

    private lateinit var statusCall:Call<ServerStatus>
    private lateinit var sensorDataCall:Call<List<SensorData>>
    private lateinit var sensorDataFromCall:Call<List<SensorData>>
    private var service:SensorDataServiceInterface

    init {
        val configService = ConfigService(context)
        val endpoint = configService.get("piteeh.rest.endpoint")
        val username = configService.get("piteeh.rest.username")
        val password = configService.get("piteeh.rest.password")

        service = ServiceGenerator(endpoint).createService(SensorDataServiceInterface::class.java, username, password)
    }

    fun getSensorDataFrom(callback:Callback<List<SensorData>>) {
        val latestTs = DateUtils.toString(TimeseriesService.instance.readLastFromDB().timestamp)
        sensorDataFromCall = service.getSensorDataFrom(latestTs)
        sensorDataFromCall.enqueue(callback)
    }

    fun getSensorData(callback:Callback<List<SensorData>>) {
        sensorDataCall = service.getSensorData()
        sensorDataCall.enqueue(callback)
    }

    fun getStatus(callback:Callback<ServerStatus>) {
        statusCall = service.getStatus()
        statusCall.enqueue(callback)
    }
}
