package com.example.alarmmanager.dataclasses

data class LocationResponse(
    val `data`: List<Data>
)

data class Data(
    val countryCode: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val stateCode: String
)
