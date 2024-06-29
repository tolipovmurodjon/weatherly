package com.mcompany.weatherly.viewmodel

import androidx.lifecycle.ViewModel
import com.mcompany.weatherly.repository.CityRepository
import com.mcompany.weatherly.server.ApiClient
import com.mcompany.weatherly.server.ApiServices

class CityViewModel(val repository: CityRepository) : ViewModel() {
    constructor() : this(CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCity(q: String, limit: Int) =
        repository.getCities(q, limit)
}