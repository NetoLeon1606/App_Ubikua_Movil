package com.example.ep2_.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Archivo para construir todo el enlace del API de OpenStreetMap

object RetrofitClient {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val api: OpenStreetMapApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenStreetMapApi::class.java)
    }
}