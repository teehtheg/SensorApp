package com.teeh.klimasensor.weather

import android.content.Context
import android.util.Log

import com.google.android.gms.tasks.Task
import com.teeh.klimasensor.common.config.ConfigService

import java.io.IOException

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by teeh on 23.07.2017.
 */

class OutsideWeatherService private constructor(private val context: Context) {

    private lateinit var weatherCall:Call<CurrentWeather>
    private lateinit var configService:ConfigService

    init {

        val retrofit = Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(CurrentWeatherService::class.java)

        configService = ConfigService(context)

        val API_KEY = configService.get("openweather.api.key")
        val CITY_ID = configService.get("openweather.city.id")

        weatherCall = service.getCurrentWeather(CITY_ID, API_KEY)
    }

    fun getWeather(callback:Callback<CurrentWeather>) {
        weatherCall.enqueue(callback)
    }


    companion object {

        private val TAG = "OutsideWeatherService"

        private val API = "http://api.openweathermap.org/data/2.5/"
        //private static final String API = "http://api.openweathermap.org/data/2.5/weather";

        private var serviceInstance: OutsideWeatherService? = null

        fun getInstance(context: Context): OutsideWeatherService {
            if (serviceInstance == null) {
                serviceInstance = OutsideWeatherService(context)
            }
            return serviceInstance!!
        }
    }


    //http://api.openweathermap.org/data/2.5/weather?id=2657896&APPID=f72974bcbf4a5febe634d6ac606ec57d
}
