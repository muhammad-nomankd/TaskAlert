package com.durranitech.taskalert.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.taskalert.modelclasses.ForecasteData
import com.durranitech.taskalert.weatherApi.RetroFitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _isLoading  = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _dataFetched  = MutableStateFlow<Boolean>(false)
    val dataFetched: StateFlow<Boolean> get() = _isLoading

    private val _temperature = MutableLiveData<Double>()
    val temperature: LiveData<Double> get()= _temperature

    private val _weatherDescription = MutableLiveData<String>()
    val weatherDescription: LiveData<String> get()= _weatherDescription

    private val _weatherHumidity = MutableLiveData<Int>()
    val weatherHumidity: LiveData<Int> get() = _weatherHumidity

    private val _fiveDaysWeatherList = MutableStateFlow<List<ForecasteData>>(emptyList())
    val fiveDaysWeatherList: StateFlow<List<ForecasteData>>  get() = _fiveDaysWeatherList

    private val apiKey = "d00134e85867c1394e1b58ef16362488"

    fun fetchWeather(city: String) {
        if (_isLoading.value || (_dataFetched.value && _fiveDaysWeatherList.value.isNotEmpty())){
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val weatherResponse = RetroFitInstance.api.getWeather(city, apiKey)
                _temperature.value = weatherResponse.main.temp
                "https://openweathermap.org/img/wn/${weatherResponse.weather.firstOrNull()?.icon}@2x.png"
                _weatherDescription.value = weatherResponse.weather[0].description
                _weatherHumidity.value = weatherResponse.main.humidity
                _dataFetched.value = true

            } catch (e: retrofit2.HttpException) {
                Log.e("API Error", "HTTP error: ${e.message()}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API Error", "Error: ${e.message}")
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchForecast(city: String) {

        if (_isLoading.value || (_dataFetched.value && _fiveDaysWeatherList.value.isNotEmpty())){
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val forcastResponse = RetroFitInstance.forcastApi.getForcast(city, apiKey)
                _fiveDaysWeatherList.value = forcastResponse.list
                _dataFetched.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API Error", "Error: ${e.message}")
            }finally {
                _isLoading.value = false
            }


        }
    }
}
