package com.mcompany.weatherly.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.matteobattilana.weather.PrecipType
import com.mcompany.weatherly.R
import com.mcompany.weatherly.adapter.ForecastAdapter
import com.mcompany.weatherly.databinding.ActivityMainBinding
import com.mcompany.weatherly.model.CurrentResponseApi
import com.mcompany.weatherly.model.ForecastResponseApi
import com.mcompany.weatherly.viewmodel.WeatherViewModel
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val forecastAdapter by lazy { ForecastAdapter() }
    private var currentEpoch: Int? = null
    private var sunriseEpoch: Int? = null
    private var sunsetEpoch: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            val lat: Double
            val lon: Double
            val name: String?

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            if (intent.hasExtra("lat") && intent.hasExtra("lon") && intent.hasExtra("name")) {
                lat = intent.getDoubleExtra("lat", 42.3601)
                lon = intent.getDoubleExtra("lon", -71.0589)
                name = intent.getStringExtra("name")

                editor.putFloat("lat", lat.toFloat())
                editor.putFloat("lon", lon.toFloat())
                editor.putString("name", name)
                editor.apply()
            } else {
                lat = sharedPreferences.getFloat("lat", 42.3601F).toDouble()
                lon = sharedPreferences.getFloat("lon", -71.0589F).toDouble()
                name = sharedPreferences.getString("name", "Boston")
            }



            addCity.setOnClickListener {
                startActivity(Intent(this@MainActivity, CityActivity::class.java))
            }


            //current Temp
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(lat, lon, "metric").enqueue(object :
                Callback<CurrentResponseApi> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<CurrentResponseApi>,
                    response: Response<CurrentResponseApi>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE
                        data?.let {

                            if (name == "None"){
                                cityTxt.text = "${it.name}"
                            }else{
                                cityTxt.text = name
                            }

                            statusTxt.text = it.weather?.get(0)?.main ?: "-"

                            windTxt.text = it.wind?.speed?.roundToInt().toString() + " km"
                            humidityTxt.text = it.main?.humidity?.toString() + "%"
                            currentTempTxt.text = it.main?.temp?.roundToInt().toString() + "°"
                            maxTempTxt.text = it.main?.tempMax?.roundToInt().toString() + "°"
                            minTempTxt.text = it.main?.tempMin?.roundToInt().toString() + "°"

                            currentEpoch = it.dt
                            sunriseEpoch = it.sys?.sunrise
                            sunsetEpoch = it.sys?.sunset


                            val drawable = if (isNightNow(currentEpoch, sunriseEpoch, sunsetEpoch)) {
                                R.drawable.night_bg
                            } else {
                                setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                            }


                            bgImage.setImageResource(drawable)
                            setEffectRainSnow(it.weather?.get(0)?.icon ?: "-")
                        }
                    }
                }

                override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }

            })

            val radius = 10f
            val decorView = window.decorView
            val rootView = (decorView.findViewById(android.R.id.content) as ViewGroup?)
            val windowBackground = decorView.background

            rootView?.let {
                blurView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true
            }


            //forecast temp
            weatherViewModel.loadForecastWeather(lat, lon, "metric")
                .enqueue(object : Callback<ForecastResponseApi> {
                    override fun onResponse(
                        call: Call<ForecastResponseApi>,
                        response: Response<ForecastResponseApi>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            blurView.visibility = View.VISIBLE

                            data?.let {
                                forecastAdapter.differ.submitList(it.list)
                                forecastView.apply {
                                    layoutManager = LinearLayoutManager(
                                        this@MainActivity,
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                                    adapter = forecastAdapter
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                })


        }


    }

    private fun isNightNow(currentEpoch: Int?, sunriseEpoch: Int?, sunsetEpoch: Int?): Boolean {
        if (currentEpoch != null && sunriseEpoch != null && sunsetEpoch != null) {
            return currentEpoch >= sunsetEpoch || currentEpoch < sunriseEpoch
        }
        return false
    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                R.drawable.sunny_bg
            }

            "02", "03", "04" -> {
                R.drawable.cloudy_bg
            }

            "09", "10", "11" -> {
                R.drawable.rainy_bg
            }

            "13" -> {
                R.drawable.snow_bg
            }

            "50" -> {
                R.drawable.haze_bg
            }

            else -> 0
        }
    }

    private fun setEffectRainSnow(icon: String) {
        when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)

            }

            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)

            }

            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)

            }

            "13" -> {
                initWeatherView(PrecipType.SNOW)

            }

            "50" -> {
                initWeatherView(PrecipType.CLEAR)

            }

        }
    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }

}
