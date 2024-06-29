package com.mcompany.weatherly.repository

import com.mcompany.weatherly.server.ApiServices

class CityRepository(val api: ApiServices) {

    fun getCities(q: String, limit: Int) =
        api.getCitiesList(q, limit, "4e31b8a0043ed59e5b0e23bd39c68c53")
}