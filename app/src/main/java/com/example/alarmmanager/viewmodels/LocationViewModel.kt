package com.example.alarmmanager.viewmodels

import LocationRetrofitInstance
import android.provider.ContactsContract.RawContacts.Data
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmanager.models.City
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    var cities by mutableStateOf<List<City>>(emptyList())
        private set

    fun fetchCities(apiKey: String, namePrefix: String) {
        viewModelScope.launch {
            try {
                delay(1000)
                val response =
                    LocationRetrofitInstance.locationApi.getCities(apiKey, namePrefix = namePrefix)
                cities = response.data
                Log.d("CityViewModel", "Cities: ${cities}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CityViewModel", "Error fetching cities", e)
            }
        }

    }
}
