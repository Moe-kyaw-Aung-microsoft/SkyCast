# SkyCast

SkyCast is a modern Android weather application built with **Kotlin** that provides **current weather**, **forecast data**, **air quality monitoring**, and **Burmese language support**.

The app is designed to deliver practical weather information in a clean and user-friendly interface.

---

## Features

- Search weather by city
- View current temperature and weather condition
- View weather details:
  - Feels like temperature
  - Humidity
  - Wind speed
  - Pressure
- View air quality information:
  - AQI
  - PM2.5
  - PM10
- View forecast data
- Weather icon support
- English and Burmese language support
- Simple language toggle
- Clean and lightweight UI

---

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **Networking:** HttpURLConnection
- **Async Handling:** Coroutines
- **API Provider:** OpenWeather API
- **Localization:** Android string resources (`values/` and `values-my/`)

---

## APIs Used

SkyCast uses the following OpenWeather APIs:

- **Current Weather API**
- **5 Day / 3 Hour Forecast API**
- **Air Pollution API**

---

## Project Structure

```bash
com.moekyawaung.skycast
├── MainActivity.kt
res/
├── layout/
│   └── activity_main.xml
├── values/
│   └── strings.xml
├── values-my/
│   └── strings.xml
