package com.mcompany.weatherly.viewmodel

import androidx.lifecycle.ViewModel
import com.mcompany.weatherly.repository.WeatherRepository
import com.mcompany.weatherly.server.ApiClient
import com.mcompany.weatherly.server.ApiServices

class WeatherViewModel(val repository: WeatherRepository) : ViewModel() {

    constructor() : this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCurrentWeather(lat: Double, long: Double, unit: String) = repository.getCurrentWeather(lat, long, unit)

    fun loadForecastWeather(lat: Double, long: Double, unit: String) = repository.getForecastWeather(lat, long, unit)

}