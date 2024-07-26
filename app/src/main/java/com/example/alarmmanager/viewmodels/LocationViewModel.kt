package com.example.alarmmanager.viewmodels

import LocationRetrofitInstance
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.dataclasses.Data
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    var cities by mutableStateOf<List<Data>>(emptyList())
        private set

    fun fetchCities(apiKey: String, namePrefix: String) {
        var cities = mutableStateOf<List<Data>>(emptyList())


        viewModelScope.launch {
            try {
                delay(1000)
                val response =
                    LocationRetrofitInstance.locationApi.getCities(apiKey, namePrefix = namePrefix)
                cities.value = response.data
                Log.d("CityViewModel", "Cities: ${response}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CityViewModel", "Error fetching cities", e)
            }
        }

    }
}
