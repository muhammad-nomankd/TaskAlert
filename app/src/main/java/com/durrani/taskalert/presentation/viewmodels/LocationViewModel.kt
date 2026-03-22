package com.durrani.taskalert.presentation.viewmodels

import LocationRetrofitInstance
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durrani.taskalert.domain.models.location
import com.durrani.taskalert.models.City
import com.durrani.taskalert.data.repository.SaveLocationRespository
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
