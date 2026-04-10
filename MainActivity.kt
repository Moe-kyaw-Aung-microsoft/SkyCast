package com.moekyawaung.skycast

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnLang: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var tvCity: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvPressure: TextView
    private lateinit var tvAirQuality: TextView
    private lateinit var tvPm25: TextView
    private lateinit var tvPm10: TextView
    private lateinit var tvForecast: TextView
    private lateinit var tvError: TextView
    private lateinit var ivWeatherIcon: ImageView

    private val apiKey = "YOUR_OPENWEATHER_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeather(city)
            } else {
                showError(getString(R.string.enter_city))
            }
        }

        btnLang.setOnClickListener {
            toggleLanguage()
        }

        fetchWeather("Yangon")
    }

    private fun bindViews() {
        etCity = findViewById(R.id.etCity)
        btnSearch = findViewById(R.id.btnSearch)
        btnLang = findViewById(R.id.btnLang)
        progressBar = findViewById(R.id.progressBar)

        tvCity = findViewById(R.id.tvCity)
        tvTemp = findViewById(R.id.tvTemp)
        tvCondition = findViewById(R.id.tvCondition)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWind = findViewById(R.id.tvWind)
        tvPressure = findViewById(R.id.tvPressure)
        tvAirQuality = findViewById(R.id.tvAirQuality)
        tvPm25 = findViewById(R.id.tvPm25)
        tvPm10 = findViewById(R.id.tvPm10)
        tvForecast = findViewById(R.id.tvForecast)
        tvError = findViewById(R.id.tvError)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
    }

    private fun fetchWeather(city: String) {
        showLoading(true)
        hideError()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weatherUrl =
                    "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
                val weatherResponse = makeRequest(weatherUrl)
                val weatherJson = JSONObject(weatherResponse)

                val cityName = weatherJson.getString("name")
                val main = weatherJson.getJSONObject("main")
                val weatherArray = weatherJson.getJSONArray("weather")
                val weatherObj = weatherArray.getJSONObject(0)
                val windObj = weatherJson.getJSONObject("wind")
                val coord = weatherJson.getJSONObject("coord")

                val temp = main.getDouble("temp")
                val feelsLike = main.getDouble("feels_like")
                val humidity = main.getInt("humidity")
                val pressure = main.getInt("pressure")
                val condition = weatherObj.getString("description")
                val iconCode = weatherObj.getString("icon")
                val wind = windObj.getDouble("speed")
                val lat = coord.getDouble("lat")
                val lon = coord.getDouble("lon")

                val forecastUrl =
                    "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=$apiKey&units=metric"
                val forecastResponse = makeRequest(forecastUrl)
                val forecastText = parseForecast(JSONObject(forecastResponse))

                val airUrl =
                    "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey"
                val airResponse = makeRequest(airUrl)
                val airData = parseAirQuality(JSONObject(airResponse))

                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    tvCity.text = cityName
                    tvTemp.text = "${temp.toInt()}°C"
                    tvCondition.text = condition.replaceFirstChar { it.uppercase() }
                    tvFeelsLike.text = getString(R.string.feels_like_value, feelsLike.toInt().toString())
                    tvHumidity.text = getString(R.string.humidity_value, humidity.toString())
                    tvWind.text = getString(R.string.wind_value, wind.toString())
                    tvPressure.text = getString(R.string.pressure_value, pressure.toString())

                    tvAirQuality.text = getString(R.string.air_quality_value, airData.aqiLabel)
                    tvPm25.text = getString(R.string.pm25_value, airData.pm25)
                    tvPm10.text = getString(R.string.pm10_value, airData.pm10)

                    tvForecast.text = forecastText

                    loadWeatherIcon(iconUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError(getString(R.string.failed_load))
                }
            }
        }
    }

    private fun parseForecast(json: JSONObject): String {
        val list = json.getJSONArray("list")
        val builder = StringBuilder()

        val maxItems = minOf(6, list.length())
        for (i in 0 until maxItems) {
            val item = list.getJSONObject(i)
            val dtTxt = item.getString("dt_txt")
            val main = item.getJSONObject("main")
            val temp = main.getDouble("temp").toInt()
            val weather = item.getJSONArray("weather").getJSONObject(0).getString("main")
            builder.append("• $dtTxt   |   $temp°C   |   $weather\n")
        }
        return builder.toString()
    }

    private fun parseAirQuality(json: JSONObject): AirData {
        val item = json.getJSONArray("list").getJSONObject(0)
        val main = item.getJSONObject("main")
        val components = item.getJSONObject("components")

        val aqi = main.getInt("aqi")
        val pm25 = String.format("%.1f", components.getDouble("pm2_5"))
        val pm10 = String.format("%.1f", components.getDouble("pm10"))

        val label = when (aqi) {
            1 -> getString(R.string.aqi_good)
            2 -> getString(R.string.aqi_fair)
            3 -> getString(R.string.aqi_moderate)
            4 -> getString(R.string.aqi_poor)
            5 -> getString(R.string.aqi_very_poor)
            else -> getString(R.string.unknown)
        }

        return AirData(label, pm25, pm10)
    }

    private fun loadWeatherIcon(iconUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(iconUrl)
                val connection = url.openConnection()
                connection.connect()
                val input = connection.getInputStream()
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)

                withContext(Dispatchers.Main) {
                    ivWeatherIcon.setImageBitmap(bitmap)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun makeRequest(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP error: $responseCode")
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        reader.close()
        connection.disconnect()

        return response.toString()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        tvError.visibility = View.VISIBLE
        tvError.text = message
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }

    private fun toggleLanguage() {
        val current = resources.configuration.locales[0].language
        val newLang = if (current == "my") "en" else "my"
        setLocale(newLang)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    data class AirData(
        val aqiLabel: String,
        val pm25: String,
        val pm10: String
    )
}
