package com.example.alarmmanager.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.WeatherResponse
import com.example.alarmmanager.weatherApi.RetroFitInstance
import kotlinx.coroutines.launch

class WeatherViewModel() : ViewModel() {

    var weatherDate : WeatherResponse? = null
        private set

    private val apiKey = "d00134e85867c1394e1b58ef16362488"

    fun fetchWeather(city: String){
        viewModelScope.launch {
            try {
                weatherDate = RetroFitInstance.api.getWeather(city, apiKey)
                Log.d("API Response", "Response: $weatherDate")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API Error", "Error: ${e.message}")
            }
        }
    }
}