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

    @GET("sensordata/count")
    Call<SensorDataCountResponse> getSensorDataCount();

    @GET("sensordata/{pageNr}")
    Call<SensorDataResponse> getSensorData(@Path("pageNr") Integer pageNr);

    @GET("sensordatafrom/{fromTs}/count")
    Call<SensorDataCountResponse> getSensorDataFromCount(@Path("fromTs") String fromTs);

    @GET("sensordatafrom/{fromTs}/{pageNr}")
    Call<SensorDataResponse> getSensorDataFrom(@Path("fromTs") String fromTs, @Path("pageNr") Integer pageNr);
}

