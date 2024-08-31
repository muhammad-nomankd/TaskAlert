package com.durranitech.taskalert.viewmodels

import LocationRetrofitInstance
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.taskalert.dataclasses.location
import com.durranitech.taskalert.models.City
import com.durranitech.taskalert.repositories.SaveLocationRespository
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    var cities by mutableStateOf<List<City>>(emptyList())
        private set

    fun fetchCities(apiKey: String, namePrefix: String) {
        viewModelScope.launch {
            try {
                val response =
                    LocationRetrofitInstance.locationApi.getCities(apiKey, namePrefix = namePrefix)
                cities = response.data
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CityViewModel", "Error fetching cities", e)
            }
        }

    }

    fun saveUserLocation(location: String, locationId: String, country: String,context: Context){
        val loc = location(location = location, locationId = locationId,country = country)

        SaveLocationRespository().saveLocation(loc,context)
    }
}
