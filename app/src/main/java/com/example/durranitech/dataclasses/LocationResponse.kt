package com.example.durranitech.models

data class LocationResponse(
    val data: List<City>
)

data class City(
    val id: Int,
    val name: String,
    val country: String,
    val countryCode: String,
    val region: String,
    val regionCode: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int?
)
