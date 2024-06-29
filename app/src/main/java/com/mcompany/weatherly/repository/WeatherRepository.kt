package com.mcompany.weatherly.repository

import com.mcompany.weatherly.server.ApiServices

class WeatherRepository(val api : ApiServices) {

    fun getCurrentWeather(lat: Double, long: Double, unit: String) =
        api.getCurrentWeather(lat, long, unit, "4e31b8a0043ed59e5b0e23bd39c68c53")

    fun getForecastWeather(lat: Double, long: Double, unit: String) =
        api.getForecastWeather(lat, long, unit, "4e31b8a0043ed59e5b0e23bd39c68c53")
}