package com.example.alarmmanager.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.weatherApi.RetroFitInstance
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _temperature = MutableLiveData<Double>()
    val temperature: LiveData<Double> = _temperature

    private val _weatherIcon = MutableLiveData<String>()
    val weatherIcon: LiveData<String> = _weatherIcon

    private val _weatherDescription = MutableLiveData<String>()
    val weatherDescription: LiveData<String> = _weatherDescription

    private val _weatherHumidity = MutableLiveData<Int>()
    val weatherHumidity: LiveData<Int> = _weatherHumidity

    private val apiKey = "d00134e85867c1394e1b58ef16362488"

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val weatherResponse = RetroFitInstance.api.getWeather(city, apiKey)
                _temperature.value = weatherResponse.main.temp
                _weatherIcon.value = weatherResponse.weather[0].icon
                _weatherDescription.value = weatherResponse.weather[0].description
                _weatherHumidity.value = weatherResponse.main.humidity

                Log.d("API Response", "Temperature: ${weatherDescription.value}")
                Log.d("API Response", "Response: $weatherResponse")
            } catch (e: retrofit2.HttpException) {
                Log.e("API Error", "HTTP error: ${e.message()}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API Error", "Error: ${e.message}")
            }
        }
    }
}
