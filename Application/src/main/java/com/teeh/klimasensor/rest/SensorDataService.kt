package com.teeh.klimasensor.rest

import android.content.Context

import com.teeh.klimasensor.common.config.ConfigService

import retrofit2.Call
import retrofit2.Callback

/**
 * Created by teeh on 23.07.2017.
 */

class SensorDataService(private val context: Context) {

    private lateinit var statusCall:Call<ServerStatus>
    private lateinit var sensorDataCountCall:Call<SensorDataCountResponse>
    private lateinit var sensorDataCall:Call<SensorDataResponse>
    private lateinit var sensorDataFromCountCall:Call<SensorDataCountResponse>
    private lateinit var sensorDataFromCall:Call<SensorDataResponse>
    private var service:SensorDataServiceInterface

    init {
        val configService = ConfigService(context)
        val teehTrustManager = SslSetupUtil(context)

        val endpoint = configService.get("piteeh.rest.endpoint")
        val username = configService.get("piteeh.rest.username")
        val password = configService.get("piteeh.rest.password")

        service = ServiceGenerator(endpoint, teehTrustManager).createService(SensorDataServiceInterface::class.java, username, password)
    }

    fun getSensorDataFromCount(callback:Callback<SensorDataCountResponse>, latestTs: String) {
        sensorDataFromCountCall = service.getSensorDataFromCount(latestTs)
        sensorDataFromCountCall.enqueue(callback)
    }

    fun getSensorDataFrom(callback:Callback<SensorDataResponse>, latestTs: String, pageNr: Int) {
        sensorDataFromCall = service.getSensorDataFrom(latestTs, pageNr)
        sensorDataFromCall.enqueue(callback)
    }

    fun getSensorDataCount(callback:Callback<SensorDataCountResponse>) {
        sensorDataCountCall = service.getSensorDataCount()
        sensorDataCountCall.enqueue(callback)
    }

    fun getSensorData(callback:Callback<SensorDataResponse>, pageNr: Int) {
        sensorDataCall = service.getSensorData(pageNr)
        sensorDataCall.enqueue(callback)
    }

    fun getStatus(callback:Callback<ServerStatus>) {
        statusCall = service.getStatus()
        statusCall.enqueue(callback)
    }
}
