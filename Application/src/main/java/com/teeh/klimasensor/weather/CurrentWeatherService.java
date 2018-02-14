package com.teeh.klimasensor.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by teeh on 23.07.2017.
 */

public interface CurrentWeatherService {
    @GET("weather/")
    Call<CurrentWeather> getCurrentWeather(@Query("id") String cityId, @Query("APPID") String apiKey);
}

