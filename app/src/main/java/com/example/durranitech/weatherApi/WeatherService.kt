package com.example.durranitech.weatherApi

import com.example.durranitech.dataclasses.ForecasteResponse
import com.example.durranitech.dataclasses.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather?")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast?")
    suspend fun getForcast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ):ForecasteResponse
}