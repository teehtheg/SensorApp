package com.teeh.klimasensor.rest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by teeh on 23.07.2017.
 */

public interface SensorDataServiceInterface {
    @GET("status")
    Call<ServerStatus> getStatus();

    @GET("sensordata")
    Call<List<SensorData>> getSensorData();

    @GET("sensordata/{fromTs}")
    Call<List<SensorData>> getSensorDataFrom(@Path("fromTs") String fromTs);
}

