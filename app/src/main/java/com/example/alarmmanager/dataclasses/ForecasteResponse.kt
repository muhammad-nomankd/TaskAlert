package com.example.alarmmanager.dataclasses

data class ForecasteResponse(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<ForecasteData>,
    val message: Int
)

data class WindF(
    val deg: Int,
    val gust: Double,
    val speed: Double
)

data class WeatherF(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)


data class SysF(
    val pod: String
)

data class RainF(
    val `3h`: Double
)

data class MainF(
    val feels_like: Double,
    val grnd_level: Int,
    val humidity: Int,
    val pressure: Int,
    val sea_level: Int,
    val temp: Double,
    val temp_kf: Double,
    val temp_max: Double,
    val temp_min: Double
)


data class CloudsF(
    val all: Int
)

data class ForecasteData(
    val clouds: CloudsF,
    val dt: Int,
    val dt_txt: String,
    val main: MainF,
    val pop: Double,
    val rain: RainF,
    val sys: SysF,
    val visibility: Int,
    val weather: List<WeatherF>,
    val wind: WindF
)

data class City(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)